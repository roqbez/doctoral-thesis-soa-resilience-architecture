package br.ufsc.gsigma.services.resilience.support;

import javax.annotation.PostConstruct;

import org.infinispan.Cache;
import org.infinispan.multimap.api.embedded.MultimapCache;
import org.infinispan.transaction.LockingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InfinispanCaches {

	@Autowired
	private InfinispanSupport infinispanSupport;

	private Cache<String, SOAApplication> soaApplications;

	private Cache<String, SOAApplicationInstance> soaApplicationInstances;

	private Cache<String, SOAService> soaServices;

	private Cache<String, Integer> analysisCache;

	private Cache<String, PlanningAction> planningCache;

	private MultimapCache<String, String> applicationToServicesMultimap;

	private MultimapCache<String, String> serviceToApplicationsMultimap;

	private static InfinispanCaches INSTANCE;

	@PostConstruct
	public void setup() throws Exception {

		this.soaApplications = infinispanSupport.getCache("soa-application");

		this.soaApplicationInstances = infinispanSupport.getCache("soa-application-instance");

		this.soaServices = infinispanSupport.getCache("soa-service");

		this.analysisCache = infinispanSupport.getCache("resilience-analysis", LockingMode.PESSIMISTIC);

		this.planningCache = infinispanSupport.getCache("resilience-planning", LockingMode.PESSIMISTIC);

		this.applicationToServicesMultimap = infinispanSupport.getMultimap("soa-application-services", LockingMode.OPTIMISTIC);

		this.serviceToApplicationsMultimap = infinispanSupport.getMultimap("soa-service-applications", LockingMode.OPTIMISTIC);

		INSTANCE = this;
	}

	public static InfinispanCaches getInstance() {
		return INSTANCE;
	}

	public Cache<String, SOAApplication> getSOAApplications() {
		return soaApplications;
	}

	public Cache<String, SOAApplicationInstance> getSOAApplicationInstances() {
		return soaApplicationInstances;
	}

	public Cache<String, SOAService> getSOAServices() {
		return soaServices;
	}

	public Cache<String, Integer> getAnalysisCache() {
		return analysisCache;
	}

	public Cache<String, PlanningAction> getPlanningCache() {
		return planningCache;
	}

	public MultimapCache<String, String> getApplicationToServicesMultimap() {
		return applicationToServicesMultimap;
	}

	public MultimapCache<String, String> getServiceToApplicationsMultimap() {
		return serviceToApplicationsMultimap;
	}

}
