package br.ufsc.gsigma.services.execution.bpel.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.ode.axis2.ExternalService;
import org.apache.ode.axis2.util.ClusterUrlTransformer;
import org.apache.ode.axis2.util.SoapMessageConverter;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.epr.URLEndpoint;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.Scheduler;
import org.w3c.dom.Node;

import com.ibm.wsdl.PortImpl;
import com.ibm.wsdl.ServiceImpl;
import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.binding.model.BindingConfiguration;
import br.ufsc.gsigma.binding.model.InvokedServiceInfo;
import br.ufsc.gsigma.binding.util.BindingServiceConstants;
import br.ufsc.gsigma.binding.util.ServiceEndpointUtil;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.infrastructure.ws.Headers;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.execution.bpel.bootstrap.RunExecutionService;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel.InfinispanProcessInstanceDAO;
import br.ufsc.gsigma.services.execution.bpel.ode.AxisODEServer;
import br.ufsc.gsigma.services.execution.bpel.ode.SoapExternalService;
import br.ufsc.gsigma.services.execution.bpel.util.ProcessUtil;
import br.ufsc.gsigma.services.execution.events.ProcessTaskStartedEvent;

public class BindingServiceExternalService implements ExternalService {

	private static final Logger logger = Logger.getLogger(BindingServiceExternalService.class);

	private PortType portType;

	private URLEndpoint endpointReference;

	private ExternalService externalService;

	private static final Method METHOD_MESSAGE_EXCHANGE_GET_DAO;

	private final ThreadLocal<ProcessInstanceDAO> processInstanceThreadLocal = new ThreadLocal<ProcessInstanceDAO>();

