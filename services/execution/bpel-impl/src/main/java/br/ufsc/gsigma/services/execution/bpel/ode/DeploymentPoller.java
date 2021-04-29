/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package br.ufsc.gsigma.services.execution.bpel.ode;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.engine.cron.CronScheduler;
import org.apache.ode.bpel.engine.cron.SystemSchedulesConfig;
import org.apache.ode.utils.WatchDog;

/**
 * Polls a directory for the deployment of a new deployment unit.
 */
public class DeploymentPoller {

	private static Log __log = LogFactory.getLog(DeploymentPoller.class);

	/** The polling interval. */
	private static final long POLL_TIME = 3000;

	private File _deployDir;

	private PollingThread _poller;

	protected AxisODEServer _odeServer;

	private boolean _onHold = false;

	private Object onHoldMonitor = new Object();

	private SystemSchedulesConfig _systemSchedulesConf;

	@SuppressWarnings("rawtypes")
	private Map<String, WatchDog> dDWatchDogsByPath = new HashMap<String, WatchDog>();

	@SuppressWarnings("rawtypes")
	private WatchDog _systemCronConfigWatchDog;

	/** Filter accepting directories containing a ode dd file. */
	private static final FileFilter _fileFilter = new FileFilter() {
		public boolean accept(File path) {
			return new File(path, "deploy.xml").exists();
		}
	};

	/** Filter accepting *.deployed files. */
	private static final FileFilter _deployedFilter = new FileFilter() {
		public boolean accept(File path) {
			return path.isFile() && path.getName().endsWith(".deployed");
		}
	};

	public DeploymentPoller(File deployDir, final AxisODEServer odeServer) {
		_odeServer = odeServer;
		_deployDir = deployDir;
		if (!_deployDir.exists())
			_deployDir.mkdir();
		_systemSchedulesConf = createSystemSchedulesConfig(odeServer.getConfigRoot());
		_systemCronConfigWatchDog = createSystemCronConfigWatchDog(odeServer.getBpelServer().getContexts().cronScheduler);
	}

	public void start() {
		_poller = new PollingThread();
		_poller.start();
		__log.info("Poller started.");
	}

	public void stop() {
		_poller.kill();
		_poller = null;
	}

	public File getDeployDir() {
		return _deployDir;
	}

	protected boolean isDeploymentFromODEFileSystemAllowed() {
		return true;
	}

	/**
	 * Scan the directory for new (or removed) files (called mainly from {@link PollingThread}) and calls whoever is in charge of the actual deployment (or undeployment).
	 */
	@SuppressWarnings({ "rawtypes" })
	private void check() {
		File[] files = _deployDir.listFiles(_fileFilter);

		// Checking for new deployment directories
		if (isDeploymentFromODEFileSystemAllowed() && files != null) {
			for (File file : files) {
				File deployXml = new File(file, "deploy.xml");
				File deployedMarker = new File(_deployDir, file.getName() + ".deployed");

				if (!deployXml.exists()) {
					// Skip if deploy.xml is abset
					__log.debug("Not deploying " + file + " (missing deploy.xml)");
				}

				WatchDog ddWatchDog = ensureDeployXmlWatchDog(file, deployXml);

				if (deployedMarker.exists()) {
					checkDeployXmlWatchDog(ddWatchDog);
					continue;
				}

				try {
					deployedMarker.createNewFile();
				} catch (IOException e1) {
					__log.error("Error creating deployed marker file, " + file + " will not be deployed");
					continue;
				}

				try {
					_odeServer.getProcessStore().undeploy(file);
				} catch (Exception ex) {
					__log.error("Error undeploying " + file.getName());
				}

				try {
					Collection<QName> deployed = _odeServer.getProcessStore().deploy(file);
					__log.info("Deployment of artifact " + file.getName() + " successful: " + deployed);
				} catch (Exception e) {
					__log.error("Deployment of " + file.getName() + " failed, aborting for now.", e);
				}
			}
		}

		// Removing deployments that disappeared
		File[] deployed = _deployDir.listFiles(_deployedFilter);
		for (File file : deployed) {
			String pkg = file.getName().substring(0, file.getName().length() - ".deployed".length());
			File deployDir = new File(_deployDir, pkg);
			if (!deployDir.exists()) {
				Collection<QName> undeployed = _odeServer.getProcessStore().undeploy(deployDir);
				file.delete();
				disposeDeployXmlWatchDog(deployDir);
				if (undeployed.size() > 0)
					__log.info("Successfully undeployed " + pkg);
			}
		}

		checkSystemCronConfigWatchDog(_systemCronConfigWatchDog);
	}

