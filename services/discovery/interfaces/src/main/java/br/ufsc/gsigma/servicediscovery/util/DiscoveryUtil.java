package br.ufsc.gsigma.servicediscovery.util;

import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;

public abstract class DiscoveryUtil {

	private static final String UBL_CLASSIFICATION_PREFIX = "ubl/";

	private static final String UBL_NS_PREFIX = "http://ubl.oasis.services/";

	public static ServiceEndpointInfo toServiceEndpointInfo(DiscoveredService discoveredService) {

		String serviceKey = discoveredService.getServiceKey();
		String serviceClassification = discoveredService.getServiceClassification();
		String serviceEndpointURL = discoveredService.getServiceEndpointURL();

		String bindingTemplateKey = discoveredService.getBindingTemplateKey();
		String serviceProtocolConverter = discoveredService.getServiceProtocolConverter();
		Double utility = discoveredService.getUtility();

		return toServiceEndpointInfo(serviceKey, serviceClassification, serviceEndpointURL, bindingTemplateKey, serviceProtocolConverter, utility);
	}

	public static ServiceEndpointInfo toServiceEndpointInfo(String serviceKey, String serviceClassification, String serviceEndpointURL) {
		return toServiceEndpointInfo(serviceKey, serviceClassification, serviceEndpointURL, null, null, null);
	}

	public static ServiceEndpointInfo toServiceEndpointInfo(String serviceKey, String serviceClassification, String serviceEndpointURL, String bindingTemplateKey, String serviceProtocolConverter, Double utility) {

		ServiceEndpointInfo result = new ServiceEndpointInfo();

		result.setServiceKey(serviceKey);
		result.setBindingTemplateKey(bindingTemplateKey);
		result.setServiceEndpointURL(serviceEndpointURL);
		result.setServiceClassification(serviceClassification);
		result.setServiceUtility(utility);
		result.setServiceProtocolConverter(serviceProtocolConverter);

		// TODO: handle namespace properly
		String namespace = serviceClassificationToNamespace(serviceClassification);
		result.setServiceNamespace(namespace);

		return result;
	}

	public static String serviceClassificationToNamespace(String serviceClassification) {
		return UBL_NS_PREFIX + serviceClassification.substring(UBL_CLASSIFICATION_PREFIX.length());
	}

}
