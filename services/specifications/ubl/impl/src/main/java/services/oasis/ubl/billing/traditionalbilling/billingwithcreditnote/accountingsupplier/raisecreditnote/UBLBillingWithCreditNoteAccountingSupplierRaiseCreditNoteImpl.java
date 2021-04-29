package services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.raisecreditnote;

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

public class UBLBillingWithCreditNoteAccountingSupplierRaiseCreditNoteImpl extends AbstractUBLService implements UBLBillingWithCreditNoteAccountingSupplierRaiseCreditNote {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLBillingWithCreditNoteAccountingSupplierRaiseCreditNoteImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public CreditNoteType raiseCreditNote(String inputDecision) {
		logRequest("raiseCreditNote", null, inputDecision);
		return new CreditNoteType();
	}

	@Override
	public void raiseCreditNoteAsync(ProcessContext processContext, String inputDecision) {

		logRequest("raiseCreditNoteAsync", processContext, inputDecision);

		RaiseCreditNoteAsyncCallback callback = new RaiseCreditNoteAsyncCallback();
		callback.processContext = processContext;

		callback.output = new CreditNoteType();

		if (interactive)
			UIUtil.createQuestionUI("AccountingSupplier/raiseCreditNoteAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { inputDecision }, new String[] { "OK" });

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());

	}

	private void logRequest(String operation, ProcessContext processContext, String inputDecision) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinputDecision=" + ReflectionToStringBuilder.toString(inputDecision));

		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}