	@SuppressWarnings({ "rawtypes" })
	protected WatchDog ensureDeployXmlWatchDog(File deployFolder, File deployXml) {
		WatchDog ddWatchDog = dDWatchDogsByPath.get(deployXml.getAbsolutePath());
		if (ddWatchDog == null) {
			ddWatchDog = WatchDog.watchFile(deployXml, new DDWatchDogObserver(deployFolder.getName()));
			dDWatchDogsByPath.put(deployXml.getAbsolutePath(), ddWatchDog);
		}

		return ddWatchDog;
	}

	@SuppressWarnings({ "rawtypes" })
	protected void checkDeployXmlWatchDog(WatchDog ddWatchDog) {
		ddWatchDog.check();
	}

	protected void disposeDeployXmlWatchDog(File deployDir) {
		dDWatchDogsByPath.remove(new File(deployDir, "deploy.xml").getAbsolutePath());
	}

	protected SystemSchedulesConfig createSystemSchedulesConfig(File configRoot) {
		return new SystemSchedulesConfig(configRoot);
	}

	@SuppressWarnings({ "rawtypes" })
	protected WatchDog createSystemCronConfigWatchDog(final CronScheduler cronScheduler) {
		return WatchDog.watchFile(_systemSchedulesConf.getSchedulesFile(), new WatchDog.DefaultObserver() {
			public void init() {
				cronScheduler.refreshSystemCronJobs(_systemSchedulesConf);
			}
		});
	}

	@SuppressWarnings({ "rawtypes" })
	protected void checkSystemCronConfigWatchDog(WatchDog ddWatchDog) {
		ddWatchDog.check();
	}

	/**
	 * Thread that does the actual polling for new files.
	 */
	private class PollingThread extends Thread {
		private boolean _active = true;

		public PollingThread() {
			setName("DeploymentPoller");
		}

		/** Stop this poller, and block until it terminates. */
		void kill() {
			synchronized (this) {
				_active = false;
				this.notifyAll();
			}
			try {
				join();
			} catch (InterruptedException ie) {
				__log.fatal("Thread unexpectedly interrupted.", ie);
			}
		}

		public void run() {
			try {
				while (_active) {
					synchronized (onHoldMonitor) {
						if (!_onHold)
							check();
					}
					synchronized (this) {
						try {
							this.wait(POLL_TIME);
						} catch (InterruptedException e) {
						}
					}
				}
			} catch (Throwable t) {
				__log.fatal("Encountered an unexpected error.  Exiting poller...", t);
			}
		}
	}

	public void hold() {
		synchronized (onHoldMonitor) {
			_onHold = true;
		}
	}

	public void release() {
		synchronized (onHoldMonitor) {
			_onHold = false;
		}
	}

	public void markAsDeployed(File file) {
		File deployedMarker = new File(_deployDir, file.getName() + ".deployed");
		try {
			deployedMarker.createNewFile();
		} catch (IOException e) {
			__log.error("Couldn't create marker file for " + file.getName());
		}
	}

	public void markAsUndeployed(File file) {
		File deployedMarker = new File(_deployDir, file.getName() + ".deployed");
		deployedMarker.delete();
	}

	@SuppressWarnings({ "rawtypes" })
	protected class DDWatchDogObserver extends WatchDog.DefaultObserver {
		private String deploymentPakage;

		public DDWatchDogObserver(String deploymentPakage) {
			this.deploymentPakage = deploymentPakage;
		}

		public void init() {
			_odeServer.getProcessStore().refreshSchedules(deploymentPakage);
		}
	}
}
