package br.ufsc.gsigma.services.execution.bpel.impl.infinispan;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.il.dbutil.DatabaseConfigException;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.spi.IndexedTypeIdentifier;
import org.hibernate.search.spi.SearchIntegrator;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.Index;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.global.TransportConfigurationBuilder;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.entries.VersionedRepeatableReadEntry;
import org.infinispan.container.versioning.EntryVersion;
import org.infinispan.container.versioning.NumericVersion;
import org.infinispan.container.versioning.SimpleClusteredVersion;
import org.infinispan.context.Flag;
import org.infinispan.counter.EmbeddedCounterManagerFactory;
import org.infinispan.counter.api.CounterConfiguration;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.CounterType;
import org.infinispan.counter.api.Storage;
import org.infinispan.counter.api.StrongCounter;
import org.infinispan.encoding.DataConversion;
import org.infinispan.filter.AcceptAllKeyValueFilter;
import org.infinispan.filter.CacheFilters;
import org.infinispan.filter.KeyValueFilter;
import org.infinispan.lock.EmbeddedClusteredLockManagerFactory;
import org.infinispan.lock.api.ClusteredLock;
import org.infinispan.lock.api.ClusteredLockManager;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.infinispan.query.impl.ComponentRegistryUtils;
import org.infinispan.query.impl.massindex.IndexUpdater;
import org.infinispan.remoting.transport.Address;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.TransactionManagerLookup;
import org.infinispan.util.concurrent.IsolationLevel;

import br.ufsc.gsigma.infrastructure.util.ZipUtils;
import br.ufsc.gsigma.infrastructure.util.thread.ThreadLocalHolder;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionFlags;
import br.ufsc.gsigma.services.execution.bpel.impl.ExecutionBpelServer;
import br.ufsc.gsigma.services.execution.bpel.impl.ExecutionODEServer;
import br.ufsc.gsigma.services.execution.bpel.impl.ExecutionProcessStore;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.binding.InfinispanBindingConfigurationHolder;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel.InfinispanBpelDAOConnectionFactory;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel.InfinispanCorrelatorDAO;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel.InfinispanEventDAO;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel.InfinispanMessageExchangeDAO;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel.InfinispanMessageRouteDAO;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel.InfinispanProcessDAO;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel.InfinispanProcessInstanceDAO;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel.InfinispanScopeDAO;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.scheduler.InfinispanSchedulerJob;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.store.InfinispanDeploymentUnitDAO;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.store.InfinispanVersionTrackerDAO;
import br.ufsc.gsigma.services.execution.bpel.ode.DeploymentPoller;

@Listener(clustered = true)
public class InfinispanDatabase extends Database {

	private static final Logger logger = Logger.getLogger(InfinispanDatabase.class);

	private static final File DATA_DIR = new File(System.getProperty("data.dir", "db/execution"));

	private static final File CACHE_DIR = new File(DATA_DIR, "cache/db");

	private static final File INDEX_DIR = new File(DATA_DIR, "cache/indexes");

	private EmbeddedCacheManager cacheManager;

	private TransactionManager transactionManager;

	// private ExecutionBpelServer bpelServer;

	private ExecutionODEServer odeServer;

	private AtomicBoolean leader = new AtomicBoolean();

	private static InfinispanDatabase INSTANCE;

	public static InfinispanDatabase getInstance() {
		return INSTANCE;
	}

	public InfinispanDatabase(OdeConfigProperties props, ExecutionBpelServer bpelServer, ExecutionODEServer odeServer) {
		super(props);
		INSTANCE = this;

		GlobalConfigurationBuilder gcb = new GlobalConfigurationBuilder();

		TransportConfigurationBuilder tcb = gcb.transport() //
				.defaultTransport(); //

		tcb.clusterName("execution-service-cluster");

		tcb.addProperty("configurationFile", "br/ufsc/gsigma/infrastructure/jgroups/jgroups.xml");

		gcb.globalState()//
				.enable() //
				.sharedPersistentLocation(new File(CACHE_DIR, "state/shared").getAbsolutePath()) //
				.persistentLocation(new File(CACHE_DIR, "state/persistent").getAbsolutePath()) //
				.temporaryLocation(new File(CACHE_DIR, "state/temp").getAbsolutePath());

		this.cacheManager = new DefaultCacheManager(gcb.build());
		this.cacheManager.addListener(this);

		this.odeServer = odeServer;

		checkLeader(getClusterMembers(), getMyAddress());
	}

