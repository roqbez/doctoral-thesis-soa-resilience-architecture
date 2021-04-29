package br.ufsc.gsigma.services.resilience.support;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.global.TransportConfigurationBuilder;
import org.infinispan.lock.EmbeddedClusteredLockManagerFactory;
import org.infinispan.lock.api.ClusteredLock;
import org.infinispan.lock.api.ClusteredLockManager;
import org.infinispan.manager.ClusterExecutor;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.multimap.api.embedded.EmbeddedMultimapCacheManagerFactory;
import org.infinispan.multimap.api.embedded.MultimapCache;
import org.infinispan.multimap.api.embedded.MultimapCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.infinispan.transaction.LockingMode;
import org.infinispan.util.concurrent.IsolationLevel;
import org.springframework.stereotype.Component;

@Component
@Listener(clustered = true)
public class InfinispanSupport {

	private static final Logger logger = Logger.getLogger(InfinispanSupport.class);

	private static final File DATA_DIR = new File(System.getProperty("data.dir", "db/resilience"));

	private static final File CACHE_DIR = new File(DATA_DIR, "cache/db");

	// private static final File INDEX_DIR = new File(DATA_DIR, "cache/indexes");

	// @Autowired
	// private TransactionManager transactionManager;

	private EmbeddedCacheManager cacheManager;

	private Set<String> multimaps = new HashSet<String>();

	private List<ClusterListener> listeners = new LinkedList<ClusterListener>();

	private AtomicBoolean leader = new AtomicBoolean();

	private static InfinispanSupport INSTANCE;

	public static InfinispanSupport getInstance() {
		return INSTANCE;
	}

	@PostConstruct
	public void setup() {

		GlobalConfigurationBuilder gcb = new GlobalConfigurationBuilder();

		TransportConfigurationBuilder tcb = gcb.transport() //
				.defaultTransport(); //

		tcb.clusterName("resilience-service-cluster");

		tcb.addProperty("configurationFile", "br/ufsc/gsigma/infrastructure/jgroups/jgroups.xml");

		gcb.globalState()//
				.enable() //
				.sharedPersistentLocation(new File(CACHE_DIR, "state/shared").getAbsolutePath()) //
				.persistentLocation(new File(CACHE_DIR, "state/persistent").getAbsolutePath()) //
				.temporaryLocation(new File(CACHE_DIR, "state/temp").getAbsolutePath());

		this.cacheManager = new DefaultCacheManager(gcb.build());
		this.cacheManager.addListener(this);

		INSTANCE = this;

		registerListener(new ClusterListener() {
			@Override
			public void viewChanged(ViewChangedEvent event) {
				Address myAddress = event.getLocalAddress();
				List<Address> members = event.getNewMembers();
				checkLeader(members, myAddress);
			}
		});

		checkLeader(getClusterMembers(), getMyAddress());
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

	public void registerListener(ClusterListener listener) {
		listeners.add(listener);
	}

	@ViewChanged
	public void viewChanged(ViewChangedEvent event) {
		logger.info("Cluster view changed --> " + event.getNewMembers());
		for (ClusterListener listener : listeners) {
			listener.viewChanged(event);
		}
	}

	public ClusterExecutor getClusterExecution() {
		return cacheManager.executor();
	}

	public <K extends Serializable, V> Cache<K, V> getCache(String cacheName) {
		return getCache(cacheName, LockingMode.OPTIMISTIC);
	}

	public <K extends Serializable, V> Cache<K, V> getCache(String cacheName, LockingMode lockingMode) {

		Cache<K, V> cache = cacheManager.getCache(cacheName, false);

		if (cache == null) {

			synchronized (cacheManager) {

				cache = cacheManager.getCache(cacheName, false);

				if (cache == null) {

					logger.info("Creating cache '" + cacheName + "'");

					Configuration cfg = getCacheConfiguration(lockingMode);

					cacheManager.defineConfiguration(cacheName, cfg);

					cache = cacheManager.getCache(cacheName);

					cache.addListener(this);
				}
			}
		}
		return cache;
	}

	private Configuration getCacheConfiguration(LockingMode lockingMode) {
		ConfigurationBuilder cfg = new ConfigurationBuilder();

		// cfg.persistence() //
		// .passivation(false) //
		// .addStore(RocksDBStoreConfigurationBuilder.class) //
		// .location(new File(CACHE_DIR, "data").getAbsolutePath() + "/")//
		// .expiredLocation(new File(CACHE_DIR, "expired").getAbsolutePath() + "/");

		cfg.locking() //
				.lockAcquisitionTimeout(1, TimeUnit.MINUTES) //
				.isolationLevel(IsolationLevel.READ_COMMITTED);

		// cfg.transaction() //
		// .lockingMode(lockingMode) //
		// .transactionMode(TransactionMode.TRANSACTIONAL) //
		// .transactionManagerLookup(new TransactionManagerLookup() {
		// @Override
		// public TransactionManager getTransactionManager() throws Exception {
		// return transactionManager;
		// }
		// });

		cfg.clustering() //
				.cacheMode(CacheMode.REPL_ASYNC) //
				.stateTransfer().awaitInitialTransfer(true) //
				.fetchInMemoryState(true);

		return cfg.build();
	}

	public ClusteredLock getLock(String name) {

		ClusteredLockManager clusteredLockManager = EmbeddedClusteredLockManagerFactory.from(cacheManager);

		if (!clusteredLockManager.isDefined(name)) {
			synchronized (this) {
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
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <K extends Serializable, V extends Serializable> MultimapCache<K, V> getMultimap(String name, LockingMode lockingMode) {

		MultimapCacheManager multimapCacheManager = EmbeddedMultimapCacheManagerFactory.from(cacheManager);

		if (!multimaps.contains(name)) {
			synchronized (this) {
				if (!multimaps.contains(name)) {

					multimapCacheManager.defineConfiguration(name, getCacheConfiguration(lockingMode));
					logger.info("Multimap created --> " + name);
				}
			}
		}
		return multimapCacheManager.get(name);
	}

}
