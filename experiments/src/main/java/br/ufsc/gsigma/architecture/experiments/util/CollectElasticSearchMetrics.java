package br.ufsc.gsigma.architecture.experiments.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.services.core.util.json.ISO8601Utils;

public class CollectElasticSearchMetrics {

	private static List<String> DEFAULT_COLUMNS = new LinkedList<String>();

	private static Map<String, String[]> PROCESS_PARTNERS = new HashMap<String, String[]>();

	static {
		DEFAULT_COLUMNS.add("@timestamp");
		DEFAULT_COLUMNS.add("message_id");
		DEFAULT_COLUMNS.add("processInstanceId");
		DEFAULT_COLUMNS.add("processCreationTime");
		DEFAULT_COLUMNS.add("processFinishTime");
		DEFAULT_COLUMNS.add("processDuration");
		DEFAULT_COLUMNS.add("numberOfBindingReconfigurations");
		DEFAULT_COLUMNS.add("QoS.Constraints.Performance.ResponseTime");
		DEFAULT_COLUMNS.add(LogConstants.RESILIENCE_REACTION_MEAN_TIME);
		DEFAULT_COLUMNS.add(LogConstants.RESILIENCE_DISCOVERY_MEAN_TIME);
		DEFAULT_COLUMNS.add(LogConstants.RESILIENCE_DEPLOYMENT_MEAN_TIME);
		DEFAULT_COLUMNS.add(LogConstants.RESILIENCE_SERVICES_CHECK_MEAN_TIME);
		DEFAULT_COLUMNS.add("QoS.Declared.EndToEnd.Performance.ResponseTime");
		DEFAULT_COLUMNS.add("QoS.Observed.EndToEnd.Performance.ResponseTime");
		DEFAULT_COLUMNS.add("processStateCode");
		DEFAULT_COLUMNS.add("executionId");
		DEFAULT_COLUMNS.add("numberOfInvokedServices");
		DEFAULT_COLUMNS.add("invokedServiceProviders");
		DEFAULT_COLUMNS.add("invokedServices");

		PROCESS_PARTNERS.put("BillingwithCreditNoteProcess", new String[] { "publisher5", "publisher61", "publisher35", "publisher20" });
		PROCESS_PARTNERS.put("CreateCatalogueProcess", new String[] { "publisher48", "publisher69", "publisher72" });
		PROCESS_PARTNERS.put("FulfilmentwithReceiptAdviceProcess", new String[] { "publisher76", "publisher33", "publisher24", "publisher68" });
		PROCESS_PARTNERS.put("OrderingProcess", new String[] { "publisher61", "publisher3", "publisher6", "publisher59" });
		PROCESS_PARTNERS.put("PaymentProcess", new String[] { "publisher24", "publisher37", "publisher60" });
	}

	public static void main(String[] args) throws Exception {

		String exp = "exp5-bs1-exs1-rs1";
		String date = "2018-10-12-09-15-06";

		printExpTable(Arrays.asList(//
				getProcessStats(getMetricFiles(exp, date, "BillingwithCreditNoteProcess")), //
				getProcessStats(getMetricFiles(exp, date, "CreateCatalogueProcess")), //
				getProcessStats(getMetricFiles(exp, date, "FulfilmentwithReceiptAdviceProcess")), //
				getProcessStats(getMetricFiles(exp, date, "OrderingProcess")), //
				getProcessStats(getMetricFiles(exp, date, "PaymentProcess")))//
		);

		// collect("exp2-bs1-exs1-rs1", new String[] { "BillingwithCreditNoteProcess", "PaymentProcess", "OrderingProcess", "FulfilmentwithReceiptAdviceProcess", "CreateCatalogueProcess" }, new File("data"), new Date());
	}

	public static void printExpTable(List<Map<String, Object>> stats) {
		printExpTable(stats, null);
	}

