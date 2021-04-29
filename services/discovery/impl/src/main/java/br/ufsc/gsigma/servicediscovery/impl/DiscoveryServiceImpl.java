package br.ufsc.gsigma.servicediscovery.impl;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.xml.ws.WebServiceException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.juddi.v3.client.UDDIConstants;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.Index;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.persistence.leveldb.configuration.LevelDBStoreConfigurationBuilder;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.FetchOptions;
import org.infinispan.query.FetchOptions.FetchMode;
import org.infinispan.query.ResultIterator;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uddi.api_v3.BindingTemplate;
import org.uddi.api_v3.BusinessService;
import org.uddi.api_v3.CategoryBag;
import org.uddi.api_v3.FindQualifiers;
import org.uddi.api_v3.FindService;
import org.uddi.api_v3.FindTModel;
import org.uddi.api_v3.GetOperationalInfo;
import org.uddi.api_v3.GetServiceDetail;
import org.uddi.api_v3.GetTModelDetail;
import org.uddi.api_v3.KeyedReference;
import org.uddi.api_v3.KeyedReferenceGroup;
import org.uddi.api_v3.OperationalInfo;
import org.uddi.api_v3.OperationalInfos;
import org.uddi.api_v3.ServiceDetail;
import org.uddi.api_v3.ServiceList;
import org.uddi.api_v3.TModel;
import org.uddi.api_v3.TModelBag;
import org.uddi.api_v3.TModelDetail;
import org.uddi.api_v3.TModelInfo;
import org.uddi.api_v3.TModelInstanceInfo;
import org.uddi.api_v3.TModelList;
import org.uddi.v3_service.UDDIInquiryPortType;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import br.ufsc.gsigma.catalog.services.model.Participant;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.Task;
import br.ufsc.gsigma.catalog.services.model.TaskParticipant;
import br.ufsc.gsigma.infrastructure.util.ObjectHolder;
import br.ufsc.gsigma.infrastructure.util.Util;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UBLUddiUtil;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiKeys;
import br.ufsc.gsigma.servicediscovery.indexing.DiscoveryCacheQuery;
import br.ufsc.gsigma.servicediscovery.indexing.IndexedService;
import br.ufsc.gsigma.servicediscovery.indexing.QoSCapableServiceQuery;
import br.ufsc.gsigma.servicediscovery.indexing.ServicesCheckQuery;
import br.ufsc.gsigma.servicediscovery.indexing.ServicesDirectoryBasedIndexManager;
import br.ufsc.gsigma.servicediscovery.interfaces.DiscoveryAdminService;
import br.ufsc.gsigma.servicediscovery.interfaces.DiscoveryService;
import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryRequest;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryRequestItem;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryResult;
import br.ufsc.gsigma.servicediscovery.model.ProcessQoSInfo;
import br.ufsc.gsigma.servicediscovery.model.ProcessQoSThresholds;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute.QoSValueAggregationType;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute.QoSValueUtilityDirection;
import br.ufsc.gsigma.servicediscovery.model.QoSConstraint;
import br.ufsc.gsigma.servicediscovery.model.QoSInformation;
import br.ufsc.gsigma.servicediscovery.model.QoSItem;
import br.ufsc.gsigma.servicediscovery.model.QoSLevel;
import br.ufsc.gsigma.servicediscovery.model.QoSLevelsRequest;
import br.ufsc.gsigma.servicediscovery.model.QoSLevelsResponse;
import br.ufsc.gsigma.servicediscovery.model.QoSThreshold;
import br.ufsc.gsigma.servicediscovery.model.QoSValueComparisionType;
import br.ufsc.gsigma.servicediscovery.model.ServiceInfo;
import br.ufsc.gsigma.servicediscovery.model.ServiceProvider;
import br.ufsc.gsigma.servicediscovery.model.ServiceQoSInformation;
import br.ufsc.gsigma.servicediscovery.model.ServiceQoSInformationItem;
import br.ufsc.gsigma.servicediscovery.model.ServicesComposition;
import br.ufsc.gsigma.servicediscovery.model.ServicesQoSInformation;
import br.ufsc.gsigma.servicediscovery.support.CompositionParticipant;
import br.ufsc.gsigma.servicediscovery.support.CompositionParticipantProvider;
import br.ufsc.gsigma.servicediscovery.support.CompositionParticipantTask;
import br.ufsc.gsigma.servicediscovery.support.CompositionParticipants;
import br.ufsc.gsigma.servicediscovery.support.ProcessQoSAggregator;
import br.ufsc.gsigma.servicediscovery.support.ProcessStructureParser;
import br.ufsc.gsigma.servicediscovery.support.QoSLevels;
import br.ufsc.gsigma.servicediscovery.support.QoSMinMax;
import br.ufsc.gsigma.servicediscovery.support.ServiceClassificationInfo;
import br.ufsc.gsigma.servicediscovery.support.ServiceQoSUtil;
import br.ufsc.gsigma.servicediscovery.support.struct.SequentialStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.TaskStructure;
import br.ufsc.gsigma.servicediscovery.support.struct.VirtualTaskStructure;
import br.ufsc.gsigma.servicediscovery.util.DiscoveryUtil;
import br.ufsc.gsigma.services.resilience.interfaces.ResilienceService;
import br.ufsc.gsigma.services.resilience.locator.ResilienceServiceLocator;
import br.ufsc.gsigma.services.resilience.model.ServicesCheckResult;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.SortedList;

public class DiscoveryServiceImpl implements DiscoveryService, DiscoveryAdminService {

	private static final Logger logger = LoggerFactory.getLogger(DiscoveryServiceImpl.class);

	private static final String CACHE_DIR = "db/discovery/cache/db";

	private static final String INDEX_DIR = "db/discovery/cache/indexes";

	private static final String PARAMETERS_CACHE_KEY_QOS_ITEMS = "qosItems";

	private static final String PARAMETERS_CACHE_KEY_TMODEL_KEY_TO_QOS_ATTRIBUTE = "tModelKeyToQoSAttribute";

	private static final String PARAMETERS_CACHE_KEY_SERVICE_CLASSIFICATION_TO_TMODEL = "serviceClassificationToTModel";

	private static UDDIInquiryPortType mainInquiryService = null;

	private List<QoSItem> qoSItems = new LinkedList<QoSItem>();

	private List<QoSItem> enabledQoSItems = new LinkedList<QoSItem>();

	private List<String> uddiRepositoriesEndpoints;

	private Set<String> listDeadUDDI = Collections.synchronizedSet(new HashSet<String>());

	private ExecutorService pool = Executors.newCachedThreadPool();

	private EmbeddedCacheManager cacheManager;

	private Cache<String, Object> parametersCache;
	private Cache<String, DiscoveredService> servicesCache;
	private Cache<String, Object> cacheServiceClassification;
	private Cache<String, Object> cacheQoSLevels;
	private Cache<String, ServiceProvider> cacheServiceProviders;

	private Map<String, UDDIInquiryPortType> mapUDDIFederationInquiryService = new HashMap<String, UDDIInquiryPortType>();

	private Map<String, QoSAttribute> mapTModelKeyToQoSAttribute = new LinkedHashMap<String, QoSAttribute>();

	private BiMap<String, String> mapServiceClassificationToTModel = HashBiMap.create();

	private String[] servicesQueryProjections;

	public static final DiscoveryServiceImpl INSTANCE = new DiscoveryServiceImpl();

	private DiscoveryServiceImpl() {
	}

	private EmbeddedCacheManager getCacheManager() {

		if (cacheManager == null) {
			synchronized (this) {
				if (cacheManager == null) {
					cacheManager = new DefaultCacheManager();
				}
			}
		}
		return cacheManager;
	}

	Cache<String, DiscoveredService> getServicesCache() {

		if (servicesCache == null) {
			synchronized (this) {
				if (servicesCache == null) {

					EmbeddedCacheManager cacheManager = getCacheManager();

					ConfigurationBuilder cfg = new ConfigurationBuilder();

					// cfg.memory().evictionType(EvictionType.COUNT).size(500000);

					cfg.persistence().passivation(false) //
							.addStore(LevelDBStoreConfigurationBuilder.class) //
							.location(new File(CACHE_DIR, "data").getAbsolutePath() + "/")//
							.expiredLocation(new File(CACHE_DIR, "expired").getAbsolutePath() + "/");

					cfg.indexing().index(Index.ALL).addProperty("hibernate.search.default.directory_provider", "filesystem").addProperty("hibernate.search." + IndexedService.class.getName() + ".indexmanager", ServicesDirectoryBasedIndexManager.class.getName())//
							.addProperty("hibernate.search.default.indexBase", INDEX_DIR);

					cfg.invocationBatching().enable();

					cacheManager.defineConfiguration("services-cache", cfg.build());

					servicesCache = cacheManager.getCache("services-cache");
				}
			}
		}
		return servicesCache;
	}

	Cache<String, Object> getParametersCache() {

		if (parametersCache == null) {
			synchronized (this) {
				if (parametersCache == null) {

					EmbeddedCacheManager cacheManager = getCacheManager();

					ConfigurationBuilder cfg = new ConfigurationBuilder();

					String location = new File(CACHE_DIR, "data").getAbsolutePath();
					String expiredLocation = new File(CACHE_DIR, "expired").getAbsolutePath();

					cfg.persistence().passivation(false) //
							.addStore(LevelDBStoreConfigurationBuilder.class) //
							.location(location + "/")//
							.expiredLocation(expiredLocation + "/");

					logger.info("Creating Infinispan cache 'parametersCache' at " + location);

					cfg.invocationBatching().enable();

					cacheManager.defineConfiguration("parameters-cache", cfg.build());

					parametersCache = cacheManager.getCache("parameters-cache");
				}
			}
		}
		return parametersCache;
	}

	private Cache<String, Object> getCacheServiceClassification() {

		if (cacheServiceClassification == null) {
			synchronized (this) {
				if (cacheServiceClassification == null) {

					EmbeddedCacheManager cacheManager = getCacheManager();

					ConfigurationBuilder cfg = new ConfigurationBuilder();

					String location = new File(CACHE_DIR, "data").getAbsolutePath();
					String expiredLocation = new File(CACHE_DIR, "expired").getAbsolutePath();

					cfg.persistence().passivation(false) //
							.addStore(LevelDBStoreConfigurationBuilder.class) //
							.location(location + "/")//
							.expiredLocation(expiredLocation + "/");

					logger.info("Creating Infinispan cache 'cacheServiceClassification' at " + location);

					cfg.invocationBatching().enable();

					cacheManager.defineConfiguration("serviceclassification-cache", cfg.build());

					cacheServiceClassification = cacheManager.getCache("serviceclassification-cache");
				}
			}
		}
		return cacheServiceClassification;
	}

	private Cache<String, Object> getCacheQoSLevels() {

		if (cacheQoSLevels == null) {
			synchronized (this) {
				if (cacheQoSLevels == null) {

					EmbeddedCacheManager cacheManager = getCacheManager();

					ConfigurationBuilder cfg = new ConfigurationBuilder();

					String location = new File(CACHE_DIR, "data").getAbsolutePath();
					String expiredLocation = new File(CACHE_DIR, "expired").getAbsolutePath();

					cfg.persistence().passivation(false) //
							.addStore(LevelDBStoreConfigurationBuilder.class) //
							.location(location + "/")//
							.expiredLocation(expiredLocation + "/");

					logger.info("Creating Infinispan cache 'cacheQoSLevels' at " + location);

					cfg.invocationBatching().enable();

					cacheManager.defineConfiguration("qoslevels-cache", cfg.build());

					cacheQoSLevels = cacheManager.getCache("qoslevels-cache");
				}
			}
		}
		return cacheQoSLevels;
	}

	private Cache<String, ServiceProvider> getCacheServiceProviders() {

		if (cacheServiceProviders == null) {
			synchronized (this) {
				if (cacheServiceProviders == null) {

					EmbeddedCacheManager cacheManager = getCacheManager();

					ConfigurationBuilder cfg = new ConfigurationBuilder();

					String location = new File(CACHE_DIR, "data").getAbsolutePath();
					String expiredLocation = new File(CACHE_DIR, "expired").getAbsolutePath();

					cfg.persistence().passivation(false) //
							.addStore(LevelDBStoreConfigurationBuilder.class) //
							.location(location + "/")//
							.expiredLocation(expiredLocation + "/");

					logger.info("Creating Infinispan cache 'cacheServiceProviders' at " + location);

					cfg.invocationBatching().enable();

					cacheManager.defineConfiguration("serviceproviders-cache", cfg.build());

					cacheServiceProviders = cacheManager.getCache("serviceproviders-cache");
				}
			}
		}
		return cacheServiceProviders;
	}

	private UDDIInquiryPortType getUDDIFederationInquiryService() {

		if (mainInquiryService == null) {
			synchronized (this) {
				if (mainInquiryService == null)
					mainInquiryService = WebServiceLocator.locateService(WebServiceLocator.UDDI_FEDERATION_UDDI_SERVICE_KEY, UDDIInquiryPortType.class);
			}
		}

		return mainInquiryService;
	}

