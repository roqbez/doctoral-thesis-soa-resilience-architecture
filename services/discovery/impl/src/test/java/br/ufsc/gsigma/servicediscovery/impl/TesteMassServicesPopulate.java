package br.ufsc.gsigma.servicediscovery.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infinispan.Cache;
import org.uddi.api_v3.BusinessService;

import br.ufsc.gsigma.infrastructure.ws.uddi.UBLUddiUtil;
import br.ufsc.gsigma.servicediscovery.indexing.IndexedService;
import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.ServiceProvider;

public class TesteMassServicesPopulate {

	public static void main(String[] args) {

		int numberOfProviders = 3000;

		DiscoveryServiceImpl discoveryService = DiscoveryServiceImpl.INSTANCE;

		Map<String, Integer> mapServiceKeyPrefixCount = new HashMap<String, Integer>();

		Cache<String, DiscoveredService> servicesCache = discoveryService.getServicesCache();

		int w = 0;

		for (int i = 1; i <= 5; i++) {

			String uddiInquiryEndpoint = "http://localhost:" + (8079 - i) + "/services/inquiry?wsdl";

			for (int z = 1; z <= numberOfProviders; z++) {

				ServiceProvider serviceProvider = new ServiceProvider("publisher" + z, "publisher" + z);

				servicesCache.startBatch();

				final String nodeName = "node" + i;

				List<BusinessService> services = UBLUddiUtil.getRandomBusinessServices(null, "uddi:publisher" + z, nodeName, mapServiceKeyPrefixCount, null);

				for (BusinessService b : services) {

					DiscoveredService discoveredService = discoveryService.getDiscoveredService(uddiInquiryEndpoint, b);
					discoveredService.setServiceProvider(serviceProvider);

					servicesCache.put(discoveredService.getServiceKey(), new IndexedService(discoveredService));

					if (++w % 3600 == 0)
						System.out.println("Indexed " + w + " services");

				}

				servicesCache.endBatch(true);

			}
		}

		System.out.println("Indexed " + w + " services");
	}
}
