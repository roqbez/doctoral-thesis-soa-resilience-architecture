package services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.validateresponse;

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
import oasis.names.specification.ubl.schema.xsd.applicationresponse_2.ApplicationResponseType;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.validateresponse.UBLBillingWithCreditNoteAccountingSupplierValidateResponse;
import services.oasis.ubl.billing.traditionalbilling.billingwithcreditnote.accountingsupplier.validateresponse.ValidateResponseAsyncCallback;

public class UBLBillingWithCreditNoteAccountingSupplierValidateResponseImpl extends AbstractUBLService implements UBLBillingWithCreditNoteAccountingSupplierValidateResponse {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLBillingWithCreditNoteAccountingSupplierValidateResponseImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public String validateResponse(String inputDecision, ApplicationResponseType inputApplicationResponse) {
		logRequest("validateResponse", null, inputDecision, inputApplicationResponse);

		String decision = "change".equals(inputDecision) ? "incorrectCharges" : "acceptCharges";

		logger.debug("decision --> " + decision);

		return decision;
	}

	@Override
	public void validateResponseAsync(ProcessContext processContext, String inputDecision, ApplicationResponseType inputApplicationResponse) {

		logRequest("validateResponseAsync", processContext, inputDecision, inputApplicationResponse);

		ValidateResponseAsyncCallback callback = new ValidateResponseAsyncCallback();
		callback.processContext = processContext;

		callback.outputDecision = "change".equals(inputDecision) ? "incorrectCharges" : "acceptCharges";

		logger.debug("decision --> " + callback.outputDecision);

		if (interactive)
			UIUtil.createQuestionUI("AccountingSupplier/validateResponseAsync" + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { inputDecision }, new String[] { "OK" });

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());

	}

	private void logRequest(String operation, ProcessContext processContext, String inputDecision, ApplicationResponseType inputApplicationResponse) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinputDecision=" + ReflectionToStringBuilder.toString(inputDecision));

		pw.println("\tinputApplicationResponse=" + ReflectionToStringBuilder.toString(inputApplicationResponse));

		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}
