package br.ufsc.gsigma.infrastructure.ws;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;

public class WebServiceLocator {

	private static final Logger logger = LoggerFactory.getLogger(WebServiceLocator.class);

	public static final String CATALOG_SERVICE_UDDI_SERVICE_KEY = "uddi:gsigma.ufsc.br:services-catalog";

	public static final String CATALOG_PERSISTENCE_SERVICE_UDDI_SERVICE_KEY = "uddi:gsigma.ufsc.br:services-catalogpersistence";

	public static final String CATALOG_SPECIFICATION_SERVICE_UDDI_SERVICE_KEY = "uddi:gsigma.ufsc.br:services-catalogspecification";

	public static final String BPEL_EXPORT_SERVICE_UDDI_SERVICE_KEY = "uddi:gsigma.ufsc.br:services-bpelexporterservice";

	public static final String DISCOVERY_SERVICE_UDDI_SERVICE_KEY = "uddi:gsigma.ufsc.br:services-discoveryservice";

	public static final String UDDI_FEDERATION_UDDI_SERVICE_KEY = "uddi:gsigma.ufsc.br:services-uddifederation";

	public static final String BPEL_PROCESS_EXECUTION_SERVICE_KEY = "uddi:gsigma.ufsc.br:services-bpelprocessexecutionservice";

	public static final String BINDING_SERVICE_KEY = "uddi:gsigma.ufsc.br:services-bindingservice";

	public static final String DEPLOYMENT_SERVICE_KEY = "uddi:gsigma.ufsc.br:services-deploymentservice";

	public static final String RESILIENCE_SERVICE_KEY = "uddi:gsigma.ufsc.br:services-resilienceservice";

	private static final Map<String, String> serviceEndpoints = new HashMap<String, String>();

	private static final Map<String, Object> serviceCache = new HashMap<String, Object>();

	@SuppressWarnings("unchecked")
	public static <T> T locateService(String serviceKey, Class<T> clazz) {

		T service = (T) serviceCache.get(serviceKey);

		if (service != null)
			return service;

		String endpoint = serviceEndpoints.get(serviceKey);

		if (endpoint == null) {
			synchronized (clazz) {
				service = (T) serviceCache.get(serviceKey);

				if (service == null) {
					endpoint = serviceEndpoints.get(serviceKey);
					if (endpoint == null) {
						Set<String> serviceKeys = new HashSet<String>();
						serviceKeys.add(serviceKey);
						try {
							Map<String, String> m = UddiLocator.findServices(serviceKeys);
							endpoint = m.get(serviceKey);
						} catch (Throwable e) {
							logger.error(e.getMessage(), e);
						}

						if (endpoint != null)
							serviceEndpoints.put(serviceKey, endpoint);
					}

					if (endpoint != null) {
						service = ServiceClient.getClient(clazz, endpoint, CxfUtil.getClientFeatures());
						serviceCache.put(serviceKey, service);
					}
				}
			}
		}

		return service;
	}

}
