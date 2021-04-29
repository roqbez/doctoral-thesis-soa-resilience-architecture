package br.ufsc.gsigma.binding.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import br.ufsc.gsigma.catalog.services.model.ServiceAssociation;
import br.ufsc.gsigma.catalog.services.model.ServiceConfig;
import br.ufsc.gsigma.catalog.services.model.ServicesInformation;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;

public abstract class ServiceEndpointUtil {

	private static final String UBL_CLASSIFICATION_PREFIX = "ubl/";

	private static final String UBL_NS_PREFIX = "http://ubl.oasis.services/";

	public static String namespaceToServiceClassification(String namespace) {
		return UBL_CLASSIFICATION_PREFIX + namespace.substring(UBL_NS_PREFIX.length());
	}

	public static Map<String, Collection<ServiceEndpointInfo>> toMapServiceEndpointInfo(ServicesInformation servicesInformation) {

		BiMap<String, String> namespaceToClassification = HashBiMap.create();

		Map<String, ServiceConfig> classificationToServiceConfig = new HashMap<String, ServiceConfig>();

		for (ServiceConfig t : servicesInformation.getServicesConfig()) {
			if (namespaceToClassification != null)
				namespaceToClassification.put(t.getNamespace(), t.getClassification());
			if (classificationToServiceConfig != null)
				classificationToServiceConfig.put(t.getClassification(), t);
		}

		Map<String, Collection<ServiceEndpointInfo>> classificationToServices = new HashMap<String, Collection<ServiceEndpointInfo>>();

		for (ServiceConfig cfg : servicesInformation.getServicesConfig()) {

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
					ServiceEndpointInfo service = new ServiceEndpointInfo(s.getServiceProviderName(), classification, namespace, s.getServiceKey(), s.getBindingTemplateKey(), s.getServiceEndpoint(), s.getServiceUtility(),
							s.getServiceProtocolConverter());
					services.add(service);
				}
			}
		}

		return classificationToServices;
	}
}
