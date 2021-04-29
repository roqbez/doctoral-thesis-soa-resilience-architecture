package br.ufsc.gsigma.architecture.experiments.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import br.ufsc.gsigma.infrastructure.util.ObjectHolder;
import br.ufsc.gsigma.infrastructure.ws.ServiceClient;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;
import br.ufsc.gsigma.services.deployment.interfaces.DeploymentService;

public abstract class ExperimentsUtil {

	private static final DeploymentService deploymentService;

	static {
		deploymentService = ServiceClient.getClient(DeploymentService.class, //
				"http://" + ServicesAddresses.DEPLOYMENT_SERVICE_HOSTNAME + ":" + ServicesAddresses.DEPLOYMENT_SERVICE_PORT + "/services/DeploymentService", //
				CxfUtil.getClientFeatures());
	}

	public static DeploymentService getDeploymentService() {
		return deploymentService;
	}

	public static String clearLogs(String executionId, String processName) throws Exception {
		return elasticSearchDeleteByQuery("{\"query\" : { \"bool\" : {\"filter\": [ {\"term\" : { \"executionId.keyword\" : \"" + executionId + "\" } }, {\"term\" : { \"processName.keyword\" : \"" + processName + "\" } } ] } } }");
	}

	public static String clearProgramLogs(String program) throws Exception {
		return elasticSearchDeleteByQuery("{\"query\":{\"term\":{\"PROGRAM.keyword\":\"" + program + "\"}}}");
	}

	public static String clearAllLogs() throws MalformedURLException, IOException, ProtocolException, UnsupportedEncodingException {

		URL u = new URL("http://logging.d-201603244.ufsc.br:9200/logstash-*");

		HttpURLConnection connection = (HttpURLConnection) u.openConnection();
		connection.setRequestMethod("DELETE");

		try {

			byte[] data = null;

			try (InputStream in = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream()) {
				data = IOUtils.toByteArray(connection.getInputStream());
			}

			return new String(data, "UTF-8");

		} finally {
			connection.disconnect();
		}
	}

	private static String elasticSearchDeleteByQuery(String payload) throws MalformedURLException, IOException, ProtocolException, UnsupportedEncodingException {

		URL u = new URL("http://logging.d-201603244.ufsc.br:9200/logstash-*/_delete_by_query");

		HttpURLConnection connection = (HttpURLConnection) u.openConnection();
		connection.addRequestProperty("Content-Type", "application/json;charset=UTF-8");
		connection.setRequestMethod("POST");

		try {
			connection.setDoOutput(true);

			try (OutputStream out = connection.getOutputStream()) {
				out.write(payload.getBytes("UTF-8"));
			}

			byte[] data = null;

			try (InputStream in = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream()) {
				data = IOUtils.toByteArray(connection.getInputStream());
			}

			return new String(data, "UTF-8");

		} finally {
			connection.disconnect();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ScheduledJob<?> scheduleAtFixedRate(final ScheduledExecutorService scheduler, final Job job, long initialDelay, long period, final int repeats) {

		final ObjectHolder<ScheduledJob<?>> scheduling = new ObjectHolder<ScheduledJob<?>>();

		Runnable runnable = new Runnable() {

			int count = 0;

			@Override
			public void run() {

				try {
					job.run(++count, repeats);
				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					if (repeats > 0 && count == repeats) {
						scheduling.get().cancel(false);
						scheduling.get().getJobDone().countDown();
					}
				}

			}
		};

		ScheduledJob<?> resp = new ScheduledJob(scheduler.scheduleAtFixedRate(runnable, initialDelay, period, TimeUnit.MILLISECONDS));
		scheduling.set(resp);
		return resp;
	}

}