	@SuppressWarnings("unchecked")
	public static void printExpTable(List<Map<String, Object>> stats, PrintWriter pw) {

		int completedTotal = 0;
		int completedOkInstances = 0;
		int completedQoSViolationInstances = 0;
		int notCompletedInstances = 0;

		for (Map<String, Object> m : stats) {

			StringBuilder sb = getExpTableLine(m);

			System.out.println(sb);

			if (pw != null) {
				pw.println(sb);
			}

			completedOkInstances += (Integer) m.get("completedOkInstances");
			completedQoSViolationInstances += (Integer) m.get("completedQoSViolationInstances");
			notCompletedInstances += (Integer) m.get("notCompletedInstances");
			completedTotal += (Integer) m.get("completedTotal");
		}

		System.out.println();

		String l = "totalCompletedOkInstances=" + Math.round(((double) completedOkInstances / completedTotal) * 100) + "\n" //
				+ "completedQoSViolationInstances=" + Math.round(((double) completedQoSViolationInstances / completedTotal) * 100) + "\n" //
				+ "notCompletedInstances=" + Math.round(((double) notCompletedInstances / completedTotal) * 100);

		System.out.println(l);

		if (pw != null) {
			pw.println(l);
		}

		System.out.println();

		for (Map<String, Object> m : stats) {
			System.out.println(m);
		}

		System.out.println();

		for (Map<String, Object> m : stats) {

			String p1 = "Response time distribution for " + m.get("processName");

			System.out.println(p1);

			if (pw != null) {
				pw.println(p1);
			}

			Integer total = (Integer) m.get("completedTotal");

			Map<Integer, Integer> dist = (Map<Integer, Integer>) m.get("responseTimeDist");

			for (Entry<Integer, Integer> e : dist.entrySet()) {
				Integer percent = (int) (((double) e.getValue() / total) * 100);

				String p2 = e.getKey() + "\t" + percent + "%";

				System.out.println(p2);

				if (pw != null) {
					pw.println(p2);
				}
			}

			String p3 = "-------------------------------------------------------------------------------";

			System.out.println(p3);

			if (pw != null) {
				pw.println(p3);
			}
		}

	}

	public static StringBuilder getExpTableLine(Map<String, Object> m) {

		int numberOfProviders = PROCESS_PARTNERS.get(m.get("processName")).length;
		int numberOfInvokedServiceProviders = (int) m.get("numberOfInvokedServiceProviders");

		int numberOfPartnersSubstitutions = Math.max(0, numberOfInvokedServiceProviders - numberOfProviders);

		int completedOkInstances = (int) m.get("completedOkInstances");
		int completedQoSViolationInstances = (int) m.get("completedQoSViolationInstances");
		int notCompletedInstances = (int) m.get("notCompletedInstances");

		int executionEnvFailures = 0;
		int bindingServiceFailures = 0;
		int resilienceServiceFailures = 0;

		long processDurationMean = Math.round(((Number) m.get("processDurationMean")).doubleValue());
		long processDurationSD = Math.round(((Number) m.get("processDurationSD")).doubleValue());
		long resilienceTimeMean = Math.round(((Number) m.get("resilienceTimeMean")).doubleValue());

		StringBuilder sb = new StringBuilder();
		sb.append(processDurationMean + "\t");
		sb.append(processDurationSD + "\t");
		// sb.append(m.get("numberOfInvokedTasks") + "\t");
		sb.append(resilienceTimeMean + "\t");
		sb.append(m.get("numberOfReactionEvents") + "\t");
		sb.append(numberOfPartnersSubstitutions + "\t");
		sb.append(completedOkInstances + "\t");
		sb.append(completedQoSViolationInstances + "\t");
		sb.append(notCompletedInstances + "\t");
		sb.append(m.get("numberOfAnalysisEvents") + "\t");
		sb.append(executionEnvFailures + "\t");
		sb.append(bindingServiceFailures + "\t");
		sb.append(resilienceServiceFailures + "\t");
		return sb;
	}

	public static MetricFiles getMetricFiles(String expName, String date, String processName) {
		File f1 = new File("data", expName + "/" + date + "/" + date + "-" + expName + "-" + processName + "-full.csv");
		File f2 = new File("data", expName + "/" + date + "/" + date + "-" + expName + "-" + processName + "-reaction.csv");
		File f3 = new File("data", expName + "/" + date + "/" + date + "-" + expName + "-" + processName + "-analysis.csv");
		return new MetricFiles(f1, f2, f3);
	}

	public static List<MetricFiles> collect(String executionId, String[] processNames, File outputFolder, Date date) throws IOException, InterruptedException {

		List<MetricFiles> files = new LinkedList<MetricFiles>();

		for (String processName : processNames) {
			MetricFiles f = collect(executionId, processName, outputFolder, date);
			files.add(f);
		}

		return files;
	}

