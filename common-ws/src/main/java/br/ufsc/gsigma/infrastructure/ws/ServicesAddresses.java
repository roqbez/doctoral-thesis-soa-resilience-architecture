package br.ufsc.gsigma.infrastructure.ws;

import java.lang.reflect.Field;

public abstract class ServicesAddresses {

	// Defaults

	public static final String BASE_DOMAIN = "d-201603244.ufsc.br";

	public static final String ALL_HOSTS = "0.0.0.0";

	public static final int DEFAULT_HTTP_PORT = 8080;

	public static final String DEFAULT_DOCKER_HTTP_HOST = ALL_HOSTS;

	public static final int DEFAULT_DOCKER_HTTP_PORT = DEFAULT_HTTP_PORT;

	public static final int DEFAULT_DOCKER_DEBUG_PORT = 18000;

	public static final int DEFAULT_DOCKER_JMX_PORT = 28000;

	public static final int DEFAULT_PUBLIC_DEBUG_PORT_OFFSET = 40000;

	public static final int DEFAULT_PUBLIC_JMX_PORT_OFFSET = 30000;

	// Catalog Service
	public static final String CATALOG_SERVICE_HOSTNAME = "catalogservice" + "." + BASE_DOMAIN;
	public static final int CATALOG_SERVICE_PORT = 8081;
	public static final int CATALOG_SERVICE_DEBUG_PORT = DEFAULT_PUBLIC_DEBUG_PORT_OFFSET + CATALOG_SERVICE_PORT;
	public static final int CATALOG_SERVICE_JMX_PORT = DEFAULT_PUBLIC_JMX_PORT_OFFSET + CATALOG_SERVICE_PORT;

	// Catalog Service Specifications
	public static final String CATALOG_SPECIFICATION_SERVICE_HOSTNAME = "catalogspecification" + "." + BASE_DOMAIN;
	public static final int CATALOG_SPECIFICATION_SERVICE_PORT = 8085;
	public static final int CATALOG_SPECIFICATION_SERVICE_DEBUG_PORT = DEFAULT_PUBLIC_DEBUG_PORT_OFFSET + CATALOG_SPECIFICATION_SERVICE_PORT;
	public static final int CATALOG_SPECIFICATION_SERVICE_JMX_PORT = DEFAULT_PUBLIC_JMX_PORT_OFFSET + CATALOG_SPECIFICATION_SERVICE_PORT;

	// Catalog Service Persistence
	public static final String CATALOG_PERSISTENCE_SERVICE_HOSTNAME = "catalogpersistence" + "." + BASE_DOMAIN;
	public static final int CATALOG_PERSISTENCE_SERVICE_PORT = 8084;
	public static final int CATALOG_PERSISTENCE_SERVICE_DEBUG_PORT = DEFAULT_PUBLIC_DEBUG_PORT_OFFSET + CATALOG_PERSISTENCE_SERVICE_PORT;
	public static final int CATALOG_PERSISTENCE_SERVICE_JMX_PORT = DEFAULT_PUBLIC_JMX_PORT_OFFSET + CATALOG_PERSISTENCE_SERVICE_PORT;

	// Service Registry UDDI
	public static final String SERVICE_REGISTRY_UDDI_HOSTNAME = "serviceregistry" + "." + BASE_DOMAIN;
	public static final int SERVICE_REGISTRY_UDDI_PORT = 8000;
	public static final int SERVICE_REGISTRY_UDDI_DEBUG_PORT = DEFAULT_PUBLIC_DEBUG_PORT_OFFSET + SERVICE_REGISTRY_UDDI_PORT;
	public static final int SERVICE_REGISTRY_UDDI_JMX_PORT = DEFAULT_PUBLIC_JMX_PORT_OFFSET + SERVICE_REGISTRY_UDDI_PORT;

	// Service Federation UDDI
	public static final String SERVICE_FEDERATION_UDDI_HOSTNAME = "servicefederation" + "." + BASE_DOMAIN;
	public static final int SERVICE_FEDERATION_UDDI_PORT = 8079;
	public static final int SERVICE_FEDERATION_UDDI_DEBUG_PORT = DEFAULT_PUBLIC_DEBUG_PORT_OFFSET + SERVICE_FEDERATION_UDDI_PORT;
	public static final int SERVICE_FEDERATION_UDDI_JMX_PORT = DEFAULT_PUBLIC_JMX_PORT_OFFSET + SERVICE_FEDERATION_UDDI_PORT;

	// UBL Services
	public static final String UBL_SERVICES_HOSTNAME = "ublservices" + "." + BASE_DOMAIN;
	public static final int UBL_SERVICES_PORT = 11000;
	public static final int UBL_SERVICES_DEBUG_PORT = DEFAULT_PUBLIC_DEBUG_PORT_OFFSET + UBL_SERVICES_PORT;
	public static final int UBL_SERVICES_JMX_PORT = DEFAULT_PUBLIC_JMX_PORT_OFFSET + UBL_SERVICES_PORT;

