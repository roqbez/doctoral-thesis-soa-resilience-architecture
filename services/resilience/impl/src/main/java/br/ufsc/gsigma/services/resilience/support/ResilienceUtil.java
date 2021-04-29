package br.ufsc.gsigma.services.resilience.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import br.ufsc.gsigma.binding.model.BindingConfiguration;
import br.ufsc.gsigma.binding.model.NamespaceServices;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.QoSCriterion;
import br.ufsc.gsigma.catalog.services.model.ServiceAssociation;
import br.ufsc.gsigma.catalog.services.model.ServiceConfig;
import br.ufsc.gsigma.catalog.services.model.ServicesInformation;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionAttributes;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionFlags;
import br.ufsc.gsigma.servicediscovery.interfaces.DiscoveryService;
import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryRequest;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryRequestItem;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryResult;
import br.ufsc.gsigma.servicediscovery.model.QoSConstraint;
import br.ufsc.gsigma.servicediscovery.model.QoSValueComparisionType;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;
import br.ufsc.gsigma.services.resilience.interfaces.ResilienceService;
import br.ufsc.gsigma.services.resilience.model.ResilienceInfo;
import br.ufsc.gsigma.services.resilience.model.ServicesCheckResult;
import br.ufsc.gsigma.services.resilience.util.ResilienceParams;

public abstract class ResilienceUtil {

	private static final Logger logger = Logger.getLogger(ResilienceUtil.class);

	public static boolean isServiceAlive(SOAService service) {
		return isServiceAlive(logger, service);
	}

	public static boolean isServiceAlive(Logger logger, SOAService service) {

		int connectionTimeout = ExecutionContext.getAttribute(ExecutionAttributes.ATT_RESILIENCE_SERVICE_CHECK_HTTP_CONNECTION_TIMEOUT, Integer.class, ResilienceParams.DEFAULT_HTTP_CONNECTION_TIMEOUT);

		int readTimeout = ExecutionContext.getAttribute(ExecutionAttributes.ATT_RESILIENCE_SERVICE_CHECK_HTTP_READ_TIMEOUT, Integer.class, ResilienceParams.DEFAULT_HTTP_READ_TIMEOUT);

		return isServiceAlive(logger, service, connectionTimeout, readTimeout);

	}

	public static boolean isServiceAlive(SOAService service, int connectionTimeout, int readTimeout) {
		return isServiceAlive(logger, service, connectionTimeout, readTimeout);
	}

	public static boolean isServiceAlive(String serviceEndpointURL, String serviceNamespace, int connectionTimeout, int readTimeout) throws SOAPException, IOException {
		return isServiceAlive(logger, serviceEndpointURL, serviceNamespace, connectionTimeout, readTimeout);
	}

	public static boolean isServiceAlive(Logger logger, String serviceEndpointURL, String serviceNamespace, int connectionTimeout, int readTimeout) throws SOAPException, IOException {
		try {
			return isServiceAliveInternal(logger, serviceEndpointURL, serviceNamespace, connectionTimeout, readTimeout);

		} catch (SOAPException | IOException e) {
			return false;
		}
	}

