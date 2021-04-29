package services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.reconcilecharges;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.ws.Holder;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.processcontext.ProcessContext;
import br.ufsc.gsigma.services.specifications.ubl.impl.AbstractUBLService;
import br.ufsc.gsigma.services.specifications.ubl.util.ReplyUtil;
import br.ufsc.gsigma.services.specifications.ubl.util.UIUtil;
import oasis.names.specification.ubl.schema.xsd.creditnote_2.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_2.InvoiceType;

public class UBLBillingWithCreditNoteAccountingCustomerReconcileChargesImpl extends AbstractUBLService implements UBLBillingWithCreditNoteAccountingCustomerReconcileCharges {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLBillingWithCreditNoteAccountingCustomerReconcileChargesImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public void reconcileCharges(InvoiceType inputInvoice, CreditNoteType inputCreditNote, Holder<String> outputDecision, Holder<InvoiceType> outputInvoice, Holder<CreditNoteType> outputCreditNote) {

		logRequest("reconcileCharges", null, inputInvoice, inputCreditNote);

		outputDecision.value = getDecision("acceptCredit", "acceptCharges", "change");

		outputInvoice.value = inputInvoice;
		outputCreditNote.value = inputCreditNote;
	}

	@Override
	public void reconcileChargesAsync(ProcessContext processContext, InvoiceType inputInvoice, CreditNoteType inputCreditNote) {

		logRequest("reconcileCharges", processContext, inputInvoice, inputCreditNote);

		ReconcileChargesAsyncCallback callback = new ReconcileChargesAsyncCallback();
		callback.processContext = processContext;

		callback.outputInvoice = inputInvoice;
		callback.outputCreditNote = inputCreditNote;

		if (interactive)
			callback.outputDecision = UIUtil.createQuestionUI("AccountingCustomer/determineActionAsync " + "[processId=" + processContext.getProcessId() + "]", "Please choose you option", new Object[] { inputInvoice, inputCreditNote }, new String[] { "acceptCredit", "acceptCharges", "change" });
		else {
			callback.outputDecision = getDecision("acceptCredit", "acceptCharges", "change");
		}

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());

	}

	private void logRequest(String operation, ProcessContext processContext, InvoiceType inputInvoice, CreditNoteType inputCreditNote) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinputInvoice=" + ReflectionToStringBuilder.toString(inputInvoice));
		pw.println("\tinputCreditNote=" + ReflectionToStringBuilder.toString(inputCreditNote));

		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}