	@SuppressWarnings("unchecked")
	public static MetricFiles collect(String executionId, String processName, File outputFolder, Date date) throws IOException, InterruptedException {

		collectInternal(executionId, processName, outputFolder, date, DEFAULT_COLUMNS);

		return collectInternal(executionId, processName, outputFolder, date, Collections.EMPTY_LIST);
	}

	private static MetricFiles collectInternal(String executionId, String processName, File outputFolder, Date date, List<String> columns) throws IOException, InterruptedException {

		String dateLabel = new SimpleDateFormat("YYYY-MM-dd-HH-mm-ss").format(date);

		File f = new File(new File(outputFolder, executionId), dateLabel);

		f.mkdirs();

		boolean hasProjections = !CollectionUtils.isEmpty(columns);

		File processInstancesFile = new File(f, dateLabel + "-" + executionId + "-" + processName + (!hasProjections ? "-full" : "") + ".csv");

		File reactionFile = new File(f, dateLabel + "-" + executionId + "-" + processName + "-reaction.csv");

		File analysisFile = new File(f, dateLabel + "-" + executionId + "-" + processName + "-analysis.csv");

		// Resilience Info
		{
			queryES(reactionFile, //
					"(message_id:RESILIENCE_REACTION AND reconfigurationDone:true AND processName:\"" + processName + "\")");

			queryES(analysisFile, //
					"(message_id:RESILIENCE_ANALYSIS AND processName:\"" + processName + "\")");

		}

		File tFile = null;

		try {

			tFile = File.createTempFile("e2csv", ".csv");

			String fileName = getLinuxFilename(tFile);

			StringBuilder es2csvCmd = new StringBuilder();

			// String query = "(message_id:PROCESS_INSTANCE AND processFinished:true AND processStateCode:(30 70))";

			String query = "(executionId:\"" + executionId + "\" AND processName:\"" + processName + "\" AND processFinished:true)";

			es2csvCmd.append("es2csv -u logging.d-201603244.ufsc.br:9200 ");
			es2csvCmd.append("-q '" + query + "' ");
			es2csvCmd.append("-o " + fileName + " ");

			if (hasProjections) {
				es2csvCmd.append("-f ");
				es2csvCmd.append(StringUtils.join(columns, " "));
			}

			// System.out.println("Executing " + es2csvCmd);

			String cmd = es2csvCmd.toString();

			Process process = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmd });

			process.waitFor();

			System.out.println(IOUtils.toString(process.getInputStream(), "UTF-8"));
			System.err.println(IOUtils.toString(process.getErrorStream(), "UTF-8"));

			try (OutputStream out = new FileOutputStream(processInstancesFile)) {

				PrintWriter pw = new PrintWriter(out, true);

				try (InputStream in = new FileInputStream(tFile)) {

					List<String> outputLines = IOUtils.readLines(in, StandardCharsets.UTF_8);

					if (outputLines.isEmpty()) {
						// System.err.println("Empty data");
						return null;
					}

					// Header
					String header = outputLines.get(0);
					String[] colNames = header.split(",");

					colNames = (String[]) ArrayUtils.add(colNames, "failedProcessDuration");

					pw.println(StringUtils.join(colNames, ","));

					Collection<String> sortedLines = new TreeSet<>();

					for (String s : outputLines.subList(1, outputLines.size())) {
						sortedLines.add(s);
					}

					SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

					DecimalFormat decf = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.getDefault()));