	public static boolean isServiceAlive(Logger logger, SOAService service, int connectionTimeout, int readTimeout) {

		try {

			return isServiceAliveInternal(logger, service.getServiceEndpointURL(), service.getServiceNamespace(), connectionTimeout, readTimeout);

		} catch (SOAPException | IOException e) {

			if (logger.isDebugEnabled()) {
				logger.debug(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_CHECK, "Service at " + service.getServiceEndpointURL() + " is unavailable: " + e.getMessage() + "\n\tEndpoint: " + service.getServiceEndpointURL()) //
						.addTransientAttribute(LogConstants.SERVICE_TARGET_URL, service.getServiceEndpointURL()) //
						.addTransientAttribute(LogConstants.SERVICE_CLASSIFICATION, service.getServiceClassification()) //
						.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_KEY, service.getServiceKey()) //
						.addTransientAttribute(LogConstants.SERVICE_UDDI_SERVICE_BINDING_KEY, service.getBindingTemplateKey()) //
				);
			}
			return false;
		}
	}

	private static boolean isServiceAliveInternal(Logger logger, String serviceEndpointURL, String serviceNamespace, int connectionTimeout, int readTimeout) throws SOAPException, IOException {

		SOAPMessage soapMessage = buildAliveMessage(serviceNamespace);

		HttpURLConnection connection = (HttpURLConnection) new URL(serviceEndpointURL).openConnection();
		connection.addRequestProperty("Content-Type", "text/xml;charset=UTF-8");
		connection.addRequestProperty("SOAPAction", serviceNamespace + "/alive");
		connection.setConnectTimeout(connectionTimeout);
		connection.setReadTimeout(readTimeout);

		ByteArrayInputStream is = null;

		try {
			connection.setDoOutput(true);

			try (OutputStream out = connection.getOutputStream()) {
				soapMessage.writeTo(out);
			}

			if (connection.getResponseCode() != 200)
				return false;

			try (InputStream in = connection.getInputStream()) {
				is = new ByteArrayInputStream(IOUtils.toByteArray(connection.getInputStream()));
			}

		} finally {
			connection.disconnect();
		}

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage responseMessage = messageFactory.createMessage(null, is);
		SOAPBody body = responseMessage.getSOAPBody();
		Node c = body.getFirstChild();
		c = c != null ? c.getFirstChild() : null;

		return c != null ? "true".equals(c.getTextContent()) : false;

	}

	public static boolean isApplicationUsesServiceNamespace(SOAApplication application, String serviceNamespace) {

		for (ServiceConfig cfg : application.getServicesInformation().getServicesConfig()) {

			if (serviceNamespace.equals(cfg.getNamespace()))
				return true;
		}

		return false;
	}

	public static boolean isApplicationWithoutBoundServices(SOAApplication application) {
		return isApplicationWithoutBoundServices(application, null);
	}

	public static boolean isApplicationWithoutBoundServices(SOAApplication application, String serviceNamespace) {

		BindingConfiguration bindingConfiguration = application.getBindingConfiguration();

		if (bindingConfiguration == null || CollectionUtils.isEmpty(bindingConfiguration.getBoundServices()))
			return true;

		for (ServiceConfig cfg : application.getServicesInformation().getServicesConfig()) {

			String namespace = cfg.getNamespace();

			if (serviceNamespace != null) {

				if (namespace.equals(serviceNamespace)) {

					NamespaceServices services = bindingConfiguration.getNamespaceServices(namespace);

					if (services == null || CollectionUtils.isEmpty(services.getServices()))
						return true;
				}

			} else {

				NamespaceServices services = bindingConfiguration.getNamespaceServices(namespace);

				if (services == null || CollectionUtils.isEmpty(services.getServices()))
					return true;
			}
		}
		return false;
	}

	public static Map<String, Collection<ServiceEndpointInfo>> checkAndDiscoverServices(ResilienceService resilienceService, Process businessProcess, ServicesInformation servicesInformation) {
		return checkAndDiscoverServices(resilienceService, businessProcess, servicesInformation, null, null);
	}

	public static boolean isServiceMonitoringDisabled(ServiceConfig serviceConfig) {
		boolean localDisabled = BooleanUtils.isTrue(ExecutionContext.getFlagValue(serviceConfig.getExecutionContext(), ExecutionFlags.FLAG_DISABLE_SERVICE_MONITORING));
		return localDisabled;
	}

	public static Map<String, Collection<ServiceEndpointInfo>> checkAndDiscoverServices(ResilienceService resilienceService, Process businessProcess, ServicesInformation servicesInformation, ResilienceInfo resilienceInfo, DiscoveryRetryParams retryParams) {

		ExecutionLogMessage logMessage = new ExecutionLogMessage();
		logMessage.setMessageId(LogConstants.MESSAGE_ID_PROCESS_SERVICE_BINDING);
		logMessage.addAttribute(LogConstants.PROCESS_NAME, businessProcess.getName());

		// Setup services...

		BiMap<String, String> namespaceToClassification = HashBiMap.create();

		Map<String, ServiceConfig> classificationToServiceConfig = new HashMap<String, ServiceConfig>();

		List<ServiceConfig> servicesConfig = new LinkedList<ServiceConfig>(servicesInformation.getServicesConfig());

		ListIterator<ServiceConfig> it = servicesConfig.listIterator();

		while (it.hasNext()) {
			ServiceConfig cfg = it.next();
			if (isServiceMonitoringDisabled(cfg)) {
				it.remove();
			}
		}

		for (ServiceConfig t : servicesConfig) {
			if (namespaceToClassification != null)
				namespaceToClassification.put(t.getNamespace(), t.getClassification());
			if (classificationToServiceConfig != null)
				classificationToServiceConfig.put(t.getClassification(), t);
		}

		Map<String, Collection<ServiceEndpointInfo>> classificationToServices = new HashMap<String, Collection<ServiceEndpointInfo>>();

		for (ServiceConfig cfg : servicesConfig) {

			String classification = cfg.getClassification();

			Collection<ServiceEndpointInfo> services = classificationToServices.get(classification);

			if (services == null) {
				services = new LinkedHashSet<ServiceEndpointInfo>();
				classificationToServices.put(classification, services);
			}

			List<ServiceAssociation> serviceAssociations = cfg.getServiceAssociations();

			if (!CollectionUtils.isEmpty(serviceAssociations)) {

				String namespace = namespaceToClassification.inverse().get(classification);

				for (ServiceAssociation s : serviceAssociations) {
					ServiceEndpointInfo service = getServiceEndpointInfo(classification, namespace, s);
					services.add(service);
				}
			}
		}
		Set<String> classifications = classificationToServices.keySet();

		Map<String, Collection<ServiceEndpointInfo>> mapUnavailableServices = new HashMap<String, Collection<ServiceEndpointInfo>>();

		if (BooleanUtils.isNotTrue(ExecutionContext.getFlagValue(ExecutionFlags.FLAG_DISABLE_SERVICE_CHECK))) {

			long st = System.currentTimeMillis();

			List<Collection<ServiceEndpointInfo>> servicesToCheck = new ArrayList<Collection<ServiceEndpointInfo>>(classifications.size());

			// Check if bound services are available
			for (String serviceClassification : classifications) {
				Collection<ServiceEndpointInfo> servicesBounded = classificationToServices.get(serviceClassification);
				servicesToCheck.add(servicesBounded);
			}

			List<ServiceEndpointInfo> flatServicesToCheck = servicesToCheck.stream().collect(LinkedList::new, List::addAll, List::addAll);

			logger.info(logMessage.clone("Checking if " + flatServicesToCheck.size() + " service(s) are alive"));

			ServicesCheckResult checkResult = resilienceService.checkServicesAvailable(flatServicesToCheck);

			for (Collection<ServiceEndpointInfo> services : servicesToCheck) {
				for (ServiceEndpointInfo s : new ArrayList<ServiceEndpointInfo>(services)) {
					if (!checkResult.isServiceAvailable(s.getServiceKey())) {
						Collection<ServiceEndpointInfo> l = mapUnavailableServices.get(s.getServiceClassification());
						if (l == null) {
							l = new LinkedList<ServiceEndpointInfo>();
							mapUnavailableServices.put(s.getServiceClassification(), l);
						}
						l.add(s);
						services.remove(s);
					}
				}
			}

			if (!mapUnavailableServices.isEmpty()) {

				if (logger.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder();

					AtomicInteger count = new AtomicInteger();

					mapUnavailableServices.values().stream().flatMap(x -> x.stream()).forEach(s -> {
						sb.append("\n\t* " + s.getServiceKey() + "\n\t\t" + s.getServiceEndpointURL());
						count.incrementAndGet();
					});

					logger.debug(logMessage.clone("The following services (" + count + ") are unavailable and removed from services list:" + sb.toString()));
				}
			}

			if (resilienceInfo != null)
				resilienceInfo.setServicesCheckTime(System.currentTimeMillis() - st);

		} else {
			logger.info("Flag DISABLE_SERVICE_CHECK set. Ignoring service availability check");
		}

		if (BooleanUtils.isNotTrue(ExecutionContext.getFlagValue(ExecutionFlags.FLAG_DISABLE_SERVICE_DISCOVERY))) {

			boolean useQoSConstraints = BooleanUtils.isNotTrue(ExecutionContext.getFlagValue(ExecutionFlags.FLAG_IGNORE_QOS_CONSTRAINTS_SERVICE_DISCOVERY));

			long st = System.currentTimeMillis();

			DiscoveryRequest discoveryRequest = null;

			// Checking if is necessary to discover services for tasks
			for (String serviceClassification : classifications) {

				ServiceConfig serviceConfig = classificationToServiceConfig.get(serviceClassification);

				Integer desiredNumberOfServices = getDesiredNumberOfServices(serviceConfig);

				Collection<ServiceEndpointInfo> servicesBound = classificationToServices.get(serviceClassification);

				int numberOfServicesBounded = servicesBound.size();

				// We have less services than desired. Trying discover them
				if (numberOfServicesBounded < desiredNumberOfServices) {

					discoveryRequest = discoveryRequest == null ? prepareDiscoveryRequest(businessProcess, servicesInformation, false) : discoveryRequest;

					int numberOfServicesToDiscover = desiredNumberOfServices - numberOfServicesBounded;

					String serviceProviderName = null;

					// TODO: improve...
					if (numberOfServicesBounded > 0) {
						serviceProviderName = servicesBound.iterator().next().getServiceProviderName();
					}

					DiscoveryRequestItem item = getDiscoveryRequestItem(serviceConfig, serviceProviderName, servicesBound, mapUnavailableServices, useQoSConstraints, numberOfServicesToDiscover);

					discoveryRequest.getItens().add(item);

				}
			}

			if (discoveryRequest == null) {
				return classificationToServices;
			}

			DiscoveryService discoveryService = WebServiceLocator.locateService(WebServiceLocator.DISCOVERY_SERVICE_UDDI_SERVICE_KEY, DiscoveryService.class);

			if (discoveryService == null) {
				throw new RuntimeException("DiscoveryService unvailable");
			}

			boolean stop = false;

			int retryAttempt = 0;

			next_discovery: while (!stop) {

				DiscoveryResult discoveryResult = null;

				try {

					discoveryResult = discoveryService.discover(discoveryRequest);

					StringBuilder logSb = null;

					for (Entry<String, List<DiscoveredService>> e : discoveryResult.groupByServiceClassification().entrySet()) {

						String serviceClassification = e.getKey();

						Collection<ServiceEndpointInfo> services = classificationToServices.get(serviceClassification);

						String serviceNamespace = namespaceToClassification.inverse().get(serviceClassification);

						List<DiscoveredService> discoveredServices = e.getValue();

						for (DiscoveredService s : discoveredServices) {
							services.add(new ServiceEndpointInfo(s.getServiceProvider().getIdentification(), serviceClassification, serviceNamespace, s.getServiceKey(), s.getBindingTemplateKey(), s.getServiceEndpointURL(), s.getUtility(), s.getServiceProtocolConverter()));
						}

						// Sorting services by utility
						Collection<ServiceEndpointInfo> sortedServices = new TreeSet<ServiceEndpointInfo>(new Comparator<ServiceEndpointInfo>() {
							@Override
							public int compare(ServiceEndpointInfo s1, ServiceEndpointInfo s2) {
								if (s1 != null && s2 != null) {

									Double u2 = s2.getServiceUtility() != null ? s2.getServiceUtility() : Long.valueOf(0L);
									Double u1 = s1.getServiceUtility() != null ? s1.getServiceUtility() : Long.valueOf(0L);

									return u2.compareTo(u1);
								} else if (s1 == null)
									return 1;
								else if (s2 == null)
									return -1;
								else
									return 0;
							}
						});
						sortedServices.addAll(services);
						classificationToServices.put(serviceClassification, sortedServices);

						if (logger.isDebugEnabled() && !discoveredServices.isEmpty()) {

							if (logSb == null) {
								logSb = new StringBuilder("The discovery service found " + discoveryResult.getNumberOfMatchingServices() + " matching service(s):");
							}

							logSb.append("\n\t* " + discoveredServices.size() + " service(s) discovered for " + serviceClassification);
							for (DiscoveredService s : discoveredServices) {
								logSb.append("\n\t\t" + s);
							}
						}
					}

					if (logSb != null) {
						logger.debug(logMessage.clone(logSb.toString()));
					}
				} catch (Exception e) {
					logger.error(logMessage.clone(e.getMessage()), e);
				}

				List<DiscoveryRequestItem> retryDiscoveryItems = new LinkedList<DiscoveryRequestItem>();

				for (Entry<String, Collection<ServiceEndpointInfo>> e : classificationToServices.entrySet()) {

					String serviceClassification = e.getKey();

					Collection<ServiceEndpointInfo> services = e.getValue();

					ServiceConfig serviceConfig = classificationToServiceConfig.get(serviceClassification);

					int desiredNumberOfServices = getDesiredNumberOfServices(serviceConfig);

					int n = services.size();

					if (n == 0) {

						if (retryAttempt < (retryParams != null ? retryParams.numberOfRetries : 0)) {

							String serviceProviderName = null;

							Collection<ServiceEndpointInfo> servicesBound = new ArrayList<ServiceEndpointInfo>();

							boolean enableQoSConstraints = retryAttempt == 0; //disable QoS constraints if retry attempt is greater than 1

							DiscoveryRequestItem discoveryItem = getDiscoveryRequestItem(serviceConfig, serviceProviderName, servicesBound, mapUnavailableServices, enableQoSConstraints, desiredNumberOfServices);

							retryDiscoveryItems.add(discoveryItem);

							logger.warn(logMessage.clone("Unable to allocate any service for task with classification " + serviceClassification + ". Trying again (" + (retryAttempt + 1) + "/" + retryParams.numberOfRetries + ") relaxing discovery request:"//
									+ "\n\tdiscoveryItem=" + discoveryItem));

							continue;
						}

						if (logger.isEnabledFor(Level.WARN)) {
							StringBuilder sb = new StringBuilder();

							if (discoveryRequest != null) {
								sb.append("\n\tdiscovery items:");
								for (DiscoveryRequestItem i : discoveryRequest.getItens()) {
									int c = (int) ObjectUtils.defaultIfNull(discoveryResult != null ? discoveryResult.getNumberOfMatchingServicesPerServiceClassification().get(i.getServiceClassification()) : null, 0);
									sb.append("\n\t\t(" + c + ") " + i);
								}
							}

							logger.warn(logMessage.clone("Unable to allocate any service for task with classification " + serviceClassification + sb.toString()));
						}

					} else if (n < desiredNumberOfServices) {
						logger.info(logMessage.clone("Can't allocate desired number of services (" + desiredNumberOfServices + ") for task with classification " + serviceClassification + ". It was only possible to allocate " + n + " of " + desiredNumberOfServices + " service(s)"));
					}
				}

				if (!retryDiscoveryItems.isEmpty()) {

					discoveryRequest.setQoSConstraints(new ArrayList<QoSConstraint>());
					discoveryRequest.getItens().clear();
					discoveryRequest.getItens().addAll(retryDiscoveryItems);

					retryAttempt++;
					continue next_discovery;
				}

				stop = true;

				long discoveryDuration = System.currentTimeMillis() - st;

				if (resilienceInfo != null) {
					resilienceInfo.setDiscoveryTime(discoveryDuration);
				}

				logger.info("Discovery took " + discoveryDuration + " ms for a query with " + (discoveryRequest != null && discoveryRequest.getItens() != null ? discoveryRequest.getItens().size() : 0) + " item(s)");

			}
		} else {
			logger.info("Flag DISABLE_SERVICE_DISCOVERY set. Ignoring service discovery");
		}

		return classificationToServices;

	}

	private static DiscoveryRequestItem getDiscoveryRequestItem(ServiceConfig serviceConfig, String serviceProviderName, Collection<ServiceEndpointInfo> servicesBound, Map<String, Collection<ServiceEndpointInfo>> mapUnavailableServices, boolean useQoSConstraints, int numberOfServicesToDiscover) {

		DiscoveryRequestItem item = new DiscoveryRequestItem(serviceConfig.getClassification(), numberOfServicesToDiscover);

		item.setServiceProvider(serviceProviderName);

		if (useQoSConstraints) {

			if (!CollectionUtils.isEmpty(serviceConfig.getQoSCriterions())) {
				List<QoSConstraint> qoSConstraints = new LinkedList<QoSConstraint>();
				for (QoSCriterion q : serviceConfig.getQoSCriterions()) {
					if (!q.isManaged()) {
						qoSConstraints.add(new QoSConstraint(q.getQoSKey(), QoSValueComparisionType.valueOf(q.getComparisionType()), q.getQoSValue()));
					}
				}
				item.setQoSConstraints(qoSConstraints);
			}

			if (!CollectionUtils.isEmpty(serviceConfig.getManagedQoSCriterions())) {
				List<QoSConstraint> qoSConstraints = new LinkedList<QoSConstraint>();
				for (QoSCriterion q : serviceConfig.getManagedQoSCriterions()) {
					qoSConstraints.add(new QoSConstraint(q.getQoSKey(), QoSValueComparisionType.valueOf(q.getComparisionType()), q.getQoSValue()));
				}
				item.setQoSConstraints(qoSConstraints);
			}
		}

		// // Excluding already bound services and unavailable ones
		Collection<ServiceEndpointInfo> unavailableServices = mapUnavailableServices.get(serviceConfig.getClassification());

		Set<String> excludeServiceEndpointsHostPort = new HashSet<String>();

		for (ServiceEndpointInfo s : servicesBound) {
			item.excludeServiceKey(s.getServiceKey());
		}

		if (unavailableServices != null) {
			for (ServiceEndpointInfo s : unavailableServices) {
				item.excludeServiceKey(s.getServiceKey());
				excludeServiceEndpointsHostPort.add(s.getServiceEndpointHostPort());
			}
		}

		// item.excludeServiceEndpointHostPorts(excludeServiceEndpointsHostPort);

		return item;
	}

	public static ServiceEndpointInfo getServiceEndpointInfo(String classification, String namespace, ServiceAssociation s) {
		return new ServiceEndpointInfo(s.getServiceProviderName(), classification, namespace, s.getServiceKey(), s.getBindingTemplateKey(), s.getServiceEndpoint(), s.getServiceUtility(), s.getServiceProtocolConverter());
	}

	private static int getDesiredNumberOfServices(ServiceConfig serviceConfig) {
		return serviceConfig != null && serviceConfig.getMaxNumberOfServices() != null ? serviceConfig.getMaxNumberOfServices() : 5;
	}

	private static DiscoveryRequest prepareDiscoveryRequest(Process businessProcess, ServicesInformation servicesInformation, boolean useQoSConstraints) {

		DiscoveryRequest discoveryRequest = new DiscoveryRequest();

		if (useQoSConstraints) {

			List<QoSCriterion> qoSCriterions = servicesInformation.getQoSCriterions();

			if (!CollectionUtils.isEmpty(qoSCriterions)) {

				List<QoSConstraint> qoSConstraints = new LinkedList<QoSConstraint>();

				for (QoSCriterion q : qoSCriterions) {
					qoSConstraints.add(new QoSConstraint(q.getQoSKey(), QoSValueComparisionType.valueOf(q.getComparisionType()), q.getQoSValue()));
				}

				discoveryRequest.setQoSConstraints(qoSConstraints);

			}
		}

		discoveryRequest.setCheckServicesAvailability(true);

		if (servicesInformation.getQoSWeights() != null) {
			discoveryRequest.setQoSWeights(servicesInformation.getQoSWeights());
		}

		if (servicesInformation.getGlobalQoSDelta() != null) {
			discoveryRequest.setGlobalQoSDelta(servicesInformation.getGlobalQoSDelta());
		}

		return discoveryRequest;
	}

	private static SOAPMessage buildAliveMessage(String serviceNamespace) throws SOAPException {

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();

		ExecutionContext executionContext = ExecutionContext.get();
		if (executionContext != null) {
			SOAPHeader headers = soapMessage.getSOAPHeader();
			try {
				JAXBSerializerUtil.write(executionContext, headers);
			} catch (Exception e) {
				throw new SOAPException(e);
			}
		}

		SOAPBody body = soapMessage.getSOAPBody();
		body.addBodyElement(new QName(serviceNamespace, "alive"));

		return soapMessage;
	}

	public static class DiscoveryRetryParams {

		int numberOfRetries = 1;

		long retrySleep = 0L;

		public DiscoveryRetryParams(int numberOfRetries, long retrySleep) {
			this.numberOfRetries = numberOfRetries;
			this.retrySleep = retrySleep;
		}

	}

}