	private void loadInfinispan() {
		getCache(InfinispanCorrelatorDAO.class);
		getCache(InfinispanDeploymentUnitDAO.class);
		getCache(InfinispanEventDAO.class);
		getCache(InfinispanMessageExchangeDAO.class);
		getCache(InfinispanMessageRouteDAO.class);
		getCache(InfinispanProcessDAO.class);
		getCache(InfinispanProcessInstanceDAO.class);
		getCache(InfinispanSchedulerJob.class);
		getCache(InfinispanScopeDAO.class);
		getCache(InfinispanVersionTrackerDAO.class);
		getCache(InfinispanBindingConfigurationHolder.class);
	}

	@ViewChanged
	public void viewChanged(ViewChangedEvent event) {
		logger.info("Cluster view changed --> " + event.getNewMembers());
		checkLeader(event.getNewMembers(), event.getLocalAddress());
	}

	private void checkLeader(List<Address> members, Address myAddress) {
		if (members.get(0).equals(myAddress)) {
			logger.info("Considering node as leader");
			this.leader.set(true);
		} else {
			this.leader.set(false);
		}
	}

	public boolean isLeader() {
		return leader.get();
	}

	public List<Address> getClusterMembers() {
		return cacheManager.getTransport().getMembers();
	}

	public Address getMyAddress() {
		return cacheManager.getTransport().getAddress();
	}

	@CacheEntryCreated
	public void cacheEntryCreated(CacheEntryCreatedEvent<?, ?> event) {

		if (!event.isOriginLocal()) {

			String cacheName = event.getCache().getName();

			logger.debug("[" + cacheName.substring(cacheName.lastIndexOf('.') + 1) + "] Entry created --> " + event.getKey() + ":" + event.getValue());

			if (cacheName.equals(InfinispanDeploymentUnitDAO.class.getName())) {

				InfinispanDeploymentUnitDAO du = (InfinispanDeploymentUnitDAO) event.getValue();

				installDeploymentUnit(du);
			}
		}
	}

	private void installDeploymentUnit(InfinispanDeploymentUnitDAO du) {

		DeploymentPoller poller = odeServer.getDeploymentPoller();

		ExecutionProcessStore processStore = (ExecutionProcessStore) odeServer.getProcessStore();

		ExecutionContext executionContext = du.getExecutionContext();

		try {

			if (executionContext != null)
				executionContext.set();

			ThreadLocalHolder.initThreadLocal();

			ThreadLocalHolder.getThreadLocalMap().put(ExecutionFlags.FLAG_IGNORE_EVENT_PUBLISHING, true);

			poller.hold();

			File deploymentDir = new File(processStore.getDeployDir(), new File(du.getDeploymentUnitDir()).getName());

			if (!deploymentDir.exists()) {
				deploymentDir.mkdirs();
			}

			ZipUtils.unzip(du.getDeploymentUnitBinary(), deploymentDir);

			File deployedMarker = new File(deploymentDir.getParent(), deploymentDir.getName() + ".deployed");

			deployedMarker.createNewFile();

			poller.markAsDeployed(deploymentDir);

			processStore.loadDeploymentUnit(du);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
		} finally {
			poller.release();

			ThreadLocalHolder.clearThreadLocal();

			if (executionContext != null)
				executionContext.remove();
		}
	}

