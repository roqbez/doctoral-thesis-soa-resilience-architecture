package br.ufsc.gsigma.infrastructure.util.docker;

import java.io.Closeable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import hirondelle.date4j.DateTime;

public abstract class ContainerLogPollSupport {

	private static Logger logger = LoggerFactory.getLogger(ContainerLogPollSupport.class);

	static class ContainerLogScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

		public ContainerLogScheduledThreadPoolExecutor(int corePoolSize) {
			super(corePoolSize);
		}

		@Override
		protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
			ContainerLogPollJob job = (ContainerLogPollJob) runnable;
			ContainerLogScheduledFuture<V> r = new ContainerLogScheduledFuture<V>(task, job);
			r.job = job;
			logger.info("Creating polling job for container " + job.containerId);
			return r;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		ContainerLogPollJob getContainerLogPollJob(String containerId) {
			for (ContainerLogPollJob job : new ArrayList<ContainerLogPollJob>((Collection) getQueue())) {
				if (job.containerId.equals(containerId)) {
					return job;
				}
			}
			return null;
		}
	}

	static class ContainerLogPollJob implements Runnable {

		private DockerClient docker;

		private String containerId;

		private List<ContainerLogClient> clients = new LinkedList<ContainerLogClient>();

		private ScheduledFuture<?> job;

		private ReentrantLock lock = new ReentrantLock();

		public ContainerLogPollJob(DockerClient docker, String containerId, ContainerLogClient client) {
			this.docker = docker;
			this.containerId = containerId;
			this.clients.add(client);
		}

		public ContainerLogPollJob(DockerClient docker, String containerId) {
			this.docker = docker;
			this.containerId = containerId;
		}

		void addClient(ContainerLogClient client) {
			this.clients.add(client);
		}

		@Override
		public void run() {

			try {

				DateTime since = null;

				try {
					lock.lock();

					for (ContainerLogClient c : clients) {
						if (c.lastTimestamp == null) {
							if (since == null)
								break;
						} else if (since != null) {
							since = c.lastTimestamp.compareTo(since) < 0 ? c.lastTimestamp : since;
						} else {
							since = c.lastTimestamp;
						}
					}

				} finally {
					lock.unlock();
				}

				LogContainerCmd cmd = (LogContainerCmd) docker.logContainerCmd(containerId) //
						.withTimestamps(true) //
						.withStdErr(Boolean.TRUE) //
						.withStdOut(Boolean.TRUE); //

				Long sinceUnixTimestamp = since != null ? since.getMilliseconds(TimeZone.getTimeZone("UTC")) : null;

				if (sinceUnixTimestamp != null)
					cmd.withSince((int) (sinceUnixTimestamp / 1000));

				final LogContainerResultCallback callback = new LogContainerResultCallback() {

					@Override
					public void onStart(Closeable stream) {
						lock.lock();
						super.onStart(stream);
					}

					@Override
					public void onComplete() {
						super.onComplete();
						lock.unlock();
					}

					@Override
					public void onNext(Frame item) {

						String msg = item.toString();

						DateTime time = getMsgTimestamp(msg);

						for (ContainerLogClient c : clients) {
							if (c.lastTimestamp == null || c.lastTimestamp.compareTo(time) < 0) {

								if (item.getStreamType() == StreamType.STDOUT)
									c.stdout.println(msg);
								else if (item.getStreamType() == StreamType.STDERR)
									c.stderr.println(msg);

								c.lastTimestamp = time;
							}
						}
					}

					protected DateTime getMsgTimestamp(String msg) {
						int idx1 = msg.indexOf(' ');
						int idx2 = msg.indexOf(' ', idx1 + 1);

						// ommit Z
						String tstmp = msg.substring(idx1 + 1, idx2 - 1);

						// https://nbsoftsolutions.com/blog/iso-8601-and-nanosecond-precision-across-languages
						return new DateTime(tstmp);
					}
				};

				cmd.exec(callback).awaitCompletion();

			} catch (NotFoundException e) {
				stopPollingClientNotExists();
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}

		private void stopPollingClientNotExists() {
			logger.info("The container with id " + containerId + " doesn't exist anymore. Stopping polling for logs");
			job.cancel(false);
		}
	}

	static class ContainerLogScheduledFuture<V> implements RunnableScheduledFuture<V> {

		private RunnableScheduledFuture<V> delegate;

		private ContainerLogPollJob job;

		public ContainerLogScheduledFuture(RunnableScheduledFuture<V> delegate, ContainerLogPollJob job) {
			this.delegate = delegate;
			this.job = job;
		}

		public ContainerLogPollJob getContainerLogPollJob() {
			return job;
		}

		public long getDelay(TimeUnit unit) {
			return delegate.getDelay(unit);
		}

		public void run() {
			delegate.run();
		}

		public boolean isPeriodic() {
			return delegate.isPeriodic();
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			return delegate.cancel(mayInterruptIfRunning);
		}

		public int compareTo(Delayed o) {
			return delegate.compareTo(o);
		}

		public boolean isCancelled() {
			return delegate.isCancelled();
		}

		public boolean isDone() {
			return delegate.isDone();
		}

		public V get() throws InterruptedException, ExecutionException {
			return delegate.get();
		}

		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return delegate.get(timeout, unit);
		}

	}

	static class ContainerLogClient {

		private DateTime lastTimestamp;

		private PrintStream stdout;

		private PrintStream stderr;

		public ContainerLogClient(PrintStream stdout, PrintStream stderr) {
			this.stdout = stdout;
			this.stderr = stderr;
		}

	}

}