	static {
		try {
			Class<?> clazz = BindingServiceExternalService.class.getClassLoader().loadClass("org.apache.ode.bpel.engine.MessageExchangeImpl");
			METHOD_MESSAGE_EXCHANGE_GET_DAO = clazz.getMethod("getDAO");
			METHOD_MESSAGE_EXCHANGE_GET_DAO.setAccessible(true);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public BindingServiceExternalService(PortType portType, Endpoint initialPartnerEndpoint, ProcessConf processConf, ExecutionBindingContext bindingContext) {

		this.portType = portType;

		this.endpointReference = new LazyURLEndpoint() {
			@Override
			protected String loadUrl() {
				BindingConfiguration bindingConfiguration = getBindingConfiguration(processConf);
				return bindingConfiguration.getServiceMediationEndpoint();
			}
		};

		AxisODEServer server = bindingContext.getServer();

		try {
			Definition definitionDelegate = processConf.getDefinitionForPortType(portType.getQName());

			Endpoint endpoint = constructEndpoint(definitionDelegate, portType);

			Definition definition = new WSDLDefinitionAdapter(processConf, definitionDelegate, portType, endpoint);

			this.externalService = new BindingServiceSoapExternalService(processConf, definition, endpoint.serviceName, endpoint.portName, server.getExecutorService(), server.getAxisConfig(), server.getScheduler(), server.getBpelServer(), server.getHttpConnectionManager(),
					server.getClusterUrlTransformer());

		} catch (AxisFault e) {
			logger.error("Could not create external service.", e);
			throw new ContextException("Error creating external service! name:" + getServiceName() + ", port:" + getPortName(), e);
		}
	}

	private Endpoint constructEndpoint(Definition definition, PortType portType) {
		return new Endpoint(definition.getQName(), portType.getQName().getLocalPart() + "Port");
	}

	@Override
	public void invoke(PartnerRoleMessageExchange odeMex) {

		try {
			MessageExchangeDAO messageExchangeDAO = (MessageExchangeDAO) METHOD_MESSAGE_EXCHANGE_GET_DAO.invoke(odeMex);
			processInstanceThreadLocal.set(messageExchangeDAO.getInstance());
			this.externalService.invoke(odeMex);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			processInstanceThreadLocal.remove();
		}

	}

	@Override
	public String getPortName() {
		return BindingServiceConstants.WS_SERVICE_PORT_NAME.getLocalPart();
	}

	@Override
	public QName getServiceName() {
		return BindingServiceConstants.WS_SERVICE_NAME;
	}

	@Override
	public EndpointReference getInitialEndpointReference() {
		return endpointReference;
	}

	@Override
	public void close() {
	}

	private class WSDLDefinitionAdapter extends WSDLDefinitionDelegate {

		private static final long serialVersionUID = 1L;

		private Service service;

		private PortType portType;

		@SuppressWarnings("unchecked")
		public WSDLDefinitionAdapter(ProcessConf processConf, Definition delegate, PortType portType, Endpoint endpoint) {

			super(delegate);

			this.service = new ServiceImpl();
			this.service.setQName(endpoint.serviceName);
			this.portType = portType;

			Binding binding = null;

			final QName portQName = portType.getQName();

			for (Binding b : (Collection<Binding>) delegate.getBindings().values()) {
				PortType bindingPortType = b.getPortType();
				if (bindingPortType != null && portQName.equals(bindingPortType.getQName())) {
					if (binding != null)
						throw new ContextException("More than one binding defined for portType " + portQName);
					binding = b;
				}
			}

			Port port = new PortImpl();
			port.setName(endpoint.portName);
			port.setBinding(binding);

			// Lazy endpoint
			SOAPAddress soapAddress = new SOAPAddressImpl() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getLocationURI() {
					BindingConfiguration bindingConfiguration = getBindingConfiguration(processConf);
					return bindingConfiguration.getServiceMediationEndpoint();
				}
			};

			port.addExtensibilityElement(soapAddress);

			service.addPort(port);
		}

		@Override
		public Service getService(QName name) {
			return service;
		}

		public PortType getPortType() {
			return portType;
		}

	}

	private static Map<Long, InvokeSync> syncs = new ConcurrentHashMap<Long, InvokeSync>();

	static InvokeSync getInvokeSync(Long processInstanceId) {
		return syncs.computeIfAbsent(processInstanceId, pid -> new InvokeSync(processInstanceId));
	}

	static class InvokeSync {

		private Long processInstanceId;

		public InvokeSync(Long processInstanceId) {
			this.processInstanceId = processInstanceId;
		}

		public synchronized void waitForReceiveReady() throws InterruptedException {
			wait();
		}

		public synchronized void notifyReceiveReady() {
			try {
				notifyAll();
			} finally {
				syncs.remove(processInstanceId);
			}
		}

	}

	private class BindingServiceSoapExternalService extends SoapExternalService {

		private ExecutionBpelServer server;

		private ProcessConf processConf;

		public BindingServiceSoapExternalService(ProcessConf processConf, Definition definition, QName serviceName, String portName, ExecutorService executorService, AxisConfiguration axisConfig, Scheduler sched, BpelServer server, MultiThreadedHttpConnectionManager connManager,
				ClusterUrlTransformer clusterUrlTransformer) throws AxisFault {
			super(processConf, definition, serviceName, portName, executorService, axisConfig, sched, server, connManager, clusterUrlTransformer);
			this.server = (ExecutionBpelServer) server;
			this.processConf = processConf;
		}

		@Override
		protected SoapMessageConverter createSoapMessageConverter(BpelServer server, Definition definition, QName serviceName, String portName) throws AxisFault {
			return new BindingServiceSoapMessageConverter((ExecutionBpelServer) server, (WSDLDefinitionAdapter) definition, serviceName, portName);
		}

		@Override
		public void invoke(PartnerRoleMessageExchange odeMex) {

			// ExecutionContext executionContext = ExecutionContext.get();
			//
			// if (executionContext != null) {
			// executionContext.incrementTimestamp();
			// }

			super.invoke(odeMex);
		}

		@Override
		protected void invokeOneWay(final PartnerRoleMessageExchange odeMex, final MessageContext mctx, final OperationClient operationClient) {

			mctx.setProperty(HTTPConstants.SO_TIMEOUT, 120 * 1000);

			final InfinispanProcessInstanceDAO processInstanceDAO = (InfinispanProcessInstanceDAO) processInstanceThreadLocal.get();

			_executorService.submit(new Callable<Object>() {
				public Object call() throws Exception {

					QName processId = null;

					Long instanceId = null;

					try {

						ProcessDAO processDAO = processInstanceDAO.getProcess();

						processId = processDAO.getProcessId();

						instanceId = processInstanceDAO.getInstanceId();

						getInvokeSync(instanceId).waitForReceiveReady();

						String serviceClassification = ServiceEndpointUtil.namespaceToServiceClassification(portType.getQName().getNamespaceURI());

						logger.debug("Sending message to service --> " + serviceClassification + ", processInstanceId=" + instanceId);

						Date startTime = new Date();

						ExecutionContext executionContext = ExecutionContext.get();

						// if (executionContext != null) {
						// processInstanceDAO.updateExecutionContext(executionContext);
						// } else {
						// logger.warn("executionContext is null for invocation");
						// }

						boolean stop = false;

						int currentAttempt = 0;
						int maxAttempts = 5;

						next_attempt: while (!stop) {

							AxisFault axisFault = null;

							InputStream inputStream = null;

							// String encoding = "UTF-8";

							int statusCode = -1;

							try {
								operationClient.execute(true);

								MessageContext msgContext = operationClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);

								statusCode = (int) ObjectUtils.defaultIfNull(msgContext.getProperty(HTTPConstants.MC_HTTP_STATUS_CODE), -1);

								if (statusCode != -1) {

									// Map<String, String> transportInfoMap = (Map<String, String>) msgContext.getProperty(Constants.Configuration.TRANSPORT_INFO_MAP);
									// encoding = transportInfoMap != null ? transportInfoMap.get(Constants.Configuration.CHARACTER_SET_ENCODING) : "UTF-8";

									inputStream = (InputStream) msgContext.getProperty(MessageContext.TRANSPORT_IN);

									String content = null;

									if (inputStream != null) {
										try (InputStream in = inputStream) {
											content = IOUtils.toString(in, StandardCharsets.UTF_8);
										}
									}

									logger.debug("Invocation of service with namespace " + portType.getQName().getNamespaceURI() + " returned with status " + statusCode + (StringUtils.isNotEmpty(content) ? " --> " + content : ""));
								}

							} catch (AxisFault e) {
								axisFault = e;
							}

							if (axisFault != null || statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {

								String errorMessage = null;

								if (inputStream != null) {
									try (InputStream in = inputStream) {
										errorMessage = IOUtils.toString(in, StandardCharsets.UTF_8);
									}
								} else if (axisFault != null) {
									errorMessage = axisFault.getMessage();
								}

								boolean retry = ++currentAttempt <= maxAttempts;

								if (retry) {
									logger.warn(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_SERVICE_BINDING, "Invocation failed for service of namespace '" + portType.getQName().getNamespaceURI() + "'. Retrying (" + currentAttempt + "/" + maxAttempts + ")\n\tCause: " + errorMessage));

									Thread.sleep(500L);
									continue next_attempt;
								} else {
									logger.error(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_SERVICE_BINDING, "Invocation failed for service of namespace '" + portType.getQName().getNamespaceURI() + "'. Aborting process instance with id " + instanceId + "\n\tCause: " + errorMessage));

								}

								ExecutionBpelEngine engine = server.getExecutionBpelEngine();
								engine.terminateProcessInstance(processDAO.getProcessId(), instanceId);

							} else {

								if (currentAttempt > 0) {
									logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_SERVICE_BINDING, "Invocation successful for service of namespace '" + portType.getQName().getNamespaceURI() + "' after attempt (" + currentAttempt + "/" + maxAttempts + ")"));
								}

								final String processName = processDAO.getType().getLocalPart();

								RequestBindingContext requestBindingContext = RequestBindingContext.get();

								int invocationNumber = 1;

								if (executionContext != null) {

									String scSuffix = serviceClassification.substring(serviceClassification.lastIndexOf('/', serviceClassification.lastIndexOf('/') - 1) + 1).replace("/", "_");

									for (InvokedServiceInfo is : executionContext.getAttributes(LogConstants.PREFIX_INVOKED_SERVICE + scSuffix, InvokedServiceInfo.class).values()) {
										if (!is.isError()) {
											invocationNumber++;
										}
									}
								}

								ProcessTaskStartedEvent evt = new ProcessTaskStartedEvent(ProcessUtil.getProcessExecutionServiceUrl(), requestBindingContext.getApplicationId(), requestBindingContext.getProcessURL(), processConf.getProcessId(), processName, instanceId, serviceClassification,
										invocationNumber, startTime);

								EventSender.getInstance().sendEvent(evt, -1);
							}

							stop = true;
						}

					} catch (Throwable t) {
						String errmsg = "Error sending message (mex=" + odeMex + "): " + t.getMessage();
						__log.error(errmsg, t);

						if (processId != null && instanceId != null) {
							ExecutionBpelEngine engine = server.getExecutionBpelEngine();
							engine.terminateProcessInstance(processId, instanceId);
						}

					} finally {
						// release the HTTP connection, we don't need it anymore
						TransportOutDescription out = mctx.getTransportOut();
						if (out != null && out.getSender() != null) {
							out.getSender().cleanup(mctx);
						}
					}
					return null;
				}

				@Override
				public String toString() {
					return "Invoke service " + portType.getQName().getNamespaceURI();
				}
			});

			odeMex.replyOneWayOk();
		}
	}

	private class BindingServiceSoapMessageConverter extends SoapMessageConverter {

		private ExecutionBpelServer server;

		private SOAPFactory soapFactory;

		private WSDLDefinitionAdapter def;

		public BindingServiceSoapMessageConverter(ExecutionBpelServer server, WSDLDefinitionAdapter def, QName serviceName, String portName) throws AxisFault {
			super(def, serviceName, portName);
			this.server = server;
			this.def = def;
			this.soapFactory = OMAbstractFactory.getSOAP11Factory();
		}

		@Override
		public void createSoapHeaders(SOAPEnvelope soapEnv, List<SOAPHeader> headerDefs, Message msgdef, Map<String, Node> headers) throws AxisFault {

			super.createSoapHeaders(soapEnv, headerDefs, msgdef, headers);

			org.apache.axiom.soap.SOAPHeader soaphdr = soapEnv.getHeader();

			if (soaphdr == null)
				soaphdr = soapFactory.createSOAPHeader(soapEnv);

			OMElement requestTimestamp = soapFactory.createOMElement(Headers.HEADER_REQUEST_TIMESTAMP);
			requestTimestamp.setText(String.valueOf(System.currentTimeMillis()));
			soaphdr.addChild(requestTimestamp);

			// ExecutionContext
			ExecutionContext executionContext = ExecutionContext.get();
			if (executionContext != null) {
				try {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					JAXBSerializerUtil.write(executionContext, os, false);
					soaphdr.addChild(XMLUtils.toOM(new ByteArrayInputStream(os.toByteArray()), true));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			// BindingContext
			ProcessInstanceDAO processInstanceDAO = processInstanceThreadLocal.get();
			QName processId = processInstanceDAO.getProcess().getProcessId();

			ProcessConf processConf = server.getExecutionBpelEngine().getProcess(processId).getConf();

			try {

				String applicationId = ProcessUtil.getApplicationId(processConf);

				BindingConfiguration bindingConfiguration = getBindingConfiguration(processConf);

				RequestBindingContext ctx = new RequestBindingContext();
				ctx.setToken(bindingConfiguration.getBindingToken());
				ctx.setApplicationId(applicationId);
				ctx.setProcessNS(processId.getNamespaceURI());
				ctx.setProcessName(processId.getLocalPart());
				ctx.setProcessInstanceId(String.valueOf(processInstanceDAO.getInstanceId()));
				ctx.setPortTypeNS(def.getPortType().getQName().getNamespaceURI());
				ctx.setPortTypeName(def.getPortType().getQName().getLocalPart());

				ctx.set();

				ByteArrayOutputStream os = new ByteArrayOutputStream();
				JAXBSerializerUtil.write(ctx, os, false);
				soaphdr.addChild(XMLUtils.toOM(new ByteArrayInputStream(os.toByteArray()), true));

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}

		@Override
		public void createSoapRequest(MessageContext msgCtx, org.apache.ode.bpel.iapi.Message message, Operation op) throws AxisFault {
			super.createSoapRequest(msgCtx, message, op);
			rewriteCallbackEndpointUrl(msgCtx);
		}

		private void rewriteCallbackEndpointUrl(MessageContext msgCtx) {
			OMElement e = msgCtx.getEnvelope().getBody().getFirstElement();

			OMElement processContext = e != null ? e.getFirstChildWithName(new QName("processContext")) : null;

			if (processContext != null) {
				OMElement callbackEndpoint = processContext.getFirstChildWithName(new QName("callbackEndpoint"));

				if (callbackEndpoint != null) {

					try {

						String addr = callbackEndpoint.getText();

						URL url = new URL(addr);

						addr = addr.replaceAll(url.getHost(), System.getProperty(RunExecutionService.EXECUTION_SERVICE_HOSTNAME));
						addr = addr.replaceAll(":" + url.getPort() + "/", ":" + System.getProperty(RunExecutionService.EXECUTION_SERVICE_PORT) + "/");

						callbackEndpoint.setText(addr);

						// ProcessInstanceDAO processInstanceDAO = processInstanceThreadLocal.get();
						// QName processId = processInstanceDAO.getProcess().getProcessId();
						// ProcessConf processConfig = server.getExecutionBpelEngine().getProcess(processId).getConf();
						// BindingConfiguration bindingConfiguration = getBindingConfiguration(processConfig);
						// callbackEndpoint.setText(bindingConfiguration.getServiceMediationEndpoint());

					} catch (Exception e1) {
					}
				}
			}
		}
	}

	private BindingConfiguration getBindingConfiguration(ProcessConf processConf) {

		try {
			BindingConfiguration bindingConfiguration = BindingServiceProcessCorrelator.getInstance().getBindingConfiguration(processConf);

			if (bindingConfiguration == null) {
				throw new ContextException("Binding configuration not found for process " + processConf.getProcessId());
			}
			return bindingConfiguration;

		} catch (InterruptedException e) {
			throw new ContextException("Interrupted waiting for binding configuration for process " + processConf.getProcessId());
		}

	}
}
