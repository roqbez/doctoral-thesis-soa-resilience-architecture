package br.ufsc.gsigma.architecture.experiments.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class CollectElasticSearchMetrics2 {

	public static void main(String[] args) throws Exception {

		File[] exps = new File("data").listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return dir.isDirectory() && name.startsWith("exp7-");
			}
		});

		for (File f : exps) {

			String exp = f.getName();

			File[] dates = f.listFiles();

			if (dates.length == 1) {

				String date = dates[0].getName();

				List<Map<String, Object>> stats = new ArrayList<Map<String, Object>>();

				stats.add(CollectElasticSearchMetrics.getProcessStats(CollectElasticSearchMetrics.getMetricFiles(exp, date, "BillingwithCreditNoteProcess")));
				stats.add(CollectElasticSearchMetrics.getProcessStats(CollectElasticSearchMetrics.getMetricFiles(exp, date, "CreateCatalogueProcess")));
				stats.add(CollectElasticSearchMetrics.getProcessStats(CollectElasticSearchMetrics.getMetricFiles(exp, date, "FulfilmentwithReceiptAdviceProcess")));
				stats.add(CollectElasticSearchMetrics.getProcessStats(CollectElasticSearchMetrics.getMetricFiles(exp, date, "OrderingProcess")));
				stats.add(CollectElasticSearchMetrics.getProcessStats(CollectElasticSearchMetrics.getMetricFiles(exp, date, "PaymentProcess")));

				DescriptiveStatistics compoundProcessDurationStats = new DescriptiveStatistics();

				int completedOkInstances = 0;
				int completedTotal = 0;

				for (Map<String, Object> stat : stats) {
					compoundProcessDurationStats.addValue((double) stat.get("processDurationMean"));
					completedOkInstances += (Integer) stat.get("completedOkInstances");
					completedTotal += (Integer) stat.get("completedTotal");
				}

				String[] s = exp.split("-");

				int bsReplicas = Integer.valueOf(s[1].substring(s[1].length() - 1));
				int exsReplicas = Integer.valueOf(s[2].substring(s[2].length() - 1));
				int rsReplicas = Integer.valueOf(s[3].substring(s[3].length() - 1));

				if (bsReplicas > 1 && exsReplicas > 1 && rsReplicas > 1) {

					String label = exsReplicas + "-" + bsReplicas + "-" + rsReplicas;

					System.out.println(label + "\t" //
							+ Math.round(compoundProcessDurationStats.getMean()) + "\t" //
							+ Math.round(((double) completedOkInstances / completedTotal) * 100) + "\t"//
							+ exp + "\t"//
							+ exsReplicas + "\t" + bsReplicas + "\t" + rsReplicas + "\t"//
							+ (bsReplicas + exsReplicas + rsReplicas));
				}
			}
		}

	}
}
