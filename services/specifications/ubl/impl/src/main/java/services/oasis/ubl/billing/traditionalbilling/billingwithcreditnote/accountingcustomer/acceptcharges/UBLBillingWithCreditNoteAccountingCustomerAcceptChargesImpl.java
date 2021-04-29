package services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.acceptcharges;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.processcontext.ProcessContext;
import br.ufsc.gsigma.services.specifications.ubl.impl.AbstractUBLService;
import br.ufsc.gsigma.services.specifications.ubl.util.ReplyUtil;
import br.ufsc.gsigma.services.specifications.ubl.util.UIUtil;
import oasis.names.specification.ubl.schema.xsd.invoice_2.InvoiceType;

public class UBLBillingWithCreditNoteAccountingCustomerAcceptChargesImpl extends AbstractUBLService implements UBLBillingWithCreditNoteAccountingCustomerAcceptCharges {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLBillingWithCreditNoteAccountingCustomerAcceptChargesImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public String acceptCharges(InvoiceType input) {
		logRequest("acceptCharges", null, input);
		return "acceptCharges";
	}

	@Override
	public void acceptChargesAsync(ProcessContext processContext, InvoiceType input) {

		logRequest("acceptItemsAsync", processContext, input);

		AcceptChargesAsyncCallback callback = new AcceptChargesAsyncCallback();
		callback.processContext = processContext;
		callback.output = "acceptCharges";

		if (interactive)
			UIUtil.createQuestionUI("AccountingCustomer/acceptChargesAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input }, new String[] { "OK" });

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());

	}

	private void logRequest(String operation, ProcessContext processContext, InvoiceType input) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinput=" + ReflectionToStringBuilder.toString(input));
		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}
