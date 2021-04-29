package services.oasis.ubl.payment.paymentprocess.accountingsupplier.notifypayee;

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

public class UBLPaymentProcessAccountingSupplierNotifyPayeeImpl extends AbstractUBLService implements UBLPaymentProcessAccountingSupplierNotifyPayee {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLPaymentProcessAccountingSupplierNotifyPayeeImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public RemittanceAdviceType notifyPayee(RemittanceAdviceType input) {
		logRequest("notifyPayee", null, input);
		return input;
	}

	@Override
	public void notifyPayeeAsync(ProcessContext processContext, RemittanceAdviceType input) {

		logRequest("notifyPayeeAsync", processContext, input);

		NotifyPayeeAsyncCallback callback = new NotifyPayeeAsyncCallback();
		callback.processContext = processContext;
		callback.output = input;

		if (interactive)
			UIUtil.createQuestionUI("AccountingCustomer/notifyPayeeAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input },
					new String[] { "OK" });
		else
			

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());
	}

	private void logRequest(String operation, ProcessContext processContext, RemittanceAdviceType input) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinput=" + ReflectionToStringBuilder.toString(input));
		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}
