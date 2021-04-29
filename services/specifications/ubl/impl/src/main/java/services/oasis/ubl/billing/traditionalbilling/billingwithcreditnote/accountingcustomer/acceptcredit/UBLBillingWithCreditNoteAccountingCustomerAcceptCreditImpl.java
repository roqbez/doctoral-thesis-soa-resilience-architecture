package services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.acceptcredit;

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
import oasis.names.specification.ubl.schema.xsd.creditnote_2.CreditNoteType;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.acceptcredit.AcceptCreditAsyncCallback;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingcustomer.acceptcredit.UBLBillingWithCreditNoteAccountingCustomerAcceptCredit;

public class UBLBillingWithCreditNoteAccountingCustomerAcceptCreditImpl extends AbstractUBLService implements UBLBillingWithCreditNoteAccountingCustomerAcceptCredit {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLBillingWithCreditNoteAccountingCustomerAcceptCreditImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public String acceptCredit(CreditNoteType input) {
		logRequest("acceptCredit", null, input);
		return "acceptCredit";
	}

	@Override
	public void acceptCreditAsync(ProcessContext processContext, CreditNoteType input) {

		logRequest("acceptCreditAsync", processContext, input);

		AcceptCreditAsyncCallback callback = new AcceptCreditAsyncCallback();
		callback.processContext = processContext;
		callback.output = "acceptCredit";

		if (interactive)
			UIUtil.createQuestionUI("AccountingCustomer/acceptChargesAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input }, new String[] { "OK" });

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());

	}

	private void logRequest(String operation, ProcessContext processContext, CreditNoteType input) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinput=" + ReflectionToStringBuilder.toString(input));
		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}
}
