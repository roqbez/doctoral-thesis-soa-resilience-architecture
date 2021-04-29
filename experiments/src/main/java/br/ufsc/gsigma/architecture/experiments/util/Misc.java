package br.ufsc.gsigma.architecture.experiments.util;

public class Misc {

	public static void main(String[] args) {

		int start = 1;
		int end = 4;

		for (int i = start; i <= end; i++) {

			for (int i2 = start; i2 <= end; i2++) {

				for (int i3 = start; i3 <= end; i3++) {

					String s = "mvn exec:java -Dexec.args=\"{`executionId`:`exp7-bs" + i + "-exs" + i2 + "-rs" + i3 + "`,`resilienceConfiguration`:{`bindingServiceReplicas`:" + i + ",`executionServiceReplicas`:" + i2 + ",`resilienceServiceReplicas`:" + i3
							+ "}, `repeats`:100,`experimentIterations`:1,`deployProcess`:false,`resetUBLServices`:true,`adhocServiceDeployment`:true,`simulateServicesUnvailability`:true,`simulateAdhocServicesUnvailability`:true,`instanceCreationInitialDelay`:2000,`instanceCreationInterval`:10000,`serviceUnavailabilityStartOffset`:10,`serviceUnavailabilityInitialDelay`:1000,`serviceUnavailabilityInterval`:10000,`serviceContainerUnavailabilityStartOffset`:20,`serviceContainerUnavailabilityInitialDelay`:2000,`serviceContainerUnavailabilityInterval`:60000,`adhocUblServicesReplicas`:3,`ublServicesReplicas`:5,`disableMonitoring`:false,`deploymentServers`:[`SERVER_150_162_6_63`,`SERVER_150_162_6_133`,`SERVER_150_162_6_194`,`SERVER_150_162_6_210`,`SERVER_150_162_6_147`],`processes`:[`br.ufsc.gsigma.architecture.experiments.billingwithcreditnote.BillingWithCreditNoteProcessExperiment6`,`br.ufsc.gsigma.architecture.experiments.createcatalogueprocess.CreateCatalogueProcessExperiment6`,`br.ufsc.gsigma.architecture.experiments.fulfilmentwithreceiptadvice.FulfilmentWithReceiptAdviceProcessExperiment6`,`br.ufsc.gsigma.architecture.experiments.orderingprocess.OrderingProcessExperiment6`,`br.ufsc.gsigma.architecture.experiments.paymentprocess.PaymentProcessExperiment6`]}\"";

					System.out.println(s);

				}
			}

		}

	}

}
