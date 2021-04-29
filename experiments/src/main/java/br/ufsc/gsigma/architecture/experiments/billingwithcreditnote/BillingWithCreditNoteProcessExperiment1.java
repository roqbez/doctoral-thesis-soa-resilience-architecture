package br.ufsc.gsigma.architecture.experiments.billingwithcreditnote;

import br.ufsc.gsigma.architecture.experiments.ExecutionExperimentParams;

public class BillingWithCreditNoteProcessExperiment1 extends AbstractBillingWithCreditNoteProcessExperiment {

	private static final int REPEATS = 100;

	private static final String EXECUTION_ID = "exp1-bs1-exs1-rs0";

	public static void main(String[] args) throws Exception {
		new BillingWithCreditNoteProcessExperiment1().executeInternal(new ExecutionExperimentParams(EXECUTION_ID, REPEATS, true, true, null, null));
	}

}
