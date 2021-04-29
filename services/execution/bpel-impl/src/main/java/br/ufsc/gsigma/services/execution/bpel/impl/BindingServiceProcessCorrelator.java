package br.ufsc.gsigma.services.execution.bpel.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.ode.bpel.iapi.ProcessConf;

import br.ufsc.gsigma.binding.model.BindingConfiguration;
import br.ufsc.gsigma.common.hash.HashUtil;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.binding.InfinispanBindingConfigurationHolder;

public class BindingServiceProcessCorrelator {

	private static final Logger logger = Logger.getLogger(BindingServiceProcessCorrelator.class);

	private static final BindingServiceProcessCorrelator INSTANCE = new BindingServiceProcessCorrelator();

	private Map<Integer, InfinispanBindingConfigurationHolder> processBinding = InfinispanDatabase.getInstance().getCache(InfinispanBindingConfigurationHolder.class);

	private Map<Integer, CountDownLatch> waitToCorrelateMap = new ConcurrentHashMap<Integer, CountDownLatch>();

	private BindingServiceProcessCorrelator() {
	}

	public static BindingServiceProcessCorrelator getInstance() {
		return INSTANCE;
	}

	public void startCorrelate(ProcessConf processConf) {
		synchronized (processBinding) {

			int key = generateKey(processConf);

			if (!processBinding.containsKey(key)) {
				if (!waitToCorrelateMap.containsKey(key))
					waitToCorrelateMap.put(key, new CountDownLatch(1));
			}
		}
	}

	public void correlate(ProcessConf processConf, BindingConfiguration bindingConfiguration) {
		synchronized (processBinding) {

			int key = generateKey(processConf);

			processBinding.put(key, new InfinispanBindingConfigurationHolder(key, bindingConfiguration));

			CountDownLatch countDown = waitToCorrelateMap.remove(key);
			if (countDown != null)
				countDown.countDown();

			logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_SERVICE_BINDING, "Correlating process " + processConf.getProcessId() + " with binding configuration " + bindingConfiguration));
		}
	}

	public boolean removeCorrelation(ProcessConf processConf) {
		synchronized (processBinding) {

			int key = generateKey(processConf);

			boolean removed = processBinding.remove(key) != null;

			CountDownLatch countDown = waitToCorrelateMap.remove(key);
			if (countDown != null)
				countDown.countDown();

			if (removed)
				logger.info("Removing correlation of process " + processConf.getProcessId());

			return removed;
		}
	}

	public BindingConfiguration getBindingConfigurationNoWait(ProcessConf processConf) {

		int key = generateKey(processConf);

		InfinispanBindingConfigurationHolder holder = processBinding.get(key);

		return holder != null ? holder.getBindingConfiguration() : null;
	}

	public BindingConfiguration getBindingConfiguration(ProcessConf processConf) throws InterruptedException {

		int key = generateKey(processConf);

		CountDownLatch countDown = waitToCorrelateMap.get(key);
		if (countDown != null) {
			logger.info("Waiting for process binding correlation be done for " + processConf.getProcessId());
			countDown.await();
		}

		InfinispanBindingConfigurationHolder holder = processBinding.get(key);

		return holder != null ? holder.getBindingConfiguration() : null;
	}

	private int generateKey(ProcessConf processConf) {
		return HashUtil.getHashFromValues(processConf.getProcessId());
	}

}
