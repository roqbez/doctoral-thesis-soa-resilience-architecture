package br.ufsc.gsigma.architecture.experiments.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import br.ufsc.services.core.util.json.JsonUtil;

public class ExperimentsQueries {

	public static void main(String[] args) throws Exception {
		printNotFinishedProcessInstances();
	}

	@SuppressWarnings("unchecked")
	public static void printNotFinishedProcessInstances() throws Exception {
		List<Map<String, Object>> list = getNotFinishedProcessInstances(new ArrayList<Integer>());

		for (Map<String, Object> m : list) {
			Map<String, Object> data = (Map<String, Object>) m.get("_source");
			System.out.println(data.get("processInstanceId") + " - " + data.get("processName"));
		}

		System.out.println("Total:" + list.size());
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getNotFinishedProcessInstances(List<Integer> duplicatedInstances) throws Exception {

		Collection<Integer> finishedInstances = new HashSet<Integer>();

		{
			String queryCreated = "{\"from\" : 0, \"size\" : 1000, \"query\":{\"term\":{\"processFinished\":\"true\"}}}";

			Map<String, Object> result = elasticSearchQuery(queryCreated);

			List<Map<String, Object>> list = (List<Map<String, Object>>) ((Map<String, Object>) result.get("hits")).get("hits");

			for (Map<String, Object> m : list) {
				Map<String, Object> data = (Map<String, Object>) m.get("_source");
				Integer processInstanceId = (Integer) data.get("processInstanceId");
				boolean added = finishedInstances.add(processInstanceId);
				if (!added) {
					System.err.println("Process instance id '" + processInstanceId + "' is duplicated on finish");

					if (duplicatedInstances != null) {
						duplicatedInstances.add(processInstanceId);
					}
				}
			}
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{\"from\" : 0, \"size\" : 1000, \"query\": { \"bool\": { ");

		sb.append("\"filter\": {\"term\":{\"processStarted\":\"true\"}}");

		sb.append(", \"must_not\": [");

		int i = 0;

		for (Integer finishInstance : finishedInstances) {

			sb.append("{\"term\":{\"processInstanceId\":" + finishInstance + "}}");

			if (i++ < finishedInstances.size() - 1) {
				sb.append(",");
			} else {
				sb.append("]");
			}
		}

		sb.append("}}}");

		Map<String, Object> notFinished = elasticSearchQuery(sb.toString());

		return (List<Map<String, Object>>) ((Map<String, Object>) notFinished.get("hits")).get("hits");
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> elasticSearchQuery(String query) throws Exception {

		URL u = new URL("http://logging.d-201603244.ufsc.br:9200/logstash-*/_search");

		HttpURLConnection connection = (HttpURLConnection) u.openConnection();
		connection.addRequestProperty("Content-Type", "application/json;charset=UTF-8");
		connection.setRequestMethod("POST");

		try {
			connection.setDoOutput(true);

			try (OutputStream out = connection.getOutputStream()) {
				out.write(query.getBytes("UTF-8"));
			}

			try (InputStream in = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream()) {
				return (Map<String, Object>) JsonUtil.getValueFromStream(connection.getInputStream(), Map.class);
			}

		} finally {
			connection.disconnect();
		}
	}

}