	public static String getProcessInstanceLockKey(String processNS, String processName, String processInstanceId) {
		return "{" + processNS + "}" + processName + "@" + processInstanceId;
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		super.setTransactionManager(transactionManager);
		this.transactionManager = transactionManager;
		loadInfinispan();
	}

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	@Override
	public synchronized void start() throws DatabaseConfigException {
		logger.info("Starting Infinispan");

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void addCacheEntry(InfinispanObject<? extends Serializable> entry) {

		Cache cache = getInstance().getCache(entry.getClass());

		if (cache != null) {
			cache.put(entry.getId(), entry);
		}
	}

	public static <K extends Serializable, V extends InfinispanObject<K>> List<V> delete(Class<V> clazz, Term... terms) {
		List<V> list = query(clazz, getQueriesFromTerms(terms));
		for (V obj : list) {
			removeCacheEntry(obj);
		}
		return list;
	}

	public static <K extends Serializable, V extends InfinispanObject<K>> V querySingleResult(Class<V> clazz, Term... terms) {
		List<V> l = query(clazz, terms);

		if (l.size() > 1) {
			logger.warn("More than one result for query with terms: " + ArrayUtils.toString(terms));
		}

		return !l.isEmpty() ? (V) l.get(0) : null;
	}

	public static <K extends Serializable, V extends InfinispanObject<K>> List<V> query(Class<V> clazz, Term... terms) {
		return query(clazz, getQueriesFromTerms(terms));
	}

	public static <K extends Serializable, V extends InfinispanObject<K>> int count(Class<V> clazz, Term... terms) {
		return count(clazz, getQueriesFromTerms(terms));
	}

	private static Query[] getQueriesFromTerms(Term... terms) {
		Query[] queries = new Query[terms.length];

		for (int i = 0; i < terms.length; i++) {
			queries[i] = new TermQuery(terms[i]);
		}
		return queries;
	}

	public static <K extends Serializable, V extends InfinispanObject<K>> V querySingleResult(Class<V> clazz, Query... queries) {

		List<V> l = query(clazz, queries);

		if (l.size() > 1) {
			logger.warn("More than one result for query with queries: " + ArrayUtils.toString(queries));
		}

		return !l.isEmpty() ? (V) l.get(0) : null;
	}

	@SuppressWarnings({ "unchecked" })
	public static <K extends Serializable, V extends InfinispanObject<K>> List<V> query(Class<V> clazz, Query... queries) {
		return (List<V>) queryOrCountInternal(false, clazz, queries);
	}

	public static <K extends Serializable, V extends InfinispanObject<K>> int count(Class<V> clazz, Query... queries) {
		return (Integer) queryOrCountInternal(true, clazz, queries);
	}

	@SuppressWarnings("rawtypes")
	private static <K extends Serializable, V extends InfinispanObject<K>> Object queryOrCountInternal(boolean isCount, Class<V> clazz, Query... queries) {

		Cache<K, V> cache = getInstance().getCache(clazz);

		if (cache != null) {

			SearchManager searchManager = Search.getSearchManager(cache);

			Query luceneQuery = null;

			if (queries == null || queries.length == 0) {
				luceneQuery = new MatchAllDocsQuery();
			} else if (queries.length == 1) {
				luceneQuery = queries[0];
			} else {
				org.apache.lucene.search.BooleanQuery.Builder b = new BooleanQuery.Builder();
				for (Query q : queries) {
					b.add(q, Occur.MUST);
				}
				luceneQuery = b.build();
			}

			CacheQuery q = searchManager.getQuery(luceneQuery, clazz);

			Object result = isCount ? q.getResultSize() : q.list();

			if (logger.isTraceEnabled()) {
				int n = isCount ? (int) result : ((List) result).size();
				logger.trace("Query '" + luceneQuery + "' on '" + clazz.getName() + "' returned " + n + (n > 1 ? "entries" : "entry"));
			}

			return result;

		} else {
			return null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean updateCacheEntry(InfinispanObject<? extends Serializable> entry) {

		Cache cache = getInstance().getCache(entry.getClass());

		if (cache != null) {

			boolean r = cache.replace(entry.getId(), entry) != null;

			if (r && logger.isDebugEnabled()) {

				Long version = getCacheEntryVersion(entry);

				logger.debug("Updating entry '" + entry.getId() + "'" + (version != null ? " at version=" + version : "") + " on cache " + cache.getName());
			}

			return r;
		} else {
			return false;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Long getCacheEntryVersion(InfinispanObject<? extends Serializable> entry) {

		Long version = null;

		Cache cache = getInstance().getCache(entry.getClass());

		if (cache != null) {

			CacheEntry cacheEntry = cache.getAdvancedCache().getCacheEntry(entry.getId());

			if (cacheEntry instanceof VersionedRepeatableReadEntry) {
				EntryVersion v = ((VersionedRepeatableReadEntry) cacheEntry).getVersion();

				if (v instanceof NumericVersion) {
					version = ((NumericVersion) v).getVersion();

				} else if (v instanceof SimpleClusteredVersion) {
					version = ((SimpleClusteredVersion) v).getVersion();
				}

			}
		}
		return version;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean removeCacheEntry(InfinispanObject<? extends Serializable> entry) {
		Cache cache = entry != null ? getInstance().getCache(entry.getClass()) : null;
		if (cache != null) {
			return cache.remove(entry.getId()) != null;
		} else {
			return false;
		}
	}

	public static StrongCounter getCounter(String name) {

		CounterManager counterManager = EmbeddedCounterManagerFactory.asCounterManager(getInstance().cacheManager);

		if (!counterManager.isDefined(name)) {

			CounterConfiguration cfg = CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG) //
					.storage(Storage.PERSISTENT).build();

			counterManager.defineCounter(name, cfg);
		}

		return counterManager.getStrongCounter(name);
	}

	@SuppressWarnings("unchecked")
	public static <K extends Serializable, V extends InfinispanObject<K>> Collection<V> getCacheEntries(Class<V> clazz) {
		Cache<K, V> cache = getInstance().getCache(clazz);

		if (cache != null) {
			return cache.values();
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public static <K extends Serializable, V extends InfinispanObject<K>> V getCacheEntry(Class<V> clazz, K key) {
		Cache<K, V> cache = getInstance().getCache(clazz);
		if (cache != null) {

			V v = cache.get(key);

			if (v instanceof InfinispanObject && logger.isTraceEnabled()) {
				Long version = getCacheEntryVersion(v);
				logger.trace("Returning entry '" + key + "'" + (version != null ? " at version=" + version : "") + " on cache " + cache.getName());
			}

			return v;

		} else {
			return null;
		}
	}

	public static void defineLock(String name) {
		getLock(name);
	}

	public static ClusteredLock getLock(String name) {

		if (Boolean.valueOf(System.getProperty("useClusterLock", "false"))) {

			InfinispanDatabase instance = getInstance();

			ClusteredLockManager clusteredLockManager = EmbeddedClusteredLockManagerFactory.from(instance.cacheManager);

			if (!clusteredLockManager.isDefined(name)) {
				synchronized (instance) {
					if (!clusteredLockManager.isDefined(name)) {
						boolean b = clusteredLockManager.defineLock(name);

						if (logger.isInfoEnabled() && b) {
							if (b) {
								logger.info("Lock created --> " + name);
							}
						}

					}
				}
			}

			return clusteredLockManager.get(name);

		} else {
			return null;
		}
	}

	public static void removeLock(String name) {

		InfinispanDatabase instance = getInstance();

		ClusteredLockManager clusteredLockManager = EmbeddedClusteredLockManagerFactory.from(instance.cacheManager);

		synchronized (instance) {
			CompletableFuture<Boolean> r = clusteredLockManager.remove(name);

			if (logger.isInfoEnabled()) {

				boolean b = false;

				try {
					b = r.get();
				} catch (Exception e) {
				}

				if (b) {
					logger.info("Lock removed --> " + name);
				}
			}
		}
	}

	public <K extends Serializable, V extends InfinispanObject<K>> Cache<K, V> getCache(Class<V> valueClass) {

		String cacheName = valueClass.getName();

		Cache<K, V> cache = cacheManager.getCache(cacheName, false);

		if (cache == null) {

			synchronized (cacheManager) {

				cache = cacheManager.getCache(cacheName, false);

				if (cache == null) {

					logger.info("Creating cache '" + cacheName + "'");

					ConfigurationBuilder cfg = new ConfigurationBuilder();

					CacheMode replMode = CacheMode.REPL_SYNC;

					cfg.transaction() //
							.transactionMode(TransactionMode.TRANSACTIONAL) //
							// .use1PcForAutoCommitTransactions(true) //
							.transactionManagerLookup(new TransactionManagerLookup() {
								@Override
								public TransactionManager getTransactionManager() throws Exception {
									return transactionManager;
								}
							});

					cfg.locking().isolationLevel(IsolationLevel.READ_COMMITTED);

					// cfg.persistence() //
					// .passivation(false) //
					// .addStore(RocksDBStoreConfigurationBuilder.class) //
					// .location(new File(CACHE_DIR, "data").getAbsolutePath() + "/")//
					// .expiredLocation(new File(CACHE_DIR, "expired").getAbsolutePath() + "/") //
					// .async().enable(); // async

					boolean indexed = valueClass.getAnnotation(Indexed.class) != null;

					if (indexed) {

						//@formatter:off
						boolean syncIndexed = 
										valueClass == InfinispanCorrelatorDAO.class || 
										valueClass == InfinispanMessageRouteDAO.class ;
//										valueClass == InfinispanMessageExchangeDAO.class;
						//@formatter:on

						String indexingMode = syncIndexed ? "sync" : "async";

						cfg.indexing() //
								.index(Index.ALL) //
								.addProperty("hibernate.search.default.directory_provider", "filesystem") //
								.addProperty("hibernate.search." + valueClass.getName() + ".worker.execution", indexingMode) //
								.addProperty("hibernate.search.default.indexBase", INDEX_DIR.getAbsolutePath());

						logger.info("Cache '" + cacheName + "' has indexing enabled in mode '" + indexingMode + "'");

					}

					cfg.clustering() //
							.remoteTimeout(10, TimeUnit.SECONDS) //
							.cacheMode(replMode) //
							.stateTransfer().awaitInitialTransfer(true) //
							.fetchInMemoryState(true);

					cacheManager.defineConfiguration(cacheName, cfg.build());

					cache = cacheManager.getCache(cacheName);

					cache.addListener(this);

					if (indexed) {

						indexCache(cache);

						// Warmup indexing subsystem
						int count = count(valueClass, new MatchAllDocsQuery());
						logger.info("Cache '" + cacheName + "' has " + count + " indexed entries");
					}
				}
			}
		}
		return cache;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void indexCache(Cache cache) {

		SearchIntegrator searchIntegrator = ComponentRegistryUtils.getComponent(cache, SearchIntegrator.class);

		KeyValueFilter filter = AcceptAllKeyValueFilter.getInstance();

		DataConversion valueDataConversion = cache.getAdvancedCache().getValueDataConversion();

		IndexUpdater indexUpdater = new IndexUpdater(cache);

		int count = 0;

		try (Stream<CacheEntry<Object, Object>> stream = cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).cacheEntrySet().stream()) {
			Iterator<CacheEntry<Object, Object>> iterator = stream.filter(CacheFilters.predicate(filter)).iterator();
			while (iterator.hasNext()) {
				CacheEntry<Object, Object> next = iterator.next();
				Object value = valueDataConversion.extractIndexable(next.getValue());
				if (value != null) {
					indexUpdater.updateIndex(next.getKey(), value);
					count++;
				}
			}
		}

		indexUpdater.waitForAsyncCompletion();

		for (IndexedTypeIdentifier indexedType : searchIntegrator.getIndexBindings().keySet()) {
			indexUpdater.flush(indexedType);
		}

		if (count > 0) {
			logger.info("Cache " + cache.getName() + " indexed with " + count + " entries");
		}
	}

	@Override
	public synchronized void shutdown() {
	}

	@Override
	public DataSource getDataSource() {
		return super.getDataSource();
	}

	@Override
	public BpelDAOConnectionFactoryJDBC createDaoCF() throws DatabaseConfigException {
		return InfinispanBpelDAOConnectionFactory.INSTANCE;
	}

}
