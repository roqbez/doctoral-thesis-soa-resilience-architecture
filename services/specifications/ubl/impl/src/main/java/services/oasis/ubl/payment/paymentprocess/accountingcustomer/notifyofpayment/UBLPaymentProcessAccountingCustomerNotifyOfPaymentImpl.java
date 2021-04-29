package services.oasis.ubl.payment.paymentprocess.accountingcustomer.notifyofpayment;

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
import oasis.names.specification.ubl.schema.xsd.remittanceadvice_2.RemittanceAdviceType;

public class UBLPaymentProcessAccountingCustomerNotifyOfPaymentImpl extends AbstractUBLService implements UBLPaymentProcessAccountingCustomerNotifyOfPayment {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLPaymentProcessAccountingCustomerNotifyOfPaymentImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public RemittanceAdviceType notifyOfPayment() {
		logRequest("notifyOfPayment", null);
		RemittanceAdviceType remittanceAdvice = new RemittanceAdviceType();
		return remittanceAdvice;
	}

	@Override
	public void notifyOfPaymentAsync(ProcessContext processContext) {

		logRequest("notifyOfPaymentAsync", processContext);

		NotifyOfPaymentAsyncCallback callback = new NotifyOfPaymentAsyncCallback();
		callback.processContext = processContext;
		callback.output = new RemittanceAdviceType();

		if (interactive)
			UIUtil.createQuestionUI("AccountingCustomer/notifyOfPaymentAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] {},
					new String[] { "OK" });
		else
			

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());
	}

	private void logRequest(String operation, ProcessContext processContext) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}