					next: for (String s : sortedLines) {

						if (hasProjections) {

							String[] parts = s.split(",", colNames.length);

							for (int k = 0; k < parts.length; k++) {

								String p = parts[k];

								if (colNames[k].equals("executionId") && !executionId.equals(p)) {
									continue next;
								}

								if (colNames[k].equals("@timestamp")) {
									parts[k] = String.valueOf(ISO8601Utils.parse(p).getTime());
								} else if (p.contains("-") && p.contains("T") && p.contains(":")) {
									try {
										parts[k] = df.format(ISO8601Utils.parse(p));
									} catch (IllegalArgumentException e) {
									}
								} else {

									if ("".equals(p)) {

										if (equalsAny(colNames[k], LogConstants.PROCESS_DURATION, LogConstants.RESILIENCE_REACTION_MEAN_TIME, LogConstants.RESILIENCE_SERVICES_CHECK_MEAN_TIME, LogConstants.RESILIENCE_DEPLOYMENT_MEAN_TIME, LogConstants.RESILIENCE_SERVICES_CHECK_MEAN_TIME))
											parts[k] = "0";
									}
								}

								if (NumberUtils.isNumber(parts[k])) {
									parts[k] = "\"" + decf.format(Double.valueOf(parts[k])) + "\"";
								}

								// if (parts[k].endsWith(".0")) {
								// parts[k] = parts[k].substring(0, parts[k].length() - 2);
								// }
							}

							int processDurationPosi = getColumnPosi(colNames, "processDuration");
							String processDuration = parts[processDurationPosi];

							boolean isFailed = 60 == Integer.valueOf(parts[getColumnPosi(colNames, "processStateCode")].replaceAll("\"", ""));

							if (isFailed) {
								parts[processDurationPosi] = "";
								parts[getColumnPosi(colNames, "QoS.Declared.EndToEnd.Performance.ResponseTime")] = "";
								parts[getColumnPosi(colNames, "QoS.Observed.EndToEnd.Performance.ResponseTime")] = "";
							}

							Object failedProcessDuration = isFailed ? processDuration : "";

							parts = (String[]) ArrayUtils.add(parts, failedProcessDuration);

							s = StringUtils.join(parts, ",");
							// System.out.println(s);
						}

						pw.println(s);
					}
				}
			}

			return new MetricFiles(processInstancesFile, reactionFile, analysisFile);

		} finally {
			tFile.delete();
		}

	}

	private static void queryES(File file, String query) throws IOException, InterruptedException {
		String fileName = getLinuxFilename(file);

		StringBuilder es2csvCmd = new StringBuilder();

		es2csvCmd.append("es2csv -u logging.d-201603244.ufsc.br:9200 ");
		es2csvCmd.append("-q '" + query + "' ");
		es2csvCmd.append("-o " + fileName + " ");

		String cmd = es2csvCmd.toString();

		Process process = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmd });

		process.waitFor();

		System.out.println(IOUtils.toString(process.getInputStream(), "UTF-8"));
		System.err.println(IOUtils.toString(process.getErrorStream(), "UTF-8"));
	}

	private static String getLinuxFilename(File tFile) {
		String aux = tFile.getAbsolutePath().replaceAll("\\\\", "/").replaceAll(":", "");

		String fileName = "/mnt/" + aux.substring(0, 1).toLowerCase() + aux.substring(1);
		return fileName;
	}

	public static Map<String, Object> getProcessStats(MetricFiles metricFiles) throws Exception {

		Map<String, Object> result = new LinkedHashMap<String, Object>();

		List<Map<String, String>> processInstancesList = csvToListMap(metricFiles.processFile);

		DescriptiveStatistics statsProcessDuration = new DescriptiveStatistics();
		DescriptiveStatistics statsResilienceMeanTime = new DescriptiveStatistics();

		String processName = "";

		int completedOkInstances = 0;
		int completedQoSViolationInstances = 0;

		int countInvokedTasks = 0;

		int countInvokedServiceProviders = 0;

		int numberOfBindingReconfigurations = 0;

		Map<Integer, Integer> mapResponseTimeInstances = new TreeMap<Integer, Integer>();

		for (int i = 5; i <= 20; i++) {
			mapResponseTimeInstances.put(1000 * i, 0);
		}

		mapResponseTimeInstances.put(1000 * 30, 0);
		mapResponseTimeInstances.put(1000 * 45, 0);
		mapResponseTimeInstances.put(1000 * 60, 0);

		for (Map<String, String> m : processInstancesList) {

			processName = m.get("processName");

			Double processDuration = Double.valueOf(m.get("processDuration"));

			for (Integer respTime : new ArrayList<Integer>(mapResponseTimeInstances.keySet())) {
				if (processDuration <= respTime) {
					Integer n = mapResponseTimeInstances.get(respTime);
					mapResponseTimeInstances.put(respTime, n + 1);
				}
			}

			statsProcessDuration.addValue(processDuration);

			Double rTime = Double.valueOf(m.get("resilienceReactionMeanTime"));

			if (rTime > 0) {
				statsResilienceMeanTime.addValue(rTime);
			}

			int processStateCode = Integer.parseInt(m.get("processStateCode"));

			if (processStateCode == 30) {
				completedOkInstances++;
			} else if (processStateCode == 70) {
				completedQoSViolationInstances++;
			}

			Collection<String> invokedTasks = new LinkedHashSet<String>();

			Collection<String> invokedServiceProviders = new LinkedHashSet<String>();

			for (Entry<String, String> e : m.entrySet()) {
				if (e.getKey().startsWith("invokedTasks.")) {
					invokedTasks.add(e.getValue());

				} else if (e.getKey().startsWith("invokedServiceProviders.")) {
					invokedServiceProviders.add(e.getValue());
				}
			}

			countInvokedTasks = Math.max(countInvokedTasks, invokedTasks.size());
			countInvokedServiceProviders = Math.max(countInvokedServiceProviders, invokedServiceProviders.size());

			numberOfBindingReconfigurations = Math.max(numberOfBindingReconfigurations, Integer.valueOf(StringUtils.defaultIfBlank(m.get("maxBindingConfigurationVersion"), "0")));

		}

		result.put("processName", processName);

		result.put("processDurationMean", statsProcessDuration.getMean());
		result.put("processDurationSD", Math.sqrt(statsProcessDuration.getVariance()));

		result.put("resilienceTimeMean", statsResilienceMeanTime.getMean());
		result.put("resilienceTimeSD", Math.sqrt(statsResilienceMeanTime.getVariance()));

		int notCompletedInstances = 100 - (completedOkInstances + completedQoSViolationInstances);

		int completedTotal = processInstancesList.size();

		result.put("completedOkInstances", completedOkInstances);
		result.put("completedQoSViolationInstances", completedQoSViolationInstances);
		result.put("notCompletedInstances", notCompletedInstances);
		result.put("completedTotal", completedTotal);

		result.put("%completedOkInstances", Math.round(((double) completedOkInstances / completedTotal) * 100));
		result.put("%completedQoSViolationInstances", Math.round(((double) completedQoSViolationInstances) * 100));
		result.put("%notCompletedInstances", Math.round(((double) notCompletedInstances) * 100));

		result.put("numberOfInvokedTasks", countInvokedTasks);
		result.put("numberOfInvokedServiceProviders", countInvokedServiceProviders);
		result.put("numberOfBindingReconfigurations", numberOfBindingReconfigurations);

		List<Map<String, String>> resilienceAnalysisList = csvToListMap(metricFiles.resilienceAnalysisFile);

		List<Map<String, String>> resilienceExecutionList = csvToListMap(metricFiles.resilienceReactionFile);

		result.put("numberOfAnalysisEvents", resilienceAnalysisList.size());
		result.put("numberOfReactionEvents", resilienceExecutionList.size());

		result.put("responseTimeDist", mapResponseTimeInstances);

		// Response time distributions

		return result;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, String>> csvToListMap(File csv) throws Exception {

		if (!csv.exists()) {
			return Collections.EMPTY_LIST;
		}

		// try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv)))) {
		// String[] headers = br.readLine().split(",");
		// List<Map<String, String>> records = br.lines().map(s -> s.split(",")) //
		// .map(t -> IntStream.range(0, t.length) //
		// .boxed() //
		// .collect(Collectors.toMap(i -> headers[i], i -> t[i]))) //
		// .collect(Collectors.toList());
		//
		// return records;
		// }

		List<Map<String, String>> response = new LinkedList<Map<String, String>>();
		CsvMapper mapper = new CsvMapper();
		CsvSchema schema = CsvSchema.emptySchema().withHeader();
		MappingIterator<Map<String, String>> iterator = mapper.readerFor(Map.class).with(schema).readValues(csv);
		while (iterator.hasNext()) {
			response.add(iterator.next());
		}
		return response;

	}

	private static int getColumnPosi(String[] colNames, String name) {
		for (int i = 0; i < colNames.length; i++) {
			if (name.equals(colNames[i]))
				return i;
		}
		return -1;
	}

	private static boolean equalsAny(String str, String... values) {
		for (String s : values) {
			if (s.equals(str))
				return true;
		}
		return false;
	}

	public static class MetricFiles {

		public File processFile;

		public File resilienceReactionFile;

		public File resilienceAnalysisFile;

		public MetricFiles(File processFile, File resilienceFile, File resilienceAnalysisFile) {
			this.processFile = processFile;
			this.resilienceReactionFile = resilienceFile;
			this.resilienceAnalysisFile = resilienceAnalysisFile;
		}

	}
}