	public static final String ADHOC_SERVICES_SUBDOMAIN = "adhocservices";
	public static final String ADHOC_SERVICES_HOSTNAME = ADHOC_SERVICES_SUBDOMAIN + "." + BASE_DOMAIN;
	public static final int ADHOC_SERVICES_INITIAL_PORT = 13000;

	// DiscoveryService
	public static final String DISCOVERY_SERVICE_HOSTNAME = "discoveryservice" + "." + BASE_DOMAIN;
	public static final int DISCOVERY_SERVICE_PORT = 8070;
	public static final int DISCOVERY_SERVICE_DEBUG_PORT = DEFAULT_PUBLIC_DEBUG_PORT_OFFSET + DISCOVERY_SERVICE_PORT;
	public static final int DISCOVERY_SERVICE_JMX_PORT = DEFAULT_PUBLIC_JMX_PORT_OFFSET + DISCOVERY_SERVICE_PORT;

	// Execution BPEL
	public static final String EXECUTION_SERVICE_HOSTNAME = "executionservice" + "." + BASE_DOMAIN;
	public static final int EXECUTION_SERVICE_PORT = 7000;
	public static final int EXECUTION_SERVICE_DEBUG_PORT = DEFAULT_PUBLIC_DEBUG_PORT_OFFSET + EXECUTION_SERVICE_PORT;
	public static final int EXECUTION_SERVICE_JMX_PORT = DEFAULT_PUBLIC_JMX_PORT_OFFSET + EXECUTION_SERVICE_PORT;

	// BPEL Exporter
	public static final String BPEL_EXPORTER_SERVICE_HOSTNAME = "bpelexporter" + "." + BASE_DOMAIN;
	public static final int BPEL_EXPORTER_SERVICE_PORT = 8083;
	public static final int BPEL_EXPORTER_SERVICE_DEBUG_PORT = DEFAULT_PUBLIC_DEBUG_PORT_OFFSET + BPEL_EXPORTER_SERVICE_PORT;
	public static final int BPEL_EXPORTER_SERVICE_JMX_PORT = DEFAULT_PUBLIC_JMX_PORT_OFFSET + BPEL_EXPORTER_SERVICE_PORT;

	// BindingService
	public static final String BINDING_SERVICE_HOSTNAME = "bindingservice" + "." + BASE_DOMAIN;
	public static final int BINDING_SERVICE_PORT = 9000;
	public static final int BINDING_SERVICE_DEBUG_PORT = DEFAULT_PUBLIC_DEBUG_PORT_OFFSET + BINDING_SERVICE_PORT;
	public static final int BINDING_SERVICE_JMX_PORT = DEFAULT_PUBLIC_JMX_PORT_OFFSET + BINDING_SERVICE_PORT;

	// DeploymentService
	public static final String DEPLOYMENT_SERVICE_HOSTNAME = "deploymentservice" + "." + BASE_DOMAIN;
	public static final int DEPLOYMENT_SERVICE_PORT = 6000;
	public static final int DEPLOYMENT_SERVICE_DEBUG_PORT = DEFAULT_PUBLIC_DEBUG_PORT_OFFSET + DEPLOYMENT_SERVICE_PORT;
	public static final int DEPLOYMENT_SERVICE_JMX_PORT = DEFAULT_PUBLIC_JMX_PORT_OFFSET + DEPLOYMENT_SERVICE_PORT;

	// ResilienceService
	public static final String RESILIENCE_SERVICE_HOSTNAME = "resilienceservice" + "." + BASE_DOMAIN;
	public static final int RESILIENCE_SERVICE_PORT = 6500;
	public static final int RESILIENCE_SERVICE_DEBUG_PORT = DEFAULT_PUBLIC_DEBUG_PORT_OFFSET + RESILIENCE_SERVICE_PORT;
	public static final int RESILIENCE_SERVICE_JMX_PORT = DEFAULT_PUBLIC_JMX_PORT_OFFSET + RESILIENCE_SERVICE_PORT;

	// JmsBroker
	public static final String JMS_BROKER_HOSTNAME = "jmsbroker" + "." + BASE_DOMAIN;
	public static final int JMS_BROKER_PORT = 61616;
	public static final int JMS_BROKER_DEBUG_PORT = 10 + JMS_BROKER_PORT;
	public static final int JMS_BROKER_JMX_PORT = 20 + JMS_BROKER_PORT;

	public static void main(String[] args) throws Exception {

		for (Field f : ServicesAddresses.class.getFields()) {
			System.out.println(f.getName() + "=" + f.get(null));
		}
	}

}