	@Override
	public boolean populateExampleServices(final int numberOfPublishers, final int numberOfServers, final List<String> servicesClassifications) {

		pool.submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {

				try {

					Cache<String, DiscoveredService> servicesCache = getServicesCache();

					if (!CollectionUtils.isEmpty(servicesClassifications)) {
						for (String serviceClassification : servicesClassifications) {
							try (ResultIterator it = getServicesIterator(serviceClassification)) {
								while (it.hasNext()) {
									IndexedService indexedService = (IndexedService) it.next();
									servicesCache.remove(indexedService.getServiceKey());
								}
							}
						}
					} else {
						servicesCache.clear();
					}

					int w = 1;

					// int i = 1;

					long s = System.currentTimeMillis();

					final int offset = UBLUddiUtil.UBL_SERVICE_URL_PREFIX.length();

					final int offset2 = "http://".length();

					Map<String, Integer> mapServiceKeyPrefixCount = new HashMap<String, Integer>();

					List<String> uddiEndpoints = getUDDIInquiryServiceEndPoints();

					logger.info("Populating services from " + uddiEndpoints.size() + " repositories");

					// for (String uddiInquiryEndpoint : uddiEndpoints) {

					String uddiInquiryEndpoint = uddiEndpoints.get(0);

					for (int z = 1; z <= numberOfPublishers; z++) {

						ServiceProvider serviceProvider = new ServiceProvider("publisher" + z, "publisher" + z);

						servicesCache.startBatch();

						final String nodeName = "nodeX";

						List<BusinessService> services = UBLUddiUtil.getRandomBusinessServices(null, "uddi:publisher" + z, nodeName, mapServiceKeyPrefixCount, servicesClassifications);

						for (BusinessService b : services) {

							for (int k = 1; k <= numberOfServers; k++) {

								DiscoveredService discoveredService = getDiscoveredService(uddiInquiryEndpoint, b);
								discoveredService.setServiceProvider(serviceProvider);

								String hostname = k > 1 ? Util.suffixHost(ServicesAddresses.UBL_SERVICES_HOSTNAME, k) : ServicesAddresses.UBL_SERVICES_HOSTNAME;
								int port = ServicesAddresses.UBL_SERVICES_PORT + (k > 0 ? k - 1 : 0);

								String urlPrefix = UBLUddiUtil.getUBLServiceUrlPrefix(hostname, port);

								String serviceEndpointURL = urlPrefix + discoveredService.getServiceEndpointURL().substring(offset);

								discoveredService.setServiceEndpointURL(serviceEndpointURL);

								int idx = serviceEndpointURL.indexOf('.');

								if (idx != -1) {
									int idx2 = discoveredService.getServiceKey().lastIndexOf('-');
									if (idx2 != -1) {
										String serviceKey = discoveredService.getServiceKey().substring(0, idx2 + 1) + serviceEndpointURL.substring(offset2, idx) + discoveredService.getServiceKey().substring(idx2);
										serviceKey = serviceKey.replace("nodeX-", "node" + (k) + "-");
										discoveredService.setServiceKey(serviceKey);
										discoveredService.setBindingTemplateKey(serviceKey + "-binding");
									}
								}

								DiscoveredService previous = servicesCache.put(discoveredService.getServiceKey(), new IndexedService(discoveredService));

								if (previous != null) {
									logger.warn("Key colision for " + discoveredService.getServiceKey());
								}

								if (w % 3600 == 0)
									logger.info("Indexed " + w + " services");

								w++;
							}
						}

						servicesCache.endBatch(true);

					}
					// i++;
					// }

					long d = System.currentTimeMillis() - s;

					logger.info("Indexed " + (w - 1) + " services in " + d + " ms");

					rebuildServiceClassificationInfo(false);
					rebuildQoSLevels(false);

					return null;

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw e;
				}

			}
		});

		return true;

	}

	@Override
	public boolean rebuildIndex() {

		final Cache<String, DiscoveredService> cache = getServicesCache();

		cache.clear();

		final Collection<String> serviceClassifications = getServiceClassifications();

		final List<String> uddiEndpoints = getUDDIInquiryServiceEndPoints();
		// final List<String> uddiEndpoints = getUDDIInquiryServiceEndPoints().subList(0, 1);

		final ServiceClassificationInfo sci = new ServiceClassificationInfo();

		Callable<Void> job = new Callable<Void>() {

			@Override
			public Void call() throws Exception {

				ExecutorService pool = Executors.newFixedThreadPool(5);

				final AtomicInteger counter = new AtomicInteger();

				for (final String serviceClassification : serviceClassifications) {

					final DiscoveryRequest discoveryRequest = new DiscoveryRequest(serviceClassification);

					for (final String uddiEndpoint : uddiEndpoints) {

						pool.submit(new Callable<Void>() {

							@Override
							public Void call() throws Exception {

								logger.info("Retrieving services of category '" + serviceClassification + "' from UDDI '" + uddiEndpoint + "'");

								List<DiscoveredService> services = searchServicesInUDDI(uddiEndpoint, discoveryRequest, 50);

								int l = services.size();

								logger.info("Preparing to index " + l + " services of category '" + serviceClassification + "' from UDDI '" + uddiEndpoint + "'");

								cache.startBatch();

								for (DiscoveredService s : services) {
									cache.put(s.getServiceKey(), new IndexedService(s));

									sci.collect(s);

									int n = counter.incrementAndGet();

									if (n % 500 == 0)
										logger.info(n + " entries indexed");
								}

								sci.postProcess();

								cache.endBatch(true);

								return null;
							}
						});
					}

					// break;

				}

				pool.shutdown();
				pool.awaitTermination(1, TimeUnit.DAYS);

				getCacheServiceClassification().put(ServiceClassificationInfo.class.getName(), sci);

				logger.info("Indexing completed. " + counter.intValue() + " entries indexed");

				return null;
			}
		};

		pool.submit(job);

		return true;
	}

	@Override
	public boolean rebuildServiceClassificationInfo() throws Exception {
		return rebuildServiceClassificationInfo(true);
	}

	private boolean rebuildServiceClassificationInfo(boolean background) throws Exception {

		Callable<Void> job = new Callable<Void>() {

			@Override
			public Void call() throws Exception {

				try {

					Map<String, ServiceClassificationInfo> m = new HashMap<String, ServiceClassificationInfo>();

					int i = 0;

					for (DiscoveredService s : getServicesCache().values()) {

						ServiceClassificationInfo sci = m.get(s.getServiceClassification());

						if (sci == null) {
							sci = new ServiceClassificationInfo();
							m.put(s.getServiceClassification(), sci);
						}

						sci.collect(s);

						if (++i % 500 == 0)
							logger.info("Service Classification Info collected from " + i + " entries");
					}

					for (ServiceClassificationInfo sci : m.values()) {
						sci.postProcess();
					}

					logger.info("Service Classification Info collected from " + i + " entries");

					getCacheServiceClassification().putAll(m);

					logger.info("Service Classification Info stored in cache");

					return null;

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw e;
				}
			}

		};

		if (background)
			pool.submit(job);
		else
			job.call();

		return true;
	}

	@Override
	public boolean rebuildQoSLevels() throws Exception {
		return rebuildQoSLevels(true);
	}

	private boolean rebuildQoSLevels(boolean background) throws Exception {

		final Collection<String> serviceClassifications = getServiceClassifications();

		final Integer[] numberOfLevels = new Integer[] { 10, 20, 40, 80, 160 };

		final AtomicInteger count = new AtomicInteger();

		final Cache<String, Object> cacheQoSLevels = getCacheQoSLevels();

		final Map<String, QoSAttribute> qoSAttributes = getQoSAttributes();

		final QoSMinMax qoSMinMax = getQoSMinMax(getServiceClassifications(), null);

		Callable<Void> job = new Callable<Void>() {

			@Override
			public Void call() throws Exception {

				try {

					long start = System.currentTimeMillis();

					List<Future<Void>> futures = new ArrayList<Future<Void>>(serviceClassifications.size());

					final ExecutorService pool = Executors.newWorkStealingPool();

					for (final String serviceClassification : serviceClassifications) {

						final ServiceClassificationInfo serviceClassificationInfo = getServiceClassificationInfo(serviceClassification);

						if (serviceClassificationInfo == null)
							throw new IllegalStateException("ServiceClassificationInfo not calculated for '" + serviceClassification + "'. Please run 'rebuildServiceClassificationInfo' operation before");

						futures.add(pool.submit(new Callable<Void>() {

							@Override
							public Void call() throws Exception {

								final QoSLevels qoSLevels = new QoSLevels(serviceClassification);

								try (ResultIterator it = getServicesIterator(serviceClassification)) {
									int c = qoSLevels.calculateQoSLevels(it, qoSAttributes, qoSMinMax, serviceClassificationInfo, numberOfLevels);
									count.addAndGet(c);
								}

								cacheQoSLevels.put(serviceClassification, qoSLevels);

								return null;
							}
						}));

					}

					for (Future<Void> f : futures)
						f.get();

					pool.shutdown();

					long duration = System.currentTimeMillis() - start;

					logger.info("QoS Levels collected from " + count + " entries in " + duration + " ms");

					return null;

				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
					throw new RuntimeException(e);
				}

			}

		};

		if (background)
			pool.submit(job);
		else
			job.call();

		return true;
	}

	private ResultIterator getServicesIterator(String serviceClassification) {
		return getServicesIterator(new String[] { serviceClassification }, null);
	}

	private ResultIterator getServicesIterator(String[] serviceClassifications, Query restrictQuery) {

		final Cache<String, DiscoveredService> cache = getServicesCache();

		final SearchManager searchManager = Search.getSearchManager(cache);

		Query q = null;

		if (serviceClassifications.length == 1) {
			q = new TermQuery(new Term("serviceClassification", serviceClassifications[0]));
		} else {
			org.apache.lucene.search.BooleanQuery.Builder b = new BooleanQuery.Builder();

			for (String serviceClassification : serviceClassifications) {
				b.add(new TermQuery(new Term("serviceClassification", serviceClassification)), Occur.SHOULD);
			}
			q = b.build();
		}

		if (restrictQuery != null) {
			org.apache.lucene.search.BooleanQuery.Builder b1 = new BooleanQuery.Builder();
			b1.add(q, Occur.MUST);
			b1.add(restrictQuery, Occur.MUST);
			q = b1.build();
		}

		if (logger.isInfoEnabled()) {
			logger.info("Returning services iterator for query=" + q);
		}

		CacheQuery cacheQuery = searchManager.getQuery(q, IndexedService.class);

		return cacheQuery.iterator();
	}

	private Set<String> getServiceClassifications() {
		return getMapServiceClassificationToTModel().keySet();
	}

	private String[] getServiceQueryProjections() {

		if (servicesQueryProjections == null) {

			synchronized (this) {

				if (servicesQueryProjections == null) {

					Collection<QoSAttribute> qoSAtts = getQoSAttributesInternal();

					servicesQueryProjections = new String[qoSAtts.size() + 2];

					servicesQueryProjections[0] = "providedId";
					servicesQueryProjections[1] = "serviceClassification";

					int i = 2;

					for (QoSAttribute q : qoSAtts) {
						servicesQueryProjections[i++] = "QoS." + q.getKey();
					}

				}
			}
		}
		return servicesQueryProjections;
	}

	private Map<String, Map<String, NavigableMap<Double, Double>>> getMapServiceQoSUtility(Collection<String> serviceClassifications, Map<String, Double> qoSWeights, Collection<String> qoSAttrs, Map<String, Double> mapServiceClassMaxUtility) throws Exception {

		Map<String, Map<String, NavigableMap<Double, Double>>> result = new HashMap<String, Map<String, NavigableMap<Double, Double>>>();

		DiscoveryCacheQuery query = getDiscoveryQuery(new DiscoveryRequest(new ArrayList<String>(serviceClassifications), qoSWeights), null, false, getServiceQueryProjections());

		FetchOptions LAZY_FETCHMODE = new FetchOptions().fetchMode(FetchMode.LAZY);

		try (ResultIterator it = query.iterator(LAZY_FETCHMODE)) {

			while (it.hasNext()) {

				Object[] s = (Object[]) it.next();

				String id = ((String) s[0]).substring(2);

				String serviceClassification = (String) s[1];

				Double serviceUtility = ((QoSCapableServiceQuery) query.getLuceneQuery()).getDocUtility().get(id);

				Double maxUtility = mapServiceClassMaxUtility.get(serviceClassification);

				if (maxUtility == null) {
					mapServiceClassMaxUtility.put(serviceClassification, serviceUtility);
				} else {
					mapServiceClassMaxUtility.put(serviceClassification, Math.max(serviceUtility, maxUtility));
				}

				Map<String, NavigableMap<Double, Double>> m = result.get(serviceClassification);

				if (m == null) {
					m = new HashMap<String, NavigableMap<Double, Double>>();
					result.put(serviceClassification, m);
				}

				int i = 2;

				for (QoSAttribute q : getQoSAttributesInternal()) {

					String qoSKey = q.getKey();

					double qoSValue = (double) s[i++];

					NavigableMap<Double, Double> qoSMap = m.get(qoSKey);

					if (qoSMap == null) {
						qoSMap = new TreeMap<Double, Double>();
						m.put(qoSKey, qoSMap);
					}

					Double utility = qoSMap.get(qoSValue);

					if (utility == null)
						qoSMap.put(qoSValue, serviceUtility);
					else
						qoSMap.put(qoSValue, Math.max(utility, serviceUtility));

				}
			}
		}
		return result;
	}

	@Override
	public QoSLevelsResponse getQoSLevels(QoSLevelsRequest request) throws Exception {

		Map<String, QoSAttribute> qoSAttributes = getEnabledQoSAttributes();

		if (request.getProcess() != null) {

			ProcessStructureParser parser = new ProcessStructureParser(request.getProcess());

			Map<String, Double> qoSWeights = request.getQoSWeights();

			fillQoSLevelsForProcess(parser, request.getNumbersOfLevels(), qoSAttributes, qoSWeights);

			SequentialStructure executionPath = parser.getSequentialPath();

			ProcessQoSAggregator agg = new ProcessQoSAggregator(qoSAttributes, null);

			VirtualTaskStructure vTask = parser.getVirtualTask(executionPath, agg);

			agg.groupQoSLevels(vTask);

			QoSLevelsResponse response = new QoSLevelsResponse();

			for (TaskStructure t : vTask.getTasks()) {
				response.getQoSLevelsByVirtualTask().put(t.getName(), t.getQoSLevels());
			}

			for (Map<String, NavigableMap<Integer, List<QoSLevel>>> m : response.getQoSLevelsByVirtualTask().values()) {
				for (String qoSKey : new ArrayList<String>(m.keySet())) {
					if (request.getQoSKeys() == null || request.getQoSKeys().isEmpty() || !request.getQoSKeys().contains(qoSKey))
						m.remove(qoSKey);
				}
			}

			return response;
		} else {
			return getQoSLevelsInternal(request, qoSAttributes);
		}

	}

	private QoSLevelsResponse getQoSLevelsInternal(QoSLevelsRequest request, final Map<String, QoSAttribute> qoSAtts) {

		try {

			QoSLevelsResponse result = new QoSLevelsResponse();

			Map<String, Double> mapMaxUtility = new HashMap<String, Double>();

			Map<String, Map<String, NavigableMap<Double, Double>>> mapCategoriesServicesQoSUtility = null;

			boolean isCustomQoSWeights = request.getQoSWeights() != null && !request.getQoSWeights().isEmpty();

			if (isCustomQoSWeights) {

				Set<Double> weights = new HashSet<Double>(qoSAtts.size());

				for (QoSAttribute q : qoSAtts.values()) {
					if (q.getValueUtilityDirection() != null) {
						Double w = request.getQoSWeights().get(q.getKey());
						w = w != null ? w : 1;
						weights.add(w);
					}
				}

				isCustomQoSWeights = weights.size() > 1;
			}

			if (isCustomQoSWeights) {
				mapCategoriesServicesQoSUtility = getMapServiceQoSUtility(request.getServiceClassifications(), request.getQoSWeights(), request.getQoSKeys(), mapMaxUtility);
			}

			List<Future<Void>> futures = new LinkedList<Future<Void>>();

			for (final String serviceClassification : request.getServiceClassifications()) {

				final QoSLevels qoSLevels = getQoSLevels(serviceClassification);

				if (qoSLevels != null) {

					final List<Integer> levelsToCalculate = new LinkedList<Integer>();

					for (Integer numberOfLevels : request.getNumbersOfLevels()) {

						boolean stop = false;

						for (String qoSKey : request.getQoSKeys()) {

							Collection<QoSLevel> levels = qoSLevels.getQoSLevels(qoSKey, numberOfLevels);

							if (levels == null) {
								levelsToCalculate.add(numberOfLevels);
								stop = true;
								break;
							}
						}
						if (stop)
							break;
					}

					if (!levelsToCalculate.isEmpty()) {

						futures.add(pool.submit(new Callable<Void>() {

							@Override
							public Void call() throws Exception {

								synchronized (qoSLevels) {

									logger.info("The service classification '" + serviceClassification + "' doesn't have its QoS attributes partitioned in levels=" + levelsToCalculate + ". Partitioning it now");

									try (ResultIterator services = getServicesIterator(serviceClassification)) {
										final QoSMinMax qoSMinMax = getQoSMinMax(Collections.singleton(serviceClassification), null);
										final ServiceClassificationInfo serviceClassificationInfo = getServiceClassificationInfo(serviceClassification);
										qoSLevels.calculateQoSLevels(services, qoSAtts, qoSMinMax, serviceClassificationInfo, levelsToCalculate.toArray(new Integer[levelsToCalculate.size()]));
									}
									getCacheQoSLevels().put(serviceClassification, qoSLevels);
								}
								return null;
							}
						}));
					}
				}
			}

			if (!futures.isEmpty()) {
				for (Future<Void> f : futures)
					f.get();
			}

			for (String serviceClassification : request.getServiceClassifications()) {

				QoSLevels qoSLevels = getQoSLevels(serviceClassification);

				if (qoSLevels == null)
					throw new IllegalStateException("QoSLevels not calculated for '" + serviceClassification + "'. Please run 'rebuildQoSLevels' operation before");

				ServiceClassificationInfo serviceClassificationInfo = getServiceClassificationInfo(serviceClassification);

				Double highestUtility = mapMaxUtility.get(serviceClassification);
				Integer numberOfServices = serviceClassificationInfo.getTotalNumberOfServices();

				result.getMaxServiceUtilityByServiceClassification().put(serviceClassification, highestUtility);
				result.getNumberOfServicesByServiceClassification().put(serviceClassification, numberOfServices);

				if (serviceClassificationInfo != null) {
					result.getMaxQoSValueByServiceClassification().put(serviceClassification, serviceClassificationInfo.getMaxQoSValue());
					result.getMinQoSValueByServiceClassification().put(serviceClassification, serviceClassificationInfo.getMinQoSValue());
				}

				Map<String, NavigableMap<Integer, List<QoSLevel>>> qoSLevelsByServiceClassification = new HashMap<String, NavigableMap<Integer, List<QoSLevel>>>();
				result.getQoSLevelsByServiceClassification().put(serviceClassification, qoSLevelsByServiceClassification);

				// Only if is custom QoSWeights
				Map<String, NavigableMap<Double, Double>> mapServiceQoSUtility = mapCategoriesServicesQoSUtility != null ? mapCategoriesServicesQoSUtility.get(serviceClassification) : null;

				for (String qoSKey : request.getQoSKeys()) {

					// Only if is custom QoSWeights
					NavigableMap<Double, Double> qoSUtility = mapServiceQoSUtility != null ? mapServiceQoSUtility.get(qoSKey) : null;

					NavigableMap<Integer, List<QoSLevel>> qoSLevelsByQoSKey = new TreeMap<Integer, List<QoSLevel>>();
					qoSLevelsByServiceClassification.put(qoSKey, qoSLevelsByQoSKey);

					for (Integer numberOfLevels : request.getNumbersOfLevels()) {

						Collection<QoSLevel> listQoSLevels = qoSLevels.getQoSLevels(qoSKey, numberOfLevels);

						if (listQoSLevels != null) {
							List<QoSLevel> levels = new ArrayList<QoSLevel>(numberOfLevels);
							qoSLevelsByQoSKey.put(numberOfLevels, levels);

							for (QoSLevel q : listQoSLevels) {

								// Only if is custom QoSWeights
								if (isCustomQoSWeights) {

									double v = q.getValue();

									int negativeH = serviceClassificationInfo.getCountLEQoSValue(qoSKey, v);
									int positiveH = serviceClassificationInfo.getCountGEQoSValue(qoSKey, v);

									Collection<Double> negativeServicesUtilities = qoSUtility.headMap(v, true).values();
									Collection<Double> positiveServicesUtilities = qoSUtility.tailMap(v, true).values();

									q = (QoSLevel) q.clone();

									// Negative
									{
										Double us = 0D;

										for (Double vu : negativeServicesUtilities)
											us = Math.max(us, vu);

										double u = ((double) negativeH / (double) numberOfServices) * (us / highestUtility);

										q.setNegativeUtility(u);
									}
									// Positive
									{
										Double us = 0D;

										for (Double vu : positiveServicesUtilities)
											us = Math.max(us, vu);

										double u = ((double) positiveH / (double) numberOfServices) * (us / highestUtility);

										q.setPositiveUtility(u);
									}

								}

								levels.add(q);
							}
						}
					}
				}

			}

			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private List<String> getUDDIInquiryServiceEndPoints() {

		if (uddiRepositoriesEndpoints == null) {

			synchronized (this) {

				if (uddiRepositoriesEndpoints == null) {

					List<String> result = new ArrayList<String>();

					UDDIInquiryPortType inquiry = getUDDIFederationInquiryService();

					if (inquiry != null) {

						try {
							FindService findService = new FindService();

							TModelBag tModelBag = new TModelBag();
							findService.setTModelBag(tModelBag);
							tModelBag.getTModelKey().add(UddiKeys.UDDI_V3_INQUIRY);
							tModelBag.getTModelKey().add(UddiKeys.UDDI_UDDISERVICEPROVIDER);

							ServiceList services = inquiry.findService(findService);

							List<String> serviceKeys = new ArrayList<String>();

							if (services.getServiceInfos() != null) {

								for (org.uddi.api_v3.ServiceInfo serviceInfo : services.getServiceInfos().getServiceInfo())
									serviceKeys.add(serviceInfo.getServiceKey());

								GetServiceDetail getServiceDetail = new GetServiceDetail();
								getServiceDetail.getServiceKey().addAll(serviceKeys);

								ServiceDetail serviceDetail = inquiry.getServiceDetail(getServiceDetail);

								for (BusinessService businessService : serviceDetail.getBusinessService()) {

									for (BindingTemplate bt : businessService.getBindingTemplates().getBindingTemplate()) {

										if (bt.getAccessPoint() != null && bt.getAccessPoint().getUseType().equals("wsdlDeployment") && bt.getAccessPoint().getValue() != null)
											result.add(bt.getAccessPoint().getValue());

									}

								}
							}
							uddiRepositoriesEndpoints = result;
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
							uddiRepositoriesEndpoints = null;
						}
					}
				}
			}
		}

		return uddiRepositoriesEndpoints;
	}

	private Map<String, QoSAttribute> getMapTModelKeyToQoSAttribute() {

		if (mapTModelKeyToQoSAttribute.isEmpty()) {
			synchronized (this) {
				if (mapTModelKeyToQoSAttribute.isEmpty()) {

					List<QoSItem> qosItens = getQoSItems();

					for (QoSItem q0 : qosItens) {
						for (QoSAttribute q : q0.getQoSAttributes())
							logger.info("QoS Item " + q.getQoSItem() + "." + q.getName() + " loaded");
					}
				}
			}
		}
		return mapTModelKeyToQoSAttribute;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private BiMap<String, String> getMapServiceClassificationToTModel() {

		if (mapServiceClassificationToTModel.isEmpty()) {
			synchronized (this) {

				if (mapServiceClassificationToTModel.isEmpty()) {

					try {

						FindTModel findTModel = new FindTModel();

						CategoryBag categoryBag = new CategoryBag();
						findTModel.setCategoryBag(categoryBag);

						KeyedReference keyedReference = new KeyedReference();
						keyedReference.setTModelKey(UddiKeys.UDDI_CATEGORIZATION_TYPES);
						keyedReference.setKeyValue("ServiceClassification");
						categoryBag.getKeyedReference().add(keyedReference);

						UDDIInquiryPortType inquiry = getUDDIFederationInquiryService();

						// As fallback, we'll use Infinispan local cache
						if (inquiry == null) {

							Object v = getParametersCache().get(PARAMETERS_CACHE_KEY_SERVICE_CLASSIFICATION_TO_TMODEL);

							if (v instanceof Map && !((BiMap) v).isEmpty()) {
								mapServiceClassificationToTModel.putAll((BiMap<String, String>) v);
								logger.warn("Can't obtain Service Classification to TModel mapping from UDDI Federation Server, using local cached values instead");
							} else {
								throw new IllegalStateException("Can't obtain Service Classification to TModel mapping from UDDI Federation Server");
							}
						} else {

							// Getting QoS Items
							TModelList tModelList = null;

							try {
								tModelList = inquiry.findTModel(findTModel);
							} catch (WebServiceException e) {
							}

							if (tModelList == null) {

								Object v = getParametersCache().get(PARAMETERS_CACHE_KEY_SERVICE_CLASSIFICATION_TO_TMODEL);

								if (v instanceof Map && !((BiMap) v).isEmpty()) {
									mapServiceClassificationToTModel.putAll((BiMap<String, String>) v);
									logger.warn("Can't obtain Service Classification to TModel mapping from UDDI Federation Server, using local cached values instead");
								} else {
									throw new IllegalStateException("Can't obtain Service Classification to TModel mapping from UDDI Federation Server");
								}

							} else {

								if (tModelList.getTModelInfos() != null) {

									Set<String> tModelKeys = new HashSet<String>();

									for (TModelInfo info : tModelList.getTModelInfos().getTModelInfo())
										tModelKeys.add(info.getTModelKey());

									GetTModelDetail getTModelDetail = new GetTModelDetail();
									getTModelDetail.getTModelKey().addAll(tModelKeys);

									TModelDetail tModelDetail = inquiry.getTModelDetail(getTModelDetail);

									if (tModelDetail.getTModel() != null) {

										for (TModel tModel : tModelDetail.getTModel()) {

											if (tModel.getCategoryBag() != null) {

												for (KeyedReference qoSItemKeyedReference : tModel.getCategoryBag().getKeyedReference()) {

													if (qoSItemKeyedReference.getTModelKey().equals(UddiKeys.UDDI_PROCESSES_SERVICECLASSIFICATION)) {
														mapServiceClassificationToTModel.put(qoSItemKeyedReference.getKeyValue(), tModel.getTModelKey());
														break;
													}
												}
											}
										}
									}
								}
								getParametersCache().put(PARAMETERS_CACHE_KEY_SERVICE_CLASSIFICATION_TO_TMODEL, mapServiceClassificationToTModel);
							}
						}

					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						mapServiceClassificationToTModel.clear();
						if (e instanceof RuntimeException)
							throw (RuntimeException) e;
						else
							new RuntimeException(e);
					}
				}
			}
		}
		return mapServiceClassificationToTModel;
	}

	@Override
	public Map<String, QoSAttribute> getQoSAttributes() {
		Map<String, QoSAttribute> m = new LinkedHashMap<String, QoSAttribute>();

		for (QoSAttribute q : getQoSAttributesInternal())
			m.put(q.getKey(), q);

		return m;
	}

	@Override
	public Map<String, QoSAttribute> getEnabledQoSAttributes() {
		Map<String, QoSAttribute> m = new LinkedHashMap<String, QoSAttribute>();

		for (QoSAttribute q : getQoSAttributesInternal())
			if (q.getValueUtilityDirection() != null)
				m.put(q.getKey(), q);

		return m;
	}

	private Collection<QoSAttribute> getQoSAttributesInternal() {
		if (mapTModelKeyToQoSAttribute.isEmpty()) {
			synchronized (this) {
				if (mapTModelKeyToQoSAttribute.isEmpty()) {
					getQoSItems();
				}
			}
		}
		return mapTModelKeyToQoSAttribute.values();
	}

	@Override
	public List<QoSItem> getEnabledQoSItems() {

		synchronized (this) {

			if (enabledQoSItems.isEmpty()) {

				enabledQoSItems.addAll(getQoSItems());

				ListIterator<QoSItem> it = enabledQoSItems.listIterator();

				while (it.hasNext()) {

					QoSItem item = it.next();
					item = (QoSItem) item.clone();
					it.set(item);

					ListIterator<QoSAttribute> it2 = item.getQoSAttributes().listIterator();

					boolean removeQoSItem = true;

					while (it2.hasNext()) {

						QoSAttribute q = it2.next();

						if (q.getValueUtilityDirection() == null) {
							it2.remove();
						} else {
							removeQoSItem = false;
						}
					}

					if (removeQoSItem)
						it.remove();
				}

			}
		}
		return enabledQoSItems;
	}

	@Override
	public List<QoSItem> getQoSItems() {

		if (qoSItems.isEmpty()) {

			synchronized (this) {

				if (qoSItems.isEmpty()) {

					mapTModelKeyToQoSAttribute.clear();

					Map<String, QoSItem> mapQoSItem = new HashMap<String, QoSItem>();

					try {

						FindTModel findTModel = new FindTModel();

						CategoryBag categoryBag = new CategoryBag();
						findTModel.setCategoryBag(categoryBag);

						KeyedReference keyedReference = new KeyedReference();
						keyedReference.setTModelKey(UddiKeys.UDDI_CATEGORIZATION_TYPES);
						keyedReference.setKeyValue("QoSItem");
						categoryBag.getKeyedReference().add(keyedReference);

						UDDIInquiryPortType inquiry = getUDDIFederationInquiryService();

						// As fallback, we'll use Infinispan local cache
						if (inquiry == null) {
							loadQoSItemsFromCache();
						} else {

							// Getting QoS Items
							TModelList tModelList = null;

							try {
								tModelList = inquiry.findTModel(findTModel);
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}

							// As fallback, we'll use Infinispan local cache
							if (tModelList == null) {
								loadQoSItemsFromCache();

							} else {

								if (tModelList.getTModelInfos() != null) {
									Set<String> qoSItemsKeys = new HashSet<String>();

									for (TModelInfo info : tModelList.getTModelInfos().getTModelInfo())
										qoSItemsKeys.add(info.getTModelKey());

									GetTModelDetail getTModelDetail = new GetTModelDetail();
									getTModelDetail.getTModelKey().addAll(qoSItemsKeys);

									TModelDetail tModelDetail = inquiry.getTModelDetail(getTModelDetail);

									if (tModelDetail.getTModel() != null) {

										for (TModel tModel : tModelDetail.getTModel()) {

											QoSItem qoSItem = new QoSItem();

											if (tModel.getDescription() != null && tModel.getDescription().size() > 0)
												qoSItem.setDescription(tModel.getDescription().get(0).getValue());

											if (tModel.getCategoryBag() != null) {

												for (KeyedReference qoSItemKeyedReference : tModel.getCategoryBag().getKeyedReference()) {

													if (isTModelKeyEquals(qoSItemKeyedReference, UddiKeys.UDDI_QOS_ITEM_ID))
														qoSItem.setId(Integer.valueOf(qoSItemKeyedReference.getKeyValue()));

													if (isTModelKeyEquals(qoSItemKeyedReference, UddiKeys.UDDI_QOS_ITEM))
														qoSItem.setName(qoSItemKeyedReference.getKeyValue());

												}

											}

											mapQoSItem.put(tModel.getTModelKey(), qoSItem);
											qoSItems.add(qoSItem);

										}

										// Finding Qos Attributes
										findTModel = new FindTModel();

										categoryBag = new CategoryBag();
										findTModel.setCategoryBag(categoryBag);

										keyedReference = new KeyedReference();
										keyedReference.setTModelKey(UddiKeys.UDDI_CATEGORIZATION_TYPES);
										keyedReference.setKeyValue("QoSAttribute");
										categoryBag.getKeyedReference().add(keyedReference);

										tModelList = inquiry.findTModel(findTModel);

										if (tModelList.getTModelInfos() != null) {

											Set<String> qoSAttributesKeys = new HashSet<String>();

											for (TModelInfo info : tModelList.getTModelInfos().getTModelInfo())
												qoSAttributesKeys.add(info.getTModelKey());

											getTModelDetail = new GetTModelDetail();
											getTModelDetail.getTModelKey().addAll(qoSAttributesKeys);

											tModelDetail = inquiry.getTModelDetail(getTModelDetail);

											if (tModelDetail.getTModel() != null) {

												for (TModel tModel : tModelDetail.getTModel()) {

													QoSAttribute qoSAttribute = new QoSAttribute();

													mapTModelKeyToQoSAttribute.put(tModel.getTModelKey(), qoSAttribute);

													if (tModel.getCategoryBag() != null) {

														for (KeyedReference qoSAttributeKeyedReference : tModel.getCategoryBag().getKeyedReference()) {

															if (isTModelKeyEquals(qoSAttributeKeyedReference, UddiKeys.UDDI_QOS_ITEM)) {
																QoSItem item = mapQoSItem.get(qoSAttributeKeyedReference.getKeyName());
																qoSAttribute.setQoSItem(item.getName());
																qoSAttribute.setQoSItemId(item.getId());
																item.getQoSAttributes().add(qoSAttribute);
															}

															if (isTModelKeyEquals(qoSAttributeKeyedReference, UddiKeys.UDDI_QOS_ATTRIBUTE))
																qoSAttribute.setName(qoSAttributeKeyedReference.getKeyValue());

															if (isTModelKeyEquals(qoSAttributeKeyedReference, UddiKeys.UDDI_QOS_UNIT))
																qoSAttribute.setUnit(qoSAttributeKeyedReference.getKeyValue());

															if (isTModelKeyEquals(qoSAttributeKeyedReference, UddiKeys.UDDI_QOS_UTILITYDIRECTION))
																qoSAttribute.setValueUtilityDirection(valueFromQoSValueUtilityDirection(qoSAttributeKeyedReference.getKeyValue()));

															if (isTModelKeyEquals(qoSAttributeKeyedReference, UddiKeys.UDDI_QOS_AGGREGATION_STRUCTURE_SEQUENTIAL))
																qoSAttribute.setSequentialAggregationType(valueFromQoSValueAggregationType(qoSAttributeKeyedReference.getKeyValue()));

															if (isTModelKeyEquals(qoSAttributeKeyedReference, UddiKeys.UDDI_QOS_AGGREGATION_STRUCTURE_LOOP))
																qoSAttribute.setLoopAggregationType(valueFromQoSValueAggregationType(qoSAttributeKeyedReference.getKeyValue()));

															if (isTModelKeyEquals(qoSAttributeKeyedReference, UddiKeys.UDDI_QOS_AGGREGATION_STRUCTURE_PARALLEL))
																qoSAttribute.setParallelAggregationType(valueFromQoSValueAggregationType(qoSAttributeKeyedReference.getKeyValue()));

															if (isTModelKeyEquals(qoSAttributeKeyedReference, UddiKeys.UDDI_QOS_AGGREGATION_STRUCTURE_CONDITIONAL))
																qoSAttribute.setConditionalAggregationType(valueFromQoSValueAggregationType(qoSAttributeKeyedReference.getKeyValue()));
														}
													}
												}
											}
										}
									}
								}
								getParametersCache().put(PARAMETERS_CACHE_KEY_QOS_ITEMS, qoSItems);
								getParametersCache().put(PARAMETERS_CACHE_KEY_TMODEL_KEY_TO_QOS_ATTRIBUTE, mapTModelKeyToQoSAttribute);
							}
						}

					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						qoSItems.clear();
						mapTModelKeyToQoSAttribute.clear();
					}
				}
			}

			if (logger.isInfoEnabled() && qoSItems != null && !qoSItems.isEmpty()) {
				StringBuilder sb = new StringBuilder("Loaded QoS items:");
				for (QoSItem i : qoSItems) {
					sb.append("\n\t" + i.getName());
					for (QoSAttribute att : i.getQoSAttributes()) {
						sb.append("\n\t\t" + att.getKey() + " - " + att.getName());
					}

				}
				logger.info(sb.toString());
			}
		}

		return qoSItems;

	}

	private boolean isTModelKeyEquals(KeyedReference kf, String tModelKey) {
		String tmk = kf.getTModelKey();
		return isTModelKeyEquals(tModelKey, tmk);
	}

	private boolean isTModelKeyEquals(String tModelKey, String tmk) {
		tmk = tmk.replaceAll(UddiKeys.UDDI_GSIGMA_PARTITION + ":", "");
		tModelKey = tModelKey.replaceAll(UddiKeys.UDDI_GSIGMA_PARTITION + ":", "");
		return tmk.equals(tModelKey);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadQoSItemsFromCache() {
		Object v1 = getParametersCache().get(PARAMETERS_CACHE_KEY_QOS_ITEMS);

		if (v1 instanceof List && !((List) v1).isEmpty()) {
			qoSItems.addAll((Collection<? extends QoSItem>) v1);
			logger.warn("Can't obtain QoS Items from UDDI Federation Server, using local cached values instead");
		} else {
			throw new IllegalStateException("Can't obtain QoS Items from UDDI Federation Server");
		}

		Object v2 = getParametersCache().get(PARAMETERS_CACHE_KEY_TMODEL_KEY_TO_QOS_ATTRIBUTE);

		if (v2 instanceof Map && !((Map) v2).isEmpty()) {
			mapTModelKeyToQoSAttribute.putAll((Map<String, QoSAttribute>) v2);
			logger.warn("Can't obtain TModel keys to QoS Attributes mapping from UDDI Federation Server, using local cached values instead");
		} else {
			throw new IllegalStateException("Can't obtain TModel keys to QoS Attributes mapping from UDDI Federation Server");
		}
	}

	private QoSAttribute.QoSValueUtilityDirection valueFromQoSValueUtilityDirection(String value) {

		for (QoSAttribute.QoSValueUtilityDirection v : QoSAttribute.QoSValueUtilityDirection.values()) {
			if (v.name().equalsIgnoreCase(value))
				return v;
		}

		return null;
	}

	private QoSAttribute.QoSValueAggregationType valueFromQoSValueAggregationType(String value) {

		for (QoSAttribute.QoSValueAggregationType v : QoSAttribute.QoSValueAggregationType.values()) {
			if (v.name().equalsIgnoreCase(value))
				return v;
		}

		return null;
	}

	private List<DiscoveredService> searchServicesInUDDI(String uddiInquiryEndpoint, DiscoveryRequest request, int maxRowsPerRequest) {

		logger.debug("[START] searchServicesInUDDI(uddiInquiryEndpoint=" + uddiInquiryEndpoint + ", request=" + request + ")");

		try {

			List<DiscoveredService> candidates = new LinkedList<DiscoveredService>();

			UDDIInquiryPortType inquiry = getUDDIInquiryService(uddiInquiryEndpoint);

			// If the UDDI Repository is available
			if (inquiry != null && !listDeadUDDI.contains(uddiInquiryEndpoint) && isServiceAlive(uddiInquiryEndpoint)) {

				FindService findService = new FindService();

				FindTModel findTModel = new FindTModel();
				findTModel.setCategoryBag(new CategoryBag());
				findService.setFindTModel(findTModel);

				FindQualifiers findQualifiers = new FindQualifiers();
				findTModel.setFindQualifiers(findQualifiers);

				findQualifiers.getFindQualifier().add(UDDIConstants.OR_ALL_KEYS_TMODEL);

				for (DiscoveryRequestItem item : request.getItens()) {
					KeyedReference keyedReference = new KeyedReference();
					keyedReference.setTModelKey(UddiKeys.UDDI_PROCESSES_SERVICECLASSIFICATION);
					keyedReference.setKeyValue(item.getServiceClassification());
					findTModel.getCategoryBag().getKeyedReference().add(keyedReference);
				}

				int start = 1;

				findService.setMaxRows(maxRowsPerRequest);

				boolean hasMoreServices = true;

				while (hasMoreServices) {

					// Getting Service Info
					logger.info("searchServicesInUDDI(uddiInquiryEndpoint=" + uddiInquiryEndpoint + ", start=" + start + ")");
					findService.setListHead(start);
					ServiceList services = inquiry.findService(findService);

					start = start + maxRowsPerRequest;

					if (start > services.getListDescription().getActualCount()) {
						hasMoreServices = false;
					}

					// if (services.getListDescription().)

					if (services.getServiceInfos() == null) {
						for (DiscoveryRequestItem item : request.getItens()) {
							logger.error("NULL:" + item.getServiceClassification() + ";uddiInquiryEndpoint=" + uddiInquiryEndpoint);
						}
					} else {

						Set<String> serviceKeys = new HashSet<String>();

						if (services.getServiceInfos() != null && services.getServiceInfos().getServiceInfo() != null)
							for (org.uddi.api_v3.ServiceInfo info : services.getServiceInfos().getServiceInfo())
								serviceKeys.add(info.getServiceKey());

						if (serviceKeys.size() > 0) {

							GetServiceDetail getServiceDetail = new GetServiceDetail();

							getServiceDetail.getServiceKey().addAll(serviceKeys);

							// Getting Service Detail
							ServiceDetail serviceDetail = inquiry.getServiceDetail(getServiceDetail);

							// If there is at least one business service
							if (serviceDetail.getBusinessService() != null) {

								Map<String, DiscoveredService> mapBindingKeyToDiscoveredService = new HashMap<String, DiscoveredService>();

								for (BusinessService businessService : serviceDetail.getBusinessService()) {
									DiscoveredService s = getDiscoveredService(uddiInquiryEndpoint, businessService);
									mapBindingKeyToDiscoveredService.put(s.getBindingTemplateKey(), s);

								}

								// Getting Publisher Info
								GetOperationalInfo getOperationalInfo = new GetOperationalInfo();
								getOperationalInfo.getEntityKey().addAll(mapBindingKeyToDiscoveredService.keySet());

								OperationalInfos operationalInfos = inquiry.getOperationalInfo(getOperationalInfo);

								if (operationalInfos != null && operationalInfos.getOperationalInfo() != null) {

									for (OperationalInfo operationalInfo : operationalInfos.getOperationalInfo()) {

										ServiceProvider serviceProvider = new ServiceProvider(operationalInfo.getAuthorizedName(), operationalInfo.getAuthorizedName());

										DiscoveredService discoveredService = mapBindingKeyToDiscoveredService.get(operationalInfo.getEntityKey());

										discoveredService.setServiceProvider(serviceProvider);

									}

								}

							}
						}
					}
				}

				// DiscoveryRequestItem item = request.getItens().get(0);
				// QoSInformation qoSInformation = item.getQoSInformation();
				// String serviceClassification = item.getServiceClassification();
				//
				// // Filtering the candidates by QoS Constraints
				// filterServiceList(candidates, matchingServices, null, qoSInformation);

				logger.debug("[END] searchServicesInUDDI(uddiInquiryEndpoint=" + uddiInquiryEndpoint + ", request=" + request + ")");

			} else {
				listDeadUDDI.add(uddiInquiryEndpoint);
			}

			return candidates;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

	}

	DiscoveredService getDiscoveredService(String uddiInquiryEndpoint, BusinessService businessService) {

		DiscoveredService discoveredService = null;

		for (BindingTemplate bindingTemplate : businessService.getBindingTemplates().getBindingTemplate()) {

			String serviceClassification = null;

			Map<String, String> mapTModelToServiceClassification = getMapServiceClassificationToTModel().inverse();

			for (TModelInstanceInfo t : bindingTemplate.getTModelInstanceDetails().getTModelInstanceInfo()) {
				String s = mapTModelToServiceClassification.get(t.getTModelKey());
				if (s != null) {
					serviceClassification = s;
					break;
				}
			}

			discoveredService = new DiscoveredService(businessService.getServiceKey(), bindingTemplate.getBindingKey(), uddiInquiryEndpoint, businessService.getName().get(0).getValue(), bindingTemplate.getAccessPoint().getValue(), serviceClassification);

			// QoS Constraints
			if (bindingTemplate.getCategoryBag().getKeyedReferenceGroup() != null && bindingTemplate.getCategoryBag().getKeyedReferenceGroup().size() > 0) {

				for (KeyedReferenceGroup keyedReferenceGroup : bindingTemplate.getCategoryBag().getKeyedReferenceGroup()) {
					if (isTModelKeyEquals(keyedReferenceGroup.getTModelKey(), UddiKeys.UDDI_QOS_SERVICEQOSLIST)) {

						// Listing the QoS Values
						for (KeyedReference qoSKeyedReference : keyedReferenceGroup.getKeyedReference()) {

							QoSAttribute qoSAttribute = getMapTModelKeyToQoSAttribute().get(qoSKeyedReference.getTModelKey());

							ServiceQoSInformationItem qoSInformationItem = new ServiceQoSInformationItem();
							qoSInformationItem.setQoSValue(parseDouble(qoSKeyedReference.getKeyValue()));
							qoSInformationItem.setQoSAttributeName(qoSAttribute.getName());
							qoSInformationItem.setQoSItemId(qoSAttribute.getQoSItemId());
							qoSInformationItem.setQoSItemName(qoSAttribute.getQoSItem());
							qoSInformationItem.setQoSUnit(qoSAttribute.getUnit());

							discoveredService.getQoSInformation().add(qoSInformationItem);
						}

					}
				}
				// Collections.sort(discoveredService.getQoSInformation());
			}

			for (KeyedReference keyedReference : bindingTemplate.getCategoryBag().getKeyedReference()) {
				if (UddiKeys.UDDI_PROCESSES_SERVICEPROTOCOLCONVERTER.equals(keyedReference.getKeyName())) {
					discoveredService.setServiceProtocolConverter(keyedReference.getKeyValue());
				}
			}

			return discoveredService;
		}
		return null;
	}

	private double parseDouble(String s) {

		if (s.indexOf(',') != -1)
			s = s.replaceAll(",", ".");

		return Double.parseDouble(s);
	}

	private UDDIInquiryPortType getUDDIInquiryService(String uddiInquiryEndpoint) {

		UDDIInquiryPortType inquiry = mapUDDIFederationInquiryService.get(uddiInquiryEndpoint);

		if (inquiry == null) {
			synchronized (this) {
				if (inquiry == null) {
					try {
						logger.debug("Trying to get Inquire Service - " + uddiInquiryEndpoint);

						JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
						factory.setServiceClass(UDDIInquiryPortType.class);
						factory.setAddress(uddiInquiryEndpoint.split("\\?wsdl")[0]);
						inquiry = (UDDIInquiryPortType) factory.create();
						mapUDDIFederationInquiryService.put(uddiInquiryEndpoint, inquiry);

						logger.debug("*inquiry(" + uddiInquiryEndpoint + ")=" + inquiry);
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("Endpoint: " + uddiInquiryEndpoint + " -> not available");
					}
				}
			}
		}
		return inquiry;
	}

	@Override
	public String discoverUniqueServiceEndpoint(String serviceClassification, QoSInformation qoSInformation) throws Exception {

		List<DiscoveredService> list = discoverInternal(new DiscoveryRequest(serviceClassification, qoSInformation, 1), 1, null);

		if (list.size() > 0)
			return list.get(0).getServiceEndpointURL();
		else
			return null;
	}

	@Override
	public DiscoveryResult discoverServices(String serviceClassification, QoSInformation qoSInformation) throws Exception {
		DiscoveryResult result = new DiscoveryResult();

		final List<DiscoveredService> matchingServices = discoverInternal(new DiscoveryRequest(serviceClassification, qoSInformation), null, null);
		int ns = matchingServices != null ? matchingServices.size() : 0;

		result.setMatchingServices(matchingServices);

		if (serviceClassification != null) {
			result.getNumberOfMatchingServicesPerServiceClassification().putIfAbsent(serviceClassification, ns);

		} else {

			if (matchingServices != null) {
				for (DiscoveredService s : matchingServices) {
					String serviceClassification2 = s.getServiceClassification();
					Integer n = result.getNumberOfMatchingServicesPerServiceClassification().get(serviceClassification2);
					result.getNumberOfMatchingServicesPerServiceClassification().put(serviceClassification2, n == null ? 1 : n + 1);
				}
			}
		}

		result.setSuccessful(ns > 0);

		return result;
	}

	private static int multiplyPrevious(int initial, int multipler, int times) {

		int n = initial;

		for (int i = 0; i < times; i++)
			n = n * 2;

		return n;
	}

	private static List<Integer> getQoSLevelsInterval(int initial, int end, int multiplier) {

		List<Integer> levels = new LinkedList<Integer>();

		if (end < initial) {
			initial = end;
			end = multiplyPrevious(initial, 2, 4);
		}

		levels.add(initial);

		int current = initial;

		while (current <= end) {
			current = current * multiplier;
			if (current <= end)
				levels.add(current);
		}

		return levels;
	}

	@Override
	public ProcessQoSThresholds getProcessQoSThresholds(Process process) {

		try {

			ProcessStructureParser parser = new ProcessStructureParser(process);

			Map<String, QoSAttribute> qoSAttributes = getEnabledQoSAttributes();

			ProcessQoSAggregator agg = new ProcessQoSAggregator(qoSAttributes);

			VirtualTaskStructure vTask = parser.getVirtualTask(agg);

			Map<String, Double> maxQoSAgg = new HashMap<String, Double>();

			Map<String, Double> minQoSAgg = new HashMap<String, Double>();

			calculateGlobalQoSDelta(vTask, agg, qoSAttributes, maxQoSAgg, minQoSAgg);

			ProcessQoSThresholds result = new ProcessQoSThresholds();
			result.setProcessName(process.getName());

			for (String qoSKey : qoSAttributes.keySet()) {
				result.getQoSThresholds().add(new QoSThreshold(qoSKey, minQoSAgg.get(qoSKey), maxQoSAgg.get(qoSKey)));
			}

			for (TaskStructure t : vTask.getLeafTasks()) {

				if (t.getTaxonomyClassification() != null) {

					ServiceClassificationInfo serviceClassificationInfo = getServiceClassificationInfo(t.getTaxonomyClassification());

					if (serviceClassificationInfo != null) {

						List<QoSThreshold> l = new LinkedList<QoSThreshold>();

						for (String qoSKey : qoSAttributes.keySet()) {
							l.add(new QoSThreshold(qoSKey, serviceClassificationInfo.getQoSMinValue(qoSKey), serviceClassificationInfo.getQoSMaxValue(qoSKey)));
						}

						result.getTaskQoSThresholds().put(t.getName(), l);
					}
				}
			}
			return result;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<ServiceProvider> getServiceProviders() {
		return new ArrayList<ServiceProvider>(getServiceProvidersInternal());
	}

	private Collection<ServiceProvider> getServiceProvidersInternal() {

		Cache<String, ServiceProvider> cacheServiceProviders = getCacheServiceProviders();

		if (cacheServiceProviders.isEmpty()) {

			synchronized (cacheServiceProviders) {

				if (cacheServiceProviders.isEmpty()) {

					Collection<ServiceProvider> result = new TreeSet<ServiceProvider>(new Comparator<ServiceProvider>() {

						@Override
						public int compare(ServiceProvider p1, ServiceProvider p2) {
							return p1.getIdentification().compareTo(p2.getIdentification());
						}
					});

					SearchManager searchManager = Search.getSearchManager(getServicesCache());
					CacheQuery cacheQuery = searchManager.getQuery(new MatchAllDocsQuery(), IndexedService.class);

					try (ResultIterator it = cacheQuery.iterator()) {
						while (it.hasNext()) {
							IndexedService s = (IndexedService) it.next();
							result.add(s.getServiceProvider());
						}
					}

					for (ServiceProvider s : result) {
						cacheServiceProviders.put(s.getIdentification(), s);
					}
				}
			}
		}

		return cacheServiceProviders.values();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DiscoveryResult discover(DiscoveryRequest request) throws Exception {

		try {

			Integer maxResultsPerServiceClass = request.getMaxResultsPerServiceClass() != null ? request.getMaxResultsPerServiceClass() : 5;

			boolean hasProcess = request.getProcess() != null;

			boolean isHeuristicSelection = !request.isUseOptimalSelection();

			DiscoveryResult result = new DiscoveryResult();

			if (!hasProcess) {

				result.setMatchingServices(discoverInternal(request, maxResultsPerServiceClass, null));

			} else {

				Process process = request.getProcess();

				// Map<Participant, List<Task>> mapParticipantTasks = new TreeMap<Participant, List<Task>>();

				CompositionParticipants compositionParticipants = new CompositionParticipants();

				for (Task t : process.getTasks()) {
					if (t.getParticipants() != null) {

						for (TaskParticipant p : t.getParticipants()) {

							Participant participant = p.getParticipant();

							String participantName = participant.getName();

							CompositionParticipant cp = compositionParticipants.getParticipant(participantName);

							if (cp == null) {
								cp = new CompositionParticipant(participantName);
								compositionParticipants.addParticipant(cp);
							}

							cp.addTask(new CompositionParticipantTask(participantName, t.getName(), t.getTaxonomyClassification()));

							DiscoveryRequestItem di = request.getItemByTaxonomyClassification(t.getTaxonomyClassification());

							if (di != null) {
								di.setParticipantName(t.getParticipants() != null && !t.getParticipants().isEmpty() ? t.getParticipants().get(0).getParticipant().getName() : null);
							}
						}
					}
				}

				if (logger.isInfoEnabled()) {
					StringBuilder sb = new StringBuilder("The participants for " + process.getName() + " are:");
					for (CompositionParticipant cp : compositionParticipants.getParticipants()) {
						sb.append("\n\t" + cp.getParticipantName());
						for (CompositionParticipantTask t : cp.getTasks()) {
							sb.append("\n\t\t" + t.getTaskName());
						}
					}
					logger.info(sb.toString());
				}

				ProcessQoSInfo processQoSInfo = new ProcessQoSInfo();
				result.setProcessQoSInfo(processQoSInfo);

				Map<String, Double> qoSWeights = request.getQoSInformation() != null ? request.getQoSInformation().getQoSWeights() : null;

				List<String> servicesClassifications = new LinkedList<String>();

				ProcessStructureParser parser = new ProcessStructureParser(process);

				Map<String, Double> maxQoSAgg = new HashMap<String, Double>();

				Map<String, Double> minQoSAgg = new HashMap<String, Double>();

				Map<String, QoSAttribute> qoSAttributes = getEnabledQoSAttributes();

				if (qoSAttributes.isEmpty()) {
					logger.warn("EnabledQoSAttributes is empty");
				}

				int maxNumberOfLevels = 0;

				ProcessQoSAggregator agg = new ProcessQoSAggregator(qoSAttributes, request.getQoSInformation());

				VirtualTaskStructure vTask = parser.getVirtualTask(agg);

				logger.trace("VTask:" + vTask);

				Map<String, Double> globalQoSDelta = null;
				if (request.getQoSInformation() == null || request.getQoSInformation().getGlobalQoSDelta() == null || request.getQoSInformation().getGlobalQoSDelta().isEmpty()) {

					globalQoSDelta = calculateGlobalQoSDelta(vTask, agg, qoSAttributes, maxQoSAgg, minQoSAgg);
					processQoSInfo.setGlobalQoSDelta(globalQoSDelta);
					processQoSInfo.setGlobalQoSMaxValue(maxQoSAgg);
					processQoSInfo.setGlobalQoSMinValue(minQoSAgg);

				} else {
					globalQoSDelta = request.getQoSInformation().getGlobalQoSDelta();
				}

				QoSInformation qoSInformation = request.getQoSInformation();

				if (qoSInformation == null) {
					qoSInformation = new QoSInformation();
					request.setQoSInformation(qoSInformation);
				}
				qoSInformation.setGlobalQoSDelta(globalQoSDelta);

				if (isHeuristicSelection) {

					int initialQosLevels = request.getInitialNumberOfQoSLevels() != null ? request.getInitialNumberOfQoSLevels() : 10;
					int maxQoSLevels = request.getMaxNumberOfQoSLevels() != null ? request.getMaxNumberOfQoSLevels() : 80;

					List<Integer> numberOfLevels = getQoSLevelsInterval(initialQosLevels, maxQoSLevels, 2);

					logger.info("Using these QoS Levels --> " + numberOfLevels);

					maxNumberOfLevels = numberOfLevels.get(numberOfLevels.size() - 1);
					fillQoSLevelsForProcess(parser, numberOfLevels, qoSAttributes, qoSWeights);
					agg.groupQoSLevels(vTask);

				}

				Map<String, Integer> maxServicesPerServiceClassification = new HashMap<String, Integer>();

				for (TaskStructure t : vTask.getLeafTasks()) {

					String serviceClassification = t.getTaxonomyClassification();
					if (serviceClassification != null) {
						result.getNumberOfMatchingServicesPerServiceClassification().putIfAbsent(serviceClassification, 0);
						servicesClassifications.add(serviceClassification);

						DiscoveryRequestItem item = request.getItemByTaxonomyClassification(serviceClassification);
						if (item != null && item.getMaxResults() != null) {
							maxServicesPerServiceClassification.put(serviceClassification, item.getMaxResults());
						} else {
							maxServicesPerServiceClassification.put(serviceClassification, maxResultsPerServiceClass);
						}
					}
				}

				boolean successful = false;

				if (isHeuristicSelection) {

					agg.selectQoSLevels(vTask);
					Map<String, List<QoSConstraint>> localQoSConstraints = defineLocalQoSConstraints(request, vTask, qoSAttributes);
					processQoSInfo.setLocalQoSConstraints(localQoSConstraints);

					nextLevel: while (!successful) {

						List<DiscoveredService> discoveredServices = discoverInternal(request, maxResultsPerServiceClass, compositionParticipants);
						result.setMatchingServices(discoveredServices);

						for (Entry<String, Integer> e : result.getNumberOfMatchingServicesPerServiceClassification().entrySet()) {
							String serviceClassification = e.getKey();
							Integer n = e.getValue();
							if (n.intValue() == 0) {

								int current = agg.getCurrentNumberOfQoSLevels(serviceClassification);

								if (current == 0 || current == maxNumberOfLevels) {
									break nextLevel;
								}

								agg.selectQoSLevels(vTask);
								localQoSConstraints = defineLocalQoSConstraints(request, vTask, qoSAttributes);
								processQoSInfo.setLocalQoSConstraints(localQoSConstraints);

								continue nextLevel;
							}
						}
						successful = true;
					}

					// Optimal Selection
				} else {

					Map<String, ServiceClassificationInfo> mapServiceClassificationInfo = (Map) getCacheServiceClassification();

					Query q = filterServicesQueryForOptimalSelection(request, qoSAttributes);

					Iterator it = getServicesIterator(servicesClassifications.toArray(new String[servicesClassifications.size()]), q);

					Map<String, Double> mapServiceUtility = new HashMap<String, Double>();

					QoSMinMax qoSMinMax = getQoSMinMax(mapServiceClassificationInfo.keySet(), request.getQoSInformation().getGlobalQoSDelta());

					Map<String, List<QoSConstraint>> localQoSConstraints = new HashMap<String, List<QoSConstraint>>();

					if (request.getItens() != null) {
						for (DiscoveryRequestItem item : request.getItens()) {
							if (item.getServiceClassification() != null) {
								List<QoSConstraint> l = new LinkedList<QoSConstraint>();
								localQoSConstraints.put(item.getServiceClassification(), l);

								if (item.getQoSInformation() != null && item.getQoSInformation().getQoSConstraints() != null) {
									for (QoSConstraint qLocal : item.getQoSInformation().getQoSConstraints()) {
										if (!qLocal.isManaged()) {
											l.add(qLocal);
										}
									}
								}
							}
						}
					}

					List<DiscoveredService> services = ServiceQoSUtil.selectOptimalServices(parser.getTaskExecutionPaths(), parser.getTaskExecutionRoutes(), parser.getCycles(), it, mapServiceClassificationInfo, qoSAttributes, qoSInformation.getQoSConstraints(), localQoSConstraints,
							qoSInformation.getQoSWeights(), qoSMinMax, maxServicesPerServiceClassification, mapServiceUtility);

					ListIterator<DiscoveredService> itServices = services.listIterator();

					while (itServices.hasNext()) {
						DiscoveredService s = itServices.next();
						s = (DiscoveredService) s.clone();
						itServices.set(s);
						s.setUtility(mapServiceUtility.get(s.getServiceKey()));
					}

					result.setMatchingServices(services);

					successful = !services.isEmpty();

				}

				result.setSuccessful(successful);

				// Total Count
				result.setTotalNumberServicesPerServiceClassification(getMapNumberOfServicesPerServiceClassification(request, true));

				if (successful) {

					Map<String, String> mapAlias = new HashMap<String, String>();

					List<List<DiscoveredService>> servicesCombinations = null;

					Collection<List<DiscoveredService>> discoveredServices = result.groupByServiceClassification().values();

					int k = 1;

					for (List<DiscoveredService> l : discoveredServices) {

						int i = 1;

						for (DiscoveredService s : l) {
							mapAlias.put(s.getServiceKey(), "S" + k + "-" + (i++));
						}
						k++;
					}

					if (compositionParticipants != null && !compositionParticipants.getParticipants().isEmpty()) {

						if (logger.isDebugEnabled()) {

							StringBuilder sb = new StringBuilder("Available providers and services for each process role:");

							for (CompositionParticipant cp : compositionParticipants.getParticipants()) {
								sb.append("\n\t" + cp.getParticipantName());
								for (CompositionParticipantProvider cpp : cp.getProviders()) {
									sb.append("\n\t\t" + cpp.getProviderName());
									for (Entry<String, Set<DiscoveredService>> e : cpp.groupByServiceClassification().entrySet()) {
										sb.append("\n\t\t\t" + e.getKey());
										for (DiscoveredService s : e.getValue()) {
											sb.append("\n\t\t\t\t" + s);
										}
									}
								}
							}
							logger.debug(sb.toString());
						}

						servicesCombinations = new LinkedList<List<DiscoveredService>>();

						List<List<CompositionParticipantProvider>> participantProviders = //
								compositionParticipants.getParticipants().stream().map(CompositionParticipant::getProviders).collect(Collectors.toList());

						List<List<CompositionParticipantProvider>> participantProviderCombinations = new ArrayList<List<CompositionParticipantProvider>>(Lists.cartesianProduct(participantProviders));

						// Removing repeated providers combination (same provider in more than one participant name)
						ListIterator<List<CompositionParticipantProvider>> it = participantProviderCombinations.listIterator();

						next_provider_combination: while (it.hasNext()) {

							List<CompositionParticipantProvider> combination = it.next();

							Set<String> providers = new HashSet<String>();

							for (CompositionParticipantProvider cpp : combination) {
								if (!providers.add(cpp.getProviderName())) {
									it.remove();
									logger.debug("Removing providers combination " + combination + " because it has a repeated provider " + cpp.getProviderName());
									continue next_provider_combination;
								}
							}
						}

						logger.info("Computing services combination for " + participantProviderCombinations.size() + " possible providers combinations in participants " + compositionParticipants.getParticipantsNames());

						for (List<CompositionParticipantProvider> providerCombination : participantProviderCombinations) {

							List<List<List<DiscoveredService>>> providersServicesCombinations = new LinkedList<List<List<DiscoveredService>>>();

							// Combinations for a service provider services
							for (CompositionParticipantProvider cpp : providerCombination) {

								Map<String, Set<DiscoveredService>> gsc = cpp.groupByServiceClassification();

								List<Set<DiscoveredService>> groupedServices = new ArrayList<Set<DiscoveredService>>(gsc.values());

								List<List<DiscoveredService>> providerServicesCombinations = new ArrayList<List<DiscoveredService>>(Sets.cartesianProduct(groupedServices));

								logger.debug(providerServicesCombinations.size() + " combinations for " + cpp + " with services classifications " + gsc.keySet());

								providersServicesCombinations.add(providerServicesCombinations);
							}

							List<List<List<DiscoveredService>>> participantsServicesCombinations = Lists.cartesianProduct(providersServicesCombinations);

							logger.debug(participantsServicesCombinations.size() + " combinations for providers combination " + providerCombination);

							for (List<List<DiscoveredService>> psc : participantsServicesCombinations) {

								List<DiscoveredService> combination = new LinkedList<DiscoveredService>();

								for (List<DiscoveredService> l : psc) {
									combination.addAll(l);
								}

								servicesCombinations.add(combination);
							}
						}

						// Don't group by participants
					} else {

						if (logger.isDebugEnabled()) {
							for (List<DiscoveredService> l : discoveredServices) {

								StringBuilder sb = new StringBuilder();
								sb.append(l.get(0).getServiceClassification() + "\n");
								for (DiscoveredService s : l)
									sb.append("\t" + s + "utility=" + s.getUtility() + "\n");
								logger.debug(sb.toString());
							}
						}

						servicesCombinations = Lists.cartesianProduct(new ArrayList(discoveredServices));
					}

					final int numberOfServicesCombinations = servicesCombinations.size();

					List<ServicesComposition> compositions = new SortedList<ServicesComposition>(new BasicEventList<ServicesComposition>(numberOfServicesCombinations), new Comparator<ServicesComposition>() {
						@Override
						public int compare(ServicesComposition c1, ServicesComposition c2) {
							return new Double(c2.getCompositionUtility()).compareTo(c1.getCompositionUtility());
						}
					});

					if (logger.isInfoEnabled()) {

						StringBuilder sb = new StringBuilder();

						List<DiscoveryRequestItem> itens = new ArrayList<DiscoveryRequestItem>(request.getItens());

						Collections.sort(itens, new Comparator<DiscoveryRequestItem>() {
							@Override
							public int compare(DiscoveryRequestItem d1, DiscoveryRequestItem d2) {

								int c = ObjectUtils.compare(d1.getParticipantName(), d2.getParticipantName());

								if (c == 0) {
									return ObjectUtils.compare(d1.getServiceClassification(), d2.getServiceClassification());
								}

								return c;
							}
						});

						for (DiscoveryRequestItem i : itens) {
							sb.append("\n\t" + i);
						}

						logger.info("There is " + numberOfServicesCombinations + " services combinations for process " + process.getName() + " using discovery itens:" + sb.toString());
					}

					int numberOfTasks = vTask.getLeafTasks().size();

					int z = 0;

					final int maxNumberOfCompositions = request.getTotalNumberOfCompositions() != null ? request.getTotalNumberOfCompositions() : 100;

					final int pruningPoint = maxNumberOfCompositions * 5;

					nextServiceCombination: for (List<DiscoveredService> serviceCombination : servicesCombinations) {

						if (z++ > pruningPoint) {
							logger.warn("Pruning service combinations in a max of " + pruningPoint + " combinations.");
							break;
						}

						Map<String, DiscoveredService> scvs = new HashMap<String, DiscoveredService>();
						for (DiscoveredService s : serviceCombination) {
							scvs.put(s.getServiceClassification(), s);
						}

						List<ServiceInfo> services = new SortedList<ServiceInfo>(new BasicEventList<ServiceInfo>(numberOfTasks), new Comparator<ServiceInfo>() {
							@Override
							public int compare(ServiceInfo s1, ServiceInfo s2) {
								return s1.getServiceProviderName().compareTo(s2.getServiceProviderName());
								// return s1.getAlias().compareTo(s2.getAlias());
							}
						});

						vTask.clearQoSValues();

						for (TaskStructure t : vTask.getLeafTasks()) {

							String serviceClassification = t.getTaxonomyClassification();

							if (serviceClassification != null) {

								DiscoveredService s = scvs.get(serviceClassification);

								for (ServiceQoSInformationItem q : s.getQoSInformation()) {
									t.setQoSValue(q.getQoSKey(), q.getQoSValue());
								}

								ServiceProvider serviceProvider = s.getServiceProvider();

								ServiceInfo serviceInfo = new ServiceInfo(s.getServiceKey(), s.getServiceClassification(), s.getBindingTemplateKey(), s.getUtility(), serviceProvider != null ? serviceProvider.getName() : null, serviceProvider != null ? serviceProvider.getUtility() : null,
										s.getServiceProtocolConverter());

								serviceInfo.setAlias(mapAlias.get(s.getServiceKey()));

								services.add(serviceInfo);
							}
						}

						// Getting end-to-end QoS
						agg.groupQoSValues(vTask);

						// Checking if the combinations meet the QoS Constraints (it's only necessary when using optimal selection)
						if (qoSInformation.getQoSConstraints() != null) {
							for (QoSConstraint c : qoSInformation.getQoSConstraints()) {

								double v = vTask.getQoSValues().get(c.getQoSKey());

								if ((c.getComparisionType() == QoSValueComparisionType.GE && v < c.getQoSValue()) || (c.getComparisionType() == QoSValueComparisionType.LE && v > c.getQoSValue())) {
									continue nextServiceCombination;
								}
							}
						}

						ServicesComposition comp = new ServicesComposition();

						comp.setServices(services);

						// Cloning the list because it is reused in next iteration
						Map<String, Double> qoSValues = new LinkedHashMap<String, Double>(vTask.getQoSValues());
						comp.setQoSValues(qoSValues);

						// Composition Utility
						Double compositionUtility = ServiceQoSUtil.calculateCompositionUtility(qoSValues, qoSAttributes, maxQoSAgg, minQoSAgg, qoSWeights);
						comp.setCompositionUtility(compositionUtility);

						compositions.add(comp);

					}

					result.setTotalNumberOfCompositions(numberOfServicesCombinations);

					if (compositions.size() < maxNumberOfCompositions)
						result.setCompositions(compositions);
					else
						result.setCompositions(compositions.subList(0, maxNumberOfCompositions));

					int w = 1;

					for (ServicesComposition c : result.getCompositions()) {
						c.setRanking(w++);
					}
				}
			}

			return result;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private Query filterServicesQueryForOptimalSelection(DiscoveryRequest request, Map<String, QoSAttribute> qoSAttributes) {
		Query q = null;

		List<QoSConstraint> qoSConstraints = request.getQoSInformation() != null ? request.getQoSInformation().getQoSConstraints() : null;

		if (qoSConstraints != null) {

			List<QoSConstraint> qList = new LinkedList<QoSConstraint>();

			for (QoSConstraint c : qoSConstraints) {

				QoSAttribute qoSAtt = qoSAttributes.get(c.getQoSKey());

				if (qoSAtt != null && (qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.SUM || qoSAtt.getSequentialAggregationType() == QoSValueAggregationType.MIN)) {

					boolean constraintSameUtilityDirection = //
							(qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE && c.getComparisionType() == QoSValueComparisionType.LE) || (qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.POSITIVE && c.getComparisionType() == QoSValueComparisionType.GE);

					if (constraintSameUtilityDirection) {
						qList.add(c);
					}

				}
			}
			if (!qList.isEmpty()) {
				q = getQoSConstraintsQuery(qList);
			}
		}
		return q;
	}

	private Map<String, Double> calculateGlobalQoSDelta(VirtualTaskStructure vTask, ProcessQoSAggregator agg, Map<String, QoSAttribute> qoSAttributes, Map<String, Double> maxQoSAgg, Map<String, Double> minQoSAgg) {

		Collection<TaskStructure> leafTasks = vTask.getLeafTasks();

		// Min
		for (TaskStructure t : leafTasks) {
			String serviceClassification = t.getTaxonomyClassification();
			ServiceClassificationInfo sci = getServiceClassificationInfo(serviceClassification);
			if (sci != null) {
				for (Entry<String, Double> e : sci.getMinQoSValue().entrySet()) {
					String qoSKey = e.getKey();
					if (qoSAttributes.containsKey(qoSKey)) {
						Double v = e.getValue();
						t.setQoSValue(qoSKey, v);
						logger.debug("QoS: " + serviceClassification + " -> " + qoSKey + " -> MIN: " + v);
					}

				}
			}
		}
		agg.groupQoSValues(vTask);
		minQoSAgg.putAll(vTask.getQoSValues());

		vTask.clearQoSValues();

		// Max
		for (TaskStructure t : leafTasks) {
			String serviceClassification = t.getTaxonomyClassification();
			ServiceClassificationInfo sci = getServiceClassificationInfo(t.getTaxonomyClassification());
			if (sci != null) {
				for (Entry<String, Double> e : sci.getMaxQoSValue().entrySet()) {
					String qoSKey = e.getKey();
					if (qoSAttributes.containsKey(qoSKey)) {
						Double v = e.getValue();
						t.setQoSValue(qoSKey, v);
						logger.debug("QoS: " + serviceClassification + " -> " + qoSKey + " -> MAX: " + v);
					}
				}
			}
		}
		agg.groupQoSValues(vTask);
		maxQoSAgg.putAll(vTask.getQoSValues());

		vTask.clearQoSValues();

		Map<String, Double> globalQoSDelta = new LinkedHashMap<String, Double>();

		for (String qoSKey : qoSAttributes.keySet()) {
			Double max = maxQoSAgg.get(qoSKey);
			Double min = minQoSAgg.get(qoSKey);
			if (max != null && min != null)
				globalQoSDelta.put(qoSKey, max - min);
		}

		StringBuilder sb = new StringBuilder();

		int k = 0;
		for (Entry<String, Double> e : globalQoSDelta.entrySet()) {
			Object qoSAtt = e.getKey();
			logger.info("Global QoS Delta on '" + e.getKey() + "' is " + e.getValue());
			sb.append("\t" + qoSAtt + " -> MIN: " + minQoSAgg.get(qoSAtt) + " MAX: " + maxQoSAgg.get(qoSAtt) + (k++ < globalQoSDelta.size() - 1 ? "\n" : ""));
		}

		String str = sb.toString();
		if (!str.isEmpty()) {
			logger.info("Composition QoS Min/Max:\n" + str);
		} else {
			logger.warn("Can't calculate global QoS information");
		}

		return globalQoSDelta;
	}

	@Override
	public ProcessQoSInfo getLocalQoSConstraints(br.ufsc.gsigma.catalog.services.model.Process process, final QoSInformation qoSInformation) {

		try {

			ProcessQoSInfo processQoSInfo = new ProcessQoSInfo();

			if (process == null)
				return processQoSInfo;

			Map<String, QoSAttribute> enabledQoSAttributes = getEnabledQoSAttributes();

			boolean hasQoSConstraints = qoSInformation != null && qoSInformation.getQoSConstraints() != null && !qoSInformation.getQoSConstraints().isEmpty();

			ProcessStructureParser parser = new ProcessStructureParser(process);

			final List<String> qoSConstraintsKeys = qoSInformation.getQoSConstraintsKeys();

			Map<String, QoSAttribute> qoSAttributes = Maps.filterKeys(enabledQoSAttributes, new Predicate<String>() {
				@Override
				public boolean apply(String qoSKey) {
					return qoSConstraintsKeys.contains(qoSKey);
				}
			});

			ProcessQoSAggregator agg = new ProcessQoSAggregator(enabledQoSAttributes, qoSInformation);

			if (hasQoSConstraints) {
				// TODO: parametrizar
				List<Integer> numberOfLevels = Arrays.asList(320);
				fillQoSLevelsForProcess(parser, numberOfLevels, qoSAttributes, qoSInformation.getQoSWeights());
			}

			VirtualTaskStructure vTask = parser.getVirtualTask(agg);

			Map<String, Double> globalQoSMinValue = new LinkedHashMap<String, Double>();
			Map<String, Double> globalQoSMaxValue = new LinkedHashMap<String, Double>();

			Map<String, Double> globalQoSDelta = calculateGlobalQoSDelta(vTask, agg, enabledQoSAttributes, globalQoSMaxValue, globalQoSMinValue);

			processQoSInfo.setGlobalQoSDelta(globalQoSDelta);
			processQoSInfo.setGlobalQoSMinValue(globalQoSMinValue);
			processQoSInfo.setGlobalQoSMaxValue(globalQoSMaxValue);

			if (hasQoSConstraints) {
				agg.groupQoSLevels(vTask);
				agg.selectQoSLevels(vTask);

				Map<String, List<QoSConstraint>> localQoSConstraints = new LinkedHashMap<String, List<QoSConstraint>>();

				for (TaskStructure t : vTask.getLeafTasks()) {

					String serviceClassification = t.getTaxonomyClassification();

					if (serviceClassification != null) {

						List<QoSLevel> levels = t.getChoosenQoSLevels();

						if (levels == null || levels.isEmpty()) {
							logger.info("Can't determine QoS Levels for task '" + t.getName() + "'");
							return processQoSInfo;
						}

						List<QoSConstraint> qoSConstraints = new ArrayList<QoSConstraint>(qoSAttributes.size());

						for (QoSLevel l : levels) {

							QoSConstraint q = getQoSConstraintFromLevel(l, qoSInformation, qoSAttributes);

							if (q != null) {
								qoSConstraints.add(q);
								logger.info("Local constraint on '" + serviceClassification + "' -> " + q + " (numberOfLevels=" + l.getTotalNumberOfLevels() + ")");
							}
						}
						localQoSConstraints.put(serviceClassification, qoSConstraints);
					}
				}
				processQoSInfo.setLocalQoSConstraints(localQoSConstraints);
			}

			return processQoSInfo;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			if (e instanceof RuntimeException)
				throw e;
			else
				throw new RuntimeException(e);
		}
	}

	private Map<String, List<QoSConstraint>> defineLocalQoSConstraints(DiscoveryRequest request, VirtualTaskStructure vTask, Map<String, QoSAttribute> qoSAttributes) {

		Map<String, List<QoSConstraint>> result = new LinkedHashMap<String, List<QoSConstraint>>();

		for (TaskStructure t : vTask.getLeafTasks()) {

			// System.out.println("Leaf Task: " + t);

			String serviceClassification = t.getTaxonomyClassification();

			if (serviceClassification != null) {

				List<QoSLevel> levels = t.getChoosenQoSLevels();

				if (levels == null || levels.isEmpty()) {
					logger.info("Can't determine QoS Levels for task '" + t.getName() + "'");
					continue;
				}

				DiscoveryRequestItem di = request.getItemByTaxonomyClassification(serviceClassification);

				if (di == null) {
					di = new DiscoveryRequestItem(serviceClassification);
					request.getItens().add(di);
				} else {
					// Remove previous managed constraints
					QoSInformation qoSInformation = di.getQoSInformation();
					if (qoSInformation != null) {
						qoSInformation.removeManagedQoSConstraints();
					}
				}

				QoSInformation qoSInformation = di.getQoSInformation();
				if (qoSInformation == null) {
					qoSInformation = new QoSInformation();
					di.setQoSInformation(qoSInformation);
				}

				for (QoSLevel l : levels) {

					QoSConstraint c = getQoSConstraintFromLevel(l, request.getQoSInformation(), qoSAttributes);

					if (c != null) {
						qoSInformation.addManagedQoSConstraint(c);
						logger.info("Local constraint on '" + serviceClassification + "' -> " + c + " (numberOfLevels=" + l.getTotalNumberOfLevels() + ")");
					}
				}

				result.put(serviceClassification, qoSInformation.getQoSConstraints());

			}
		}

		return result;
	}

	private QoSConstraint getQoSConstraintFromLevel(QoSLevel l, QoSInformation qoSInformation, Map<String, QoSAttribute> qoSAttributes) {

		String qoSKey = l.getQoSKey();

		QoSConstraint constraint = qoSInformation != null ? qoSInformation.getQoSConstraint(qoSKey) : null;

		if (constraint != null) {

			double v = l.getValue();

			QoSAttribute qoSAtt = qoSAttributes.get(qoSKey);

			boolean isNegative = constraint != null && constraint.getComparisionType() != null ? constraint.getComparisionType() == QoSValueComparisionType.LE : qoSAtt.getValueUtilityDirection() == QoSValueUtilityDirection.NEGATIVE;

			return new QoSConstraint(qoSKey, isNegative ? QoSValueComparisionType.LE : QoSValueComparisionType.GE, v);

		} else {
			return null;
		}
	}

	private void fillQoSLevelsForProcess(ProcessStructureParser parser, List<Integer> numberOfLevels, Map<String, QoSAttribute> qoSAttributes, Map<String, Double> qoSWeights) {

		QoSLevelsRequest qoSLevelsRequest = new QoSLevelsRequest();
		qoSLevelsRequest.setQoSWeights(qoSWeights);
		qoSLevelsRequest.setNumbersOfLevels(numberOfLevels);

		qoSLevelsRequest.setQoSKeys(new ArrayList<String>(qoSAttributes.keySet()));

		Map<String, TaskStructure> mapTaxonomyToTask = new HashMap<String, TaskStructure>();

		for (TaskStructure t : parser.getTasks()) {
			String taxonomyClassification = t.getTaxonomyClassification();
			if (taxonomyClassification != null) {
				qoSLevelsRequest.getServiceClassifications().add(taxonomyClassification);
				mapTaxonomyToTask.put(taxonomyClassification, t);
			}
		}

		QoSLevelsResponse qoSLevels = getQoSLevelsInternal(qoSLevelsRequest, qoSAttributes);

		for (Entry<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> e : qoSLevels.getQoSLevelsByServiceClassification().entrySet()) {
			String taxonomyClassification = e.getKey();
			Map<String, NavigableMap<Integer, List<QoSLevel>>> levels = e.getValue();
			TaskStructure t = mapTaxonomyToTask.get(taxonomyClassification);
			t.setQoSLevels(levels);
		}
	}

	@Override
	public ServicesQoSInformation getServicesQoSInformation(List<String> servicesKeys) throws Exception {

		Query luceneQuery = getQueryFromDiscoveryItem(new DiscoveryRequestItem(servicesKeys), false);

		final Cache<String, DiscoveredService> cache = getServicesCache();

		SearchManager searchManager = Search.getSearchManager(cache);
		CacheQuery q = searchManager.getQuery(luceneQuery, IndexedService.class);

		ServicesQoSInformation result = new ServicesQoSInformation();

		ResultIterator it = q.iterator();

		try {
			while (it.hasNext()) {
				DiscoveredService service = (DiscoveredService) it.next();
				result.getServicesQoSInformation().add(new ServiceQoSInformation(service.getServiceKey(), service.getQoSInformation()));
			}
		} finally {
			it.close();
		}

		return result;
	}

	// @Override
	// public List<ServiceQoSInformationItem> getServicesQoSInformation(String serviceClassification, String qoSKey) throws Exception {
	//
	// List<DiscoveredService> services = discoverInternal(new DiscoveryRequest(serviceClassification), null);
	//
	// List<ServiceQoSInformationItem> result = new ArrayList<ServiceQoSInformationItem>(services.size());
	//
	// for (DiscoveredService s : services) {
	//
	// for (ServiceQoSInformationItem q : s.getQoSInformation()) {
	// if (q.getQoSKey().equals(qoSKey))
	// result.add(q);
	// }
	// }
	//
	// Collections.sort(result, new Comparator<ServiceQoSInformationItem>() {
	//
	// @Override
	// public int compare(ServiceQoSInformationItem q1, ServiceQoSInformationItem q2) {
	// return q1.getQoSValue().compareTo(q2.getQoSValue());
	// }
	// });
	//
	// return result;
	// }

	@Override
	public Map<String, Integer> getNumberOfServicesPerServiceClassification() {
		return getMapNumberOfServicesPerServiceClassification(new DiscoveryRequest(getServiceClassifications()), true);
	}

	@SuppressWarnings({ "unchecked" })
	private List<DiscoveredService> discoverInternal(final DiscoveryRequest request, Integer maxResultsPerServiceClass, CompositionParticipants compositionParticipants) throws Exception {
		return (List<DiscoveredService>) discoverOrCountInternal(request, false, maxResultsPerServiceClass, compositionParticipants);
	}

	@SuppressWarnings("unused")
	private Integer countServicesInternal(final DiscoveryRequest request) throws Exception {
		return (Integer) discoverOrCountInternal(request, true, null, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object discoverOrCountInternal(final DiscoveryRequest request, boolean isCount, Integer maxResultsPerServiceClass, CompositionParticipants compositionParticipants) throws Exception {

		try {

			if (!isCount) {

				if (request.getMaxResultsPerServiceClass() == null)
					request.setMaxResultsPerServiceClass(maxResultsPerServiceClass);

				final Map<String, Double> qoSWeights = request.getQoSInformation() != null ? request.getQoSInformation().getQoSWeights() : null;

				final List<DiscoveryCacheQuery> cacheQueries = new ArrayList<DiscoveryCacheQuery>(request.getItens().size());

				// long s = System.currentTimeMillis();
				// final Collection<ServiceProvider> serviceProviders = getServiceProvidersInternal();
				// long d = System.currentTimeMillis() - s;
				// logger.info("Service Providers obtained in " + d + " ms");

				final AtomicBoolean usingParticipants = new AtomicBoolean();

				final Map<String, Set<DiscoveryRequestItem>> groupDiscoveryRequestItemByParticipant = new ConcurrentHashMap<String, Set<DiscoveryRequestItem>>();

				if (request.getItens().size() > 1) {

					Map<String, Double> globalQoSDelta = request.getQoSInformation() != null ? request.getQoSInformation().getGlobalQoSDelta() : null;

					boolean boolUsingParticipants = true;

					for (DiscoveryRequestItem item : request.getItens()) {
						boolUsingParticipants = boolUsingParticipants && item.getParticipantName() != null;
						if (!boolUsingParticipants) {
							break;
						}
					}

					usingParticipants.set(boolUsingParticipants);

					if (usingParticipants.get()) {

						logger.info("DiscoveryRequest has explicit participants definition. Using grouped search by participant");

						groupDiscoveryRequestItemByParticipant.putAll(request.getItens().stream().collect( //
								Collectors.groupingBy(DiscoveryRequestItem::getParticipantName, Collectors.mapping(x -> x, Collectors.toSet()))));

						for (Entry<String, Set<DiscoveryRequestItem>> e : groupDiscoveryRequestItemByParticipant.entrySet()) {
							Set<DiscoveryRequestItem> itens = e.getValue();
							String participantName = e.getKey();
							DiscoveryCacheQuery discoverQuery = getDiscoveryQuery(new DiscoveryRequest(itens, qoSWeights, globalQoSDelta), participantName, isCount, null);
							cacheQueries.add(discoverQuery);
						}

					} else {
						for (DiscoveryRequestItem item : request.getItens()) {

							DiscoveryCacheQuery discoverQuery = getDiscoveryQuery(new DiscoveryRequest(item, qoSWeights, globalQoSDelta), null, isCount, null);

							if (item.getMaxResults() != null) {
								discoverQuery.maxResults(item.getMaxResults());
							} else if (request.getMaxResultsPerServiceClass() != null) {
								discoverQuery.maxResults(request.getMaxResultsPerServiceClass());
							}

							cacheQueries.add(discoverQuery);
						}
					}

				} else {
					DiscoveryCacheQuery discoverQuery = getDiscoveryQuery(request, null, isCount, null);

					DiscoveryRequestItem item = request.getItens().size() > 0 ? request.getItens().get(0) : null;

					if (item != null && item.getMaxResults() != null) {
						discoverQuery.maxResults(item.getMaxResults());
					} else if (request.getMaxResultsPerServiceClass() != null) {
						discoverQuery.maxResults(request.getMaxResultsPerServiceClass());
					}

					cacheQueries.add(discoverQuery);
				}

				final Set<String> qosKeys = getEnabledQoSAttributes().keySet();

				final List<Future<Void>> futures = cacheQueries.size() > 1 ? new ArrayList<Future<Void>>(cacheQueries.size()) : null;

				final List<DiscoveredService> result = cacheQueries.size() > 1 ? Collections.synchronizedList(new LinkedList<DiscoveredService>()) : new LinkedList<DiscoveredService>();

				final ObjectHolder<Boolean> needToSortHolder = new ObjectHolder<Boolean>(false);

				for (final DiscoveryCacheQuery cacheQuery : cacheQueries) {

					Callable<Void> job = () -> {

						try {
							long start = System.currentTimeMillis();

							LinkedHashMap<String, Set<DiscoveredService>> filteredDiscoveredServicesByProvider = new LinkedHashMap<String, Set<DiscoveredService>>();

							Map<String, Map<String, Set<DiscoveredService>>> groupDiscoveredServicesByParticipants = new LinkedHashMap<String, Map<String, Set<DiscoveredService>>>();

							QoSCapableServiceQuery query = (QoSCapableServiceQuery) cacheQuery.getLuceneQuery();

							List<DiscoveredService> services = null;

							if (request.isCheckServicesAvailability()) {
								Query subQuery = query.getSubQuery();
								services = new ArrayList<DiscoveredService>((List) cacheQuery.changeLuceneQuery(new ServicesCheckQuery(subQuery, ResilienceServiceLocator.get())).list());

							} else {
								services = new ArrayList<DiscoveredService>((List) cacheQuery.list());
							}

							final Map<String, Double> docUtility = new HashMap<String, Double>();
							docUtility.putAll(query.getDocUtility());

							String originalQueryStr = query.toString();

							// checkServicesLegacy(request, needToSortHolder, cacheQuery, query, services, docUtility, originalQueryStr);

							for (DiscoveredService discoveredService : services) {

								String participantName = discoveredService.getServiceProvider().getIdentification();

								if (usingParticipants.get()) {

									Map<String, Set<DiscoveredService>> groupByServiceClassification = groupDiscoveredServicesByParticipants.get(participantName);

									if (groupByServiceClassification == null) {
										groupByServiceClassification = new LinkedHashMap<String, Set<DiscoveredService>>();
										groupDiscoveredServicesByParticipants.put(participantName, groupByServiceClassification);
									}

									String serviceClassification = discoveredService.getServiceClassification();

									Set<DiscoveredService> s = groupByServiceClassification.get(serviceClassification);

									if (s == null) {
										s = new TreeSet<DiscoveredService>(new Comparator<DiscoveredService>() {
											@Override
											public int compare(DiscoveredService s1, DiscoveredService s2) {

												Double u1 = docUtility.get(s1.getServiceKey());
												Double u2 = docUtility.get(s2.getServiceKey());

												int c = u2.compareTo(u1);

												if (c == 0) {
													return s1.getServiceEndpointURL().compareTo(s2.getServiceEndpointURL());
												} else {
													return c;
												}
											}
										});

										groupByServiceClassification.put(serviceClassification, s);
									}

									s.add(discoveredService);

								} else {

									Set<DiscoveredService> s = filteredDiscoveredServicesByProvider.get(participantName);

									if (s == null) {
										s = new LinkedHashSet<>();
										filteredDiscoveredServicesByProvider.put(participantName, s);
									}

									s.add(discoveredService);
								}
							}

							if (usingParticipants.get()) {

								next_provider: for (Entry<String, Map<String, Set<DiscoveredService>>> e : groupDiscoveredServicesByParticipants.entrySet()) {

									String providerName = e.getKey();

									Map<String, Set<DiscoveredService>> groupByServiceClassification = e.getValue();

									Set<DiscoveredService> providerServices = new LinkedHashSet<DiscoveredService>();

									for (DiscoveryRequestItem i : cacheQuery.getItens()) {
										Set<DiscoveredService> s = groupByServiceClassification.get(i.getServiceClassification());
										if (s != null && !s.isEmpty()) {
											providerServices.addAll(s);
										} else {
											logger.trace("Provider " + providerName + " doesn't have any matching services of type " + i.getServiceClassification());
											continue next_provider;
										}
									}

									logger.debug("Provider " + providerName + " has " + providerServices.size() + " matching services");

									filteredDiscoveredServicesByProvider.put(providerName, providerServices);
								}

								// TODO: improve provider services pruning and ranking

								List<ServiceProvider> sortedServiceProviders = new SortedList<ServiceProvider>(new BasicEventList<ServiceProvider>(filteredDiscoveredServicesByProvider.size()), //
										new Comparator<ServiceProvider>() {
											@Override
											public int compare(ServiceProvider p1, ServiceProvider p2) {
												return p2.getUtility().compareTo(p1.getUtility());
											}
										} //
								);

								for (Entry<String, Set<DiscoveredService>> e : filteredDiscoveredServicesByProvider.entrySet()) {

									String serviceIdentification = e.getKey();

									double utility = 0;

									ServiceProvider serviceProvider = new ServiceProvider(serviceIdentification);

									for (DiscoveredService s : e.getValue()) {
										s.setServiceProvider(serviceProvider);
										utility += docUtility.get(s.getServiceKey());
									}

									// Mean
									utility = utility / e.getValue().size();

									serviceProvider.setName(serviceIdentification);
									serviceProvider.setUtility(utility);

									sortedServiceProviders.add(serviceProvider);

								}

								String participantName = cacheQuery.getParticipantName();

								int maxProvidersForParticipantName = request.getMaxCandidatesFromParticipantName(participantName);

								int i = 1;

								// Pruning by the n best providers
								for (ServiceProvider serviceProvider : sortedServiceProviders) {
									if (i++ > maxProvidersForParticipantName) {
										filteredDiscoveredServicesByProvider.remove(serviceProvider.getIdentification());
									}
								}

								for (Entry<String, Set<DiscoveredService>> entry : filteredDiscoveredServicesByProvider.entrySet()) {

									String providerName = entry.getKey();

									Set<DiscoveredService> providerServices = entry.getValue();

									CompositionParticipantProvider cpp = null;

									if (compositionParticipants != null) {
										cpp = new CompositionParticipantProvider(participantName, providerName);
										CompositionParticipant compositionParticipant = compositionParticipants.getParticipant(participantName);
										compositionParticipant.addProvider(cpp);
									}

									Map<String, Set<DiscoveredService>> groupByServiceClassification = new LinkedHashMap<String, Set<DiscoveredService>>();

									for (DiscoveredService service : providerServices) {

										Set<DiscoveredService> s = groupByServiceClassification.get(service.getServiceClassification());

										if (s == null) {
											s = new LinkedHashSet<DiscoveredService>();
											groupByServiceClassification.put(service.getServiceClassification(), s);
										}
										s.add(service);
									}

									for (Entry<String, Set<DiscoveredService>> e : groupByServiceClassification.entrySet()) {

										String serviceClassification = e.getKey();

										DiscoveryRequestItem item = request.getItemByTaxonomyClassification(serviceClassification);

										int m = item.getMaxResults() != null ? item.getMaxResults() : 5;

										int w = 1;

										for (DiscoveredService s : e.getValue()) {
											if (w++ > m) {
												providerServices.remove(s);
											} else if (cpp != null) {
												cpp.addService(s);
											}
										}
									}

								}
							}

							for (Set<DiscoveredService> providerServices : filteredDiscoveredServicesByProvider.values()) {

								for (DiscoveredService s : providerServices) {

									ListIterator<ServiceQoSInformationItem> it = s.getQoSInformation().listIterator();

									while (it.hasNext()) {
										if (!qosKeys.contains(it.next().getQoSKey()))
											it.remove();
									}

									Double utility = docUtility.get(s.getServiceKey());

									s.setUtility(utility);
									result.add(s);
								}
							}

							long d = System.currentTimeMillis() - start;

							logger.info("Discovery returned " + services.size() + " results of a max of " + Math.max(services.size(), cacheQuery.getMaxResults()) + " in " + d + " ms for query: " + originalQueryStr);

							return null;

						} catch (Exception e) {
							logger.error(e.getMessage(), e);
							throw e;
						}
					};

					if (cacheQueries.size() > 1)
						futures.add(pool.submit(job));
					else
						job.call();
				}

				if (futures != null)

				{
					for (Future<Void> f : futures) {
						f.get();
					}
				}

				if (needToSortHolder.get()) {
					Collections.sort(result, new Comparator<DiscoveredService>() {
						@Override
						public int compare(DiscoveredService s1, DiscoveredService s2) {
							return s2.getUtility().compareTo(s1.getUtility());
						}
					});
				}

				return result;

			} else {

				DiscoveryCacheQuery cacheQuery = getDiscoveryQuery(request, null, isCount, null);

				return cacheQuery.getResultSize();
			}

		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
	private void checkServicesLegacy(final DiscoveryRequest request, final ObjectHolder<Boolean> needToSortHolder, final DiscoveryCacheQuery cacheQuery, QoSCapableServiceQuery query, List<DiscoveredService> services, final Map<String, Double> docUtility, String originalQueryStr) {

		int n = services.size();

		if (request.isCheckServicesAvailability() && services.size() > 0) {

			final ObjectHolder<ResilienceService> resilienceServiceHolder = new ObjectHolder<ResilienceService>();

			ResilienceService resilienceService = resilienceServiceHolder.get();

			if (resilienceService == null) {
				synchronized (resilienceServiceHolder) {
					resilienceService = resilienceServiceHolder.get();
					if (resilienceService == null) {
						resilienceService = ResilienceServiceLocator.get();
						resilienceServiceHolder.set(resilienceService);
					}
				}
			}

			boolean done = false;

			List<DiscoveredService> servicesToCheck = new LinkedList<DiscoveredService>(services);
			services.clear();

			while (!done) {

				if (logger.isInfoEnabled())
					logger.info("Checking availability of " + servicesToCheck.size() + " service(s) for query: " + originalQueryStr);

				ServicesCheckResult checkResult = resilienceService.checkServicesAvailable(servicesToCheck.stream().map(s -> DiscoveryUtil.toServiceEndpointInfo(s)).collect(Collectors.toList()));

				if (!checkResult.getUnavailableServices().isEmpty()) {

					needToSortHolder.set(true);

					Collection<String> unavailableServiceKeys = new LinkedHashSet<String>();

					Collection<String> unavailableServicesHostPort = new LinkedHashSet<String>();

					for (DiscoveredService s : servicesToCheck) {

						String serviceKey = s.getServiceKey();

						if (!checkResult.isServiceAvailable(serviceKey)) {
							unavailableServiceKeys.add(serviceKey);
							unavailableServicesHostPort.add(s.getServiceEndpointHostPort());
						} else {
							services.add(s);
						}
					}

					if (logger.isInfoEnabled()) {
						StringBuilder sb = new StringBuilder("There are " + unavailableServiceKeys.size() + " unvailable service(s) in result of query: " + originalQueryStr + ":");
						for (String s : unavailableServiceKeys) {
							sb.append("\n\t" + s);
						}
						logger.info(sb.toString());
					}

					// Trying to find other services (excluding from query the unavailable ones)
					Query subQuery = query.getSubQuery();

					org.apache.lucene.search.BooleanQuery.Builder b = new BooleanQuery.Builder();

					if (subQuery instanceof BooleanQuery) {
						for (BooleanClause c : ((BooleanQuery) subQuery).clauses()) {
							b.add(c);
						}
					} else {
						b.add(subQuery, Occur.MUST);
					}

					for (String serviceKey : unavailableServiceKeys) {
						b.add(new TermQuery(new Term("serviceKey", serviceKey)), Occur.MUST_NOT);
					}

					for (String serviceHostPort : unavailableServicesHostPort) {
						b.add(new TermQuery(new Term("serviceEndpointHostPort", serviceHostPort)), Occur.MUST_NOT);
					}

					query = query.changeSubQuery(b.build());

					DiscoveryCacheQuery discoveryCacheQuery = cacheQuery.changeLuceneQuery(query);
					discoveryCacheQuery.maxResults(n - services.size());

					List<DiscoveredService> discoveredServices = (List) discoveryCacheQuery.list();

					if (logger.isInfoEnabled()) {
						StringBuilder sb = new StringBuilder("Returning " + discoveredServices.size() + " service(s) in result of query: " + discoveryCacheQuery + ":");
						for (DiscoveredService s : discoveredServices) {
							sb.append("\n\t" + s.getServiceKey());
						}
						logger.info(sb.toString());
					}

					docUtility.putAll(query.getDocUtility());

					// We can't find other services, stopping....
					if (discoveredServices.isEmpty()) {
						done = true;

						// Continue to check the discovered services
					} else {
						servicesToCheck.clear();
						servicesToCheck.addAll(discoveredServices);
					}
					// All services OK, we can stop
				} else {
					services.addAll(servicesToCheck);
					done = true;
				}
			}

			n = services.size();
		}
	}

	private Map<String, Integer> getMapNumberOfServicesPerServiceClassification(DiscoveryRequest request, boolean filterOnlyByServiceClassification) {

		Map<String, Integer> m = new TreeMap<String, Integer>();

		final Cache<String, DiscoveredService> cache = getServicesCache();

		SearchManager searchManager = Search.getSearchManager(cache);

		for (DiscoveryRequestItem item : request.getItens()) {
			Query q = getQueryFromDiscoveryItem(item, filterOnlyByServiceClassification);
			m.put(item.getServiceClassification(), searchManager.getQuery(q, IndexedService.class).getResultSize());
		}
		return m;
	}

	private DiscoveryCacheQuery getDiscoveryQuery(final DiscoveryRequest request, final String participantName, boolean isCount, String[] projections) throws Exception {

		try {

			final Cache<String, DiscoveredService> cache = getServicesCache();

			final Map<String, QoSAttribute> qoSAttributes = getQoSAttributes();

			SearchManager searchManager = Search.getSearchManager(cache);

			org.apache.lucene.search.BooleanQuery.Builder b = new BooleanQuery.Builder();

			List<DiscoveryRequestItem> discoveryRequestItens = request.getItens();

			for (DiscoveryRequestItem i : discoveryRequestItens) {
				Query q = getQueryFromDiscoveryItem(i, false);
				b.add(q, Occur.SHOULD);
			}

			Query q = b.build();

			if (!isCount) {

				Map<String, Double> qoSWeights = request.getQoSInformation() != null ? request.getQoSInformation().getQoSWeights() : null;
				Map<String, Double> globalQoSDelta = request.getQoSInformation() != null ? request.getQoSInformation().getGlobalQoSDelta() : null;

				Set<String> servicesClassifications = new HashSet<String>(discoveryRequestItens.size());

				for (DiscoveryRequestItem item : discoveryRequestItens) {
					servicesClassifications.add(item.getServiceClassification());
				}

				QoSMinMax qoSMinMax = getQoSMinMax(servicesClassifications, globalQoSDelta);

				q = new QoSCapableServiceQuery(q, qoSMinMax, qoSWeights, cache, qoSAttributes);
			}

			return new DiscoveryCacheQuery(searchManager, q, projections, discoveryRequestItens, participantName);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private Query getQueryFromDiscoveryItem(DiscoveryRequestItem i, boolean filterOnlyByServiceClassification) {

		final boolean containsServiceProvider = !StringUtils.isEmpty(i.getServiceProvider());
		final boolean containsServiceClassification = !StringUtils.isEmpty(i.getServiceClassification());
		final boolean containsServiceEndpointURL = !StringUtils.isEmpty(i.getServiceEndpointURL());

		final boolean containsQoS = i.getQoSInformation() != null && i.getQoSInformation().getQoSConstraints() != null && !i.getQoSInformation().getQoSConstraints().isEmpty();

		final boolean containsServicesKeys = i.getServiceKeys() != null && !i.getServiceKeys().isEmpty();
		final boolean containsExcludeServicesKeys = i.getExcludeServiceKeys() != null && !i.getExcludeServiceKeys().isEmpty();

		final boolean containsExcludeServicesEndpointsHostPort = i.getExcludeServiceEndpointsHostPort() != null && !i.getExcludeServiceEndpointsHostPort().isEmpty();

		if (!containsServiceClassification && !containsServiceEndpointURL && !containsQoS && !containsServicesKeys && !containsExcludeServicesKeys && !containsExcludeServicesEndpointsHostPort)
			return new MatchAllDocsQuery();

		org.apache.lucene.search.BooleanQuery.Builder b = new BooleanQuery.Builder();

		if (containsServiceProvider)
			b.add(new TermQuery(new Term("serviceProvider.identification", i.getServiceProvider())), Occur.MUST);

		if (containsServiceClassification)
			b.add(new TermQuery(new Term("serviceClassification", i.getServiceClassification())), Occur.MUST);

		if (containsServiceEndpointURL)
			b.add(new TermQuery(new Term("serviceEndpointURL", i.getServiceEndpointURL())), Occur.MUST);

		if (!filterOnlyByServiceClassification) {
			if (containsQoS) {
				b.add(getQoSConstraintsQuery(i.getQoSInformation().getQoSConstraints()), Occur.MUST);
			}
		}

		if (containsServicesKeys) {
			for (String serviceKey : i.getServiceKeys()) {
				b.add(new TermQuery(new Term("serviceKey", serviceKey)), Occur.SHOULD);
			}
		}

		if (containsExcludeServicesKeys) {
			for (String serviceKey : i.getExcludeServiceKeys()) {
				b.add(new TermQuery(new Term("serviceKey", serviceKey)), Occur.MUST_NOT);
			}
		}

		if (containsExcludeServicesEndpointsHostPort) {
			for (String serviceEndpointHostPort : i.getExcludeServiceEndpointsHostPort()) {
				b.add(new TermQuery(new Term("serviceEndpointHostPort", serviceEndpointHostPort)), Occur.MUST_NOT);
			}
		}

		BooleanQuery q = b.build();

		List<BooleanClause> clauses = q.clauses();

		if (clauses.size() == 1)
			return clauses.get(0).getQuery();
		else
			return q;
	}

	private Query getQoSConstraintsQuery(List<QoSConstraint> list) {

		org.apache.lucene.search.BooleanQuery.Builder b1 = new BooleanQuery.Builder();

		for (QoSConstraint q : list) {

			String field = "QoS." + q.getQoSKey();
			Double value = q.getQoSValue();

			if (q.getComparisionType() == QoSValueComparisionType.LE) {
				b1.add(NumericRangeQuery.newDoubleRange(field, null, value, false, true), Occur.FILTER);
			} else if (q.getComparisionType() == QoSValueComparisionType.GE) {
				b1.add(NumericRangeQuery.newDoubleRange(field, value, null, true, false), Occur.FILTER);
			}
			// else if (q.getComparisionType().equals("eq")) {
			// b1.add(NumericRangeQuery.newDoubleRange(field, value, value, true, true), Occur.FILTER);
			// } else if (q.getComparisionType().equals("lt")) {
			// b1.add(NumericRangeQuery.newDoubleRange(field, null, value, false, false), Occur.FILTER);
			// } else if (q.getComparisionType().equals("gt")) {
			// b1.add(NumericRangeQuery.newDoubleRange(field, value, null, false, false), Occur.FILTER);
			// }
		}

		return b1.build();
	}

	private QoSMinMax getQoSMinMax(final Collection<String> serviceClassifications, final Map<String, Double> globalQoSDelta) {

		return new QoSMinMax() {

			Map<String, Map<String, Double>> qoSMinValue = new HashMap<String, Map<String, Double>>();
			Map<String, Map<String, Double>> qoSMaxValue = new HashMap<String, Map<String, Double>>();

			{
				for (String serviceClassification : serviceClassifications) {
					ServiceClassificationInfo serviceClassificationInfo = getServiceClassificationInfo(serviceClassification);
					if (serviceClassificationInfo != null) {
						qoSMinValue.put(serviceClassification, serviceClassificationInfo.getMinQoSValue());
						qoSMaxValue.put(serviceClassification, serviceClassificationInfo.getMaxQoSValue());
					}
				}
			}

			@Override
			public Double getQoSMinValue(String serviceClassification, String qoSKey) {
				Map<String, Double> map = qoSMinValue.get(serviceClassification);
				if (map == null)
					return null;
				return map.get(qoSKey);
			}

			@Override
			public Double getQoSMaxValue(String serviceClassification, String qoSKey) {
				Map<String, Double> map = qoSMaxValue.get(serviceClassification);
				if (map == null)
					return null;
				return map.get(qoSKey);
			}

			@Override
			public Double getGlobalQoSDeltaValue(String qoSKey) {
				if (globalQoSDelta != null)
					return globalQoSDelta.get(qoSKey);
				else
					return null;
			}
		};
	}

	private ServiceClassificationInfo getServiceClassificationInfo(String serviceClassification) {
		if (serviceClassification == null)
			return null;
		else
			return (ServiceClassificationInfo) getCacheServiceClassification().get(serviceClassification);
	}

	private QoSLevels getQoSLevels(String serviceClassification) {
		if (serviceClassification == null)
			return null;
		else
			return (QoSLevels) getCacheQoSLevels().get(serviceClassification);
	}

	// private void filterServiceList(List<DiscoveredService> candidateServices, List<DiscoveredService> resultMatchingServices,
	// List<DiscoveredService> resultNonMatchingServices, QoSInformation qoSInformation) {
	//
	// // If there aren't QoS Constraints, all the candidates matches the
	// // constraints
	// if (qoSInformation == null || qoSInformation.getQoSConstraint() == null || qoSInformation.getQoSConstraint().size() == 0) {
	// synchronized (resultMatchingServices) {
	// resultMatchingServices.addAll(candidateServices);
	// }
	// } else {
	//
	// for (DiscoveredService service : candidateServices) {
	//
	// if (isServiceMatchedQoS(service.getQoSInformation(), qoSInformation)) {
	// synchronized (resultMatchingServices) {
	// resultMatchingServices.add(service);
	// }
	// }
	// }
	// }
	//
	// }
	//
	// private boolean isServiceMatchedQoS(List<ServiceQoSInformationItem> serviceQoS, QoSInformation requiredQoS) {
	//
	// Map<String, ServiceQoSInformationItem> mapServiceQoS = new HashMap<String, ServiceQoSInformationItem>();
	//
	// for (ServiceQoSInformationItem sqi : serviceQoS)
	// mapServiceQoS.put(sqi.getQoSKey(), sqi);
	//
	// for (QoSConstraint qoSConstraint : requiredQoS.getQoSConstraint()) {
	// ServiceQoSInformationItem serviceQoSInformationItem = mapServiceQoS.get(qoSConstraint.getQoSKey());
	//
	// if (serviceQoSInformationItem == null)
	// return false;
	//
	// if (serviceQoSInformationItem.getQoSValue() != null && qoSConstraint.getComparisionType() != null && qoSConstraint.getQoSValue() !=
	// null) {
	//
	// // Comparision Type = equals
	// if (qoSConstraint.getComparisionType().equals("eq"))
	// if (!(serviceQoSInformationItem.getQoSValue().compareTo(qoSConstraint.getQoSValue()) == 0))
	// return false;
	//
	// // Comparision Type = lower than
	// if (qoSConstraint.getComparisionType().equals("lt"))
	// if (!(serviceQoSInformationItem.getQoSValue().compareTo(qoSConstraint.getQoSValue()) < 0))
	// return false;
	//
	// // Comparision Type = lower equal than
	// if (qoSConstraint.getComparisionType().equals("le"))
	// if (!(serviceQoSInformationItem.getQoSValue().compareTo(qoSConstraint.getQoSValue()) <= 0))
	// return false;
	//
	// // Comparision Type = greater than
	// if (qoSConstraint.getComparisionType().equals("gt"))
	// if (!(serviceQoSInformationItem.getQoSValue().compareTo(qoSConstraint.getQoSValue()) > 0))
	// return false;
	//
	// // Comparision Type = greater equal than
	// if (qoSConstraint.getComparisionType().equals("ge"))
	// if (!(serviceQoSInformationItem.getQoSValue().compareTo(qoSConstraint.getQoSValue()) >= 0))
	// return false;
	//
	// }
	//
	// }
	//
	// return true;
	//
	// }

	private boolean isServiceAlive(String urlStr) {

		Socket socket = null;

		try {
			URL url = new URL(urlStr);
			logger.debug("isServiceAlive(" + urlStr + ")");

			socket = new Socket(url.getHost(), url.getPort());
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
				}
		}
	}

	public static void main(String[] args) throws Exception {

		// System.out.println(getQoSLevelsInterval(10, 160, 2));

		// QoSInformation qoSInformation = new QoSInformation();
		// qoSInformation.getQoSConstraints().add(new QoSConstraint("Acessibility", "Acessibility", QoSValueComparisionType.GE, 46));

		// System.out.println(new
		// DiscoveryServiceImpl().discoverUniqueServiceEndpoint("ubl/ordering/orderingprocess/buyerParty/acceptOrder",
		// qoSInformation));

		// DiscoveryResult discoveryResult = new
		// DiscoveryServiceImpl().discoverServices("ubl/ordering/orderingprocess/buyerParty/acceptOrder", qoSInformation);

		// System.out.println("Matching Services");
		// for (DiscoveredService discoveredService : discoveryResult.getMatchingServices()) {
		// System.out.println("\t" + discoveredService);
		// }

	}
}
