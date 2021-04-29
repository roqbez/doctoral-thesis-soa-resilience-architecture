package br.ufsc.gsigma.services.execution.bpel.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.apache.ode.axis2.ODEService;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.store.ProcessConfImpl;
import org.apache.ode.utils.HierarchicalProperties;
import org.apache.ode.utils.Properties;
import org.apache.ode.utils.WatchDog;

import com.google.common.util.concurrent.Striped;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.binding.model.InvokedServiceInfo;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.util.thread.ThreadLocalHolder;
import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.HTTPRequestInfo;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel.InfinispanProcessInstanceDAO;
import br.ufsc.gsigma.services.execution.bpel.util.ProcessUtil;
import br.ufsc.gsigma.services.execution.events.ProcessTaskFinishedEvent;
import br.ufsc.services.core.util.json.JsonUtil;

public class ExecutionODEService extends ODEService {

	private static final Logger logger = Logger.getLogger(ExecutionODEService.class);

	private Striped<Lock> locker = Striped.lock(512);

	private ExecutionBpelServer executionBpelServer;

	private static final Field fCacheOfImmutableMaps;

	private static final Field fPropertiesWatchDog;

	static {
		try {
			fCacheOfImmutableMaps = HierarchicalProperties.class.getDeclaredField("cacheOfImmutableMaps");
			fCacheOfImmutableMaps.setAccessible(true);

			fPropertiesWatchDog = ProcessConfImpl.class.getDeclaredField("propertiesWatchDog");
			fPropertiesWatchDog.setAccessible(true);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ExecutionODEService(AxisService axisService, ProcessConf processConf, QName serviceName, String portName, BpelServer server, TransactionManager txManager) throws AxisFault {
		super(axisService, processConf, serviceName, portName, server, txManager);
		this.executionBpelServer = (ExecutionBpelServer) server;

		try {

			WatchDog propertiesWatchDog = (WatchDog) fPropertiesWatchDog.get(processConf);
			HierarchicalProperties props = (HierarchicalProperties) propertiesWatchDog.getObserver().get();

			MultiKeyMap cacheOfImmutableMaps = (MultiKeyMap) fCacheOfImmutableMaps.get(props);

			final Map eprCfg = executionBpelServer.getEndpointReferenceContext().getConfigLookup(getMyServiceRef());
			final QName service = (QName) eprCfg.get("service");
			final String port = (String) eprCfg.get("port");

			Map<String, String> m = (Map<String, String>) cacheOfImmutableMaps.get(service, port);

			if (m == null) {
				m = new HashMap<String, String>();
				m.put(Properties.PROP_MEX_TIMEOUT, String.valueOf(1 * 1000));
				cacheOfImmutableMaps.put(service, port, Collections.unmodifiableMap(m));
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void onAxisMessageExchange(MessageContext msgContext, MessageContext outMsgContext, SOAPFactory soapFactory) throws AxisFault {

		long currentTime = System.currentTimeMillis();

		SOAPEnvelope envelope = msgContext.getEnvelope();

		if (logger.isTraceEnabled()) {
			logger.trace("Received message with SOAP envelope --> " + envelope);
		}

		// if (logger.isDebugEnabled()) {
		// logger.debug("Received message with SOAP body" //
		// + "\n\treceivedMessage=" + envelope.getBody());
		// }

		// ClusteredLock lock = null;
		Lock lock = null;

		ExecutionContext executionContext = null;
		RequestBindingContext requestBindingContext = null;

		String threadName = null;

		try {

			SOAPHeader soapHeader = envelope.getHeader();

			if (soapHeader != null) {

				try {
					Iterator<OMNode> it = soapHeader.getChildren();
					while (it.hasNext()) {
						OMElement oMElement = (OMElement) it.next();

						if (oMElement.getQName().equals(ExecutionContext.QNAME)) {
							executionContext = JAXBSerializerUtil.read(ExecutionContext.class, XMLUtils.toDOM(oMElement));
						} else if (oMElement.getQName().equals(RequestBindingContext.QNAME)) {
							requestBindingContext = JAXBSerializerUtil.read(RequestBindingContext.class, XMLUtils.toDOM(oMElement));
						}
					}

					InfinispanProcessInstanceDAO processInstanceDAO = null;

					ExecutionContext instanceExecutionContext = null;

					if (requestBindingContext != null) {
						String processNamespace = requestBindingContext.getProcessNS();
						String processName = requestBindingContext.getProcessName();
						String processInstanceId = requestBindingContext.getProcessInstanceId();
						if (processInstanceId != null) {

							String key = InfinispanDatabase.getProcessInstanceLockKey(processNamespace, processName, processInstanceId);

							lock = locker.get(key);
							lock.lock();

							String threadSuffix = requestBindingContext.getInvokedServiceInfo() != null ? "-" + requestBindingContext.getInvokedServiceInfo().getServiceEndpointInfo().getServiceClassification().replaceAll("/", "_").toLowerCase() : "";
							threadName = Thread.currentThread().getName() + "-pid:" + processInstanceId + threadSuffix;

							processInstanceDAO = (InfinispanProcessInstanceDAO) executionBpelServer.getOdeServer().getBpelDAOConnectionFactory().getConnection().getInstance(Long.valueOf(processInstanceId));
							instanceExecutionContext = processInstanceDAO.getExecutionContext();
						}
					}

					if (executionContext != null && requestBindingContext != null) {

						String ns = requestBindingContext.getPortTypeNS();

						int executionContextTstmp = executionContext.getTimestamp();

						// Received parallel replies, need to merge execution contexts
						if (instanceExecutionContext != null && instanceExecutionContext.getTimestamp() > executionContextTstmp) {

							List<InvokedServiceInfo> instanceInvokedServices = new ArrayList<InvokedServiceInfo>(instanceExecutionContext.getAttributes(LogConstants.PREFIX_INVOKED_SERVICE, InvokedServiceInfo.class).values());

							List<InvokedServiceInfo> incomingRequestInvokedServices = new ArrayList<InvokedServiceInfo>(executionContext.getAttributes(LogConstants.PREFIX_INVOKED_SERVICE, InvokedServiceInfo.class).values());

							String lastCommonInvokedService = null;
							int lastCommonIndex = -1;

							for (int i = 0; i < Math.min(instanceInvokedServices.size(), incomingRequestInvokedServices.size()); i++) {

								String s1 = getInvokedServiceInfoKey(instanceInvokedServices.get(i), ns);
								String s2 = getInvokedServiceInfoKey(incomingRequestInvokedServices.get(i), ns);

								if (ObjectUtils.equals(s1, s2)) {
									lastCommonInvokedService = s1;
									lastCommonIndex = i;
								} else {
									break;
								}
							}

							if (lastCommonIndex != -1) {
								List<InvokedServiceInfo> invokedServicesToAdd = instanceInvokedServices.subList(lastCommonIndex + 1, instanceInvokedServices.size());

								for (InvokedServiceInfo s : invokedServicesToAdd) {
									executionContext.addAttribute(getInvokedServiceInfoKey(s, ns), s);
								}

								String json = executionContext.getAttribute(LogConstants.INVOKED_SERVICES_FORKS, String.class);
								List<String> invokedServicesForks = json != null ? (List<String>) JsonUtil.getTypedListValue(json, String.class) : null;

								if (invokedServicesForks == null) {
									invokedServicesForks = new ArrayList<String>();
								}

								invokedServicesForks.add(lastCommonInvokedService);

								executionContext.addAttribute(LogConstants.INVOKED_SERVICES_FORKS, invokedServicesForks);
							}

							// TODO:...
						}

						InvokedServiceInfo invokedServiceInfo = requestBindingContext.getInvokedServiceInfo();

						if (invokedServiceInfo != null) {

							String invokedServiceField = getInvokedServiceInfoKey(invokedServiceInfo, ns);
							invokedServiceInfo.setInvokeFinishTime(currentTime);
							executionContext.addAttribute(invokedServiceField, invokedServiceInfo);
						}

						executionContext.incrementTimestamp();

						executionContext.set();

						requestBindingContext.set();

						if (processInstanceDAO != null) {

							processInstanceDAO.setExecutionContext(executionContext);

							// Write to cache in async
							final InfinispanProcessInstanceDAO pinsDAO = processInstanceDAO;
							final ExecutionContext exCtx = executionContext;
							executionBpelServer.getOdeServer().getExecutorService().submit(() -> {
								pinsDAO.updateExecutionContext(exCtx);
							});
						}

						// HttpServletRequest httpRequest = (HttpServletRequest) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
						// executionContext.addAttribute(ExecutionAttributes.ATT_HTTP_REQUEST_INFO, buildHTTPRequestInfoFromServletRequest(httpRequest));

						if (logger.isDebugEnabled()) {
							Map<String, InvokedServiceInfo> invokedServicesMap = executionContext.getAttributes(LogConstants.PREFIX_INVOKED_SERVICE, InvokedServiceInfo.class);

							StringBuilder sb = new StringBuilder();

							for (Entry<String, InvokedServiceInfo> e : invokedServicesMap.entrySet()) {
								sb.append("\n\t\t" + e.getKey() + "=" + e.getValue());
							}

							if (logger.isTraceEnabled()) {
								logger.trace("Received a reply from service --> " + invokedServiceInfo.getServiceEndpointInfo().getServiceClassification() + " for processName=" + requestBindingContext.getProcessName() + ", processInstanceId=" + requestBindingContext.getProcessInstanceId() //
										+ "\n\tCurrent invoked services are:" + sb.toString());
							} else {
								logger.debug("Received a reply from service --> " + invokedServiceInfo.getServiceEndpointInfo().getServiceClassification() + " for processName=" + requestBindingContext.getProcessName() + ", processInstanceId=" + requestBindingContext.getProcessInstanceId());
							}
						}

						if (logger.isTraceEnabled()) {
							logger.trace("Received message with log attributes --> " + ExecutionLogMessage.getLogMessageAttributes(executionContext));
						}

						sendProcessTaskFinishedEvent(executionContext, requestBindingContext, currentTime);
					}

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new AxisFault(e.getMessage());
				}
			}

			String previousThreadName = null;

			try {

				if (threadName != null) {
					previousThreadName = Thread.currentThread().getName();
					Thread.currentThread().setName(threadName);
				}

				super.onAxisMessageExchange(msgContext, outMsgContext, soapFactory);

			} finally {
				if (previousThreadName != null) {
					Thread.currentThread().setName(previousThreadName);
				}
			}

		} finally {
			if (lock != null) {
				lock.unlock();
			}
			ThreadLocalHolder.clearThreadLocal();
		}
	}

	private String getInvokedServiceInfoKey(InvokedServiceInfo invokedServiceInfo, String ns) {
		return LogConstants.PREFIX_INVOKED_SERVICE + ns.substring(ns.lastIndexOf('/', ns.lastIndexOf('/') - 1) + 1).replace("/", "_") + "_" + invokedServiceInfo.getRepeatCount() + "_" + invokedServiceInfo.getInvokeAttempt();
	}

	private void sendProcessTaskFinishedEvent(ExecutionContext executionContext, RequestBindingContext requestBindingContext, long currentTime) {

		QName processId = new QName(requestBindingContext.getProcessNS(), requestBindingContext.getProcessName());

		String serviceClassification = requestBindingContext.getInvokedServiceInfo().getServiceEndpointInfo().getServiceClassification();

		int invocationNumber = 0;

		String scSuffix = serviceClassification.substring(serviceClassification.lastIndexOf('/', serviceClassification.lastIndexOf('/') - 1) + 1).replace("/", "_");

		for (InvokedServiceInfo is : executionContext.getAttributes(LogConstants.PREFIX_INVOKED_SERVICE + scSuffix, InvokedServiceInfo.class).values()) {
			if (!is.isError()) {
				invocationNumber++;
			}
		}

		ProcessTaskFinishedEvent evt = new ProcessTaskFinishedEvent(ProcessUtil.getProcessExecutionServiceUrl(), requestBindingContext.getApplicationId(), requestBindingContext.getProcessURL(), processId, requestBindingContext.getProcessName(),
				Long.valueOf(requestBindingContext.getProcessInstanceId()), serviceClassification, invocationNumber, new Date(currentTime));

		EventSender.getInstance().sendEvent(evt, -1);
	}

	@SuppressWarnings("unused")
	private HTTPRequestInfo buildHTTPRequestInfoFromServletRequest(HttpServletRequest request) {

		HTTPRequestInfo requestInfo = new HTTPRequestInfo();

		requestInfo.setMethod(request.getMethod());
		requestInfo.setContextPath(request.getContextPath());
		requestInfo.setServletPath(request.getServletPath());
		requestInfo.setScheme(request.getScheme());
		requestInfo.setRequestURI(request.getRequestURI());
		requestInfo.setRequestURL(request.getRequestURL() != null ? request.getRequestURL().toString() : null);
		requestInfo.setQueryString(request.getQueryString());

		requestInfo.setPathInfo(request.getPathInfo());
		requestInfo.setServerName(request.getServerName());
		requestInfo.setServerPort(request.getServerPort());
		requestInfo.setRemoteAddr(request.getRemoteAddr());
		requestInfo.setSecure("https".equalsIgnoreCase(request.getScheme()));

		return requestInfo;

	}

}
