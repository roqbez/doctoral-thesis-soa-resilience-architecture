package services.oasis.ubl.payment.paymentprocess.payeeparty.receiveadvice;

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

public class UBLPaymentProcessPayeePartyReceiveAdviceImpl extends AbstractUBLService implements UBLPaymentProcessPayeePartyReceiveAdvice {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLPaymentProcessPayeePartyReceiveAdviceImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public void receiveAdvice(RemittanceAdviceType inputFromAccountingCustomer, RemittanceAdviceType inputFromAccountingSupplier) {
		logRequest("receiveAdvice", null, inputFromAccountingCustomer, inputFromAccountingSupplier);
	}

	@Override
	public void receiveAdviceAsync(ProcessContext processContext, RemittanceAdviceType inputFromAccountingCustomer, RemittanceAdviceType inputFromAccountingSupplier) {

		logRequest("receiveAdviceAsync", processContext, inputFromAccountingCustomer, inputFromAccountingSupplier);

		ReceiveAdviceAsyncCallback callback = new ReceiveAdviceAsyncCallback();
		callback.processContext = processContext;

		if (interactive)
			UIUtil.createQuestionUI("PayeeParty/receiveAdviceAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process",
					new Object[] { inputFromAccountingCustomer, inputFromAccountingSupplier }, new String[] { "OK" });
		else
			

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());
	}

	private void logRequest(String operation, ProcessContext processContext, RemittanceAdviceType inputFromAccountingCustomer, RemittanceAdviceType inputFromAccountingSupplier) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinputFromAccountingCustomer=" + ReflectionToStringBuilder.toString(inputFromAccountingCustomer));
		pw.println("\tinputFromAccountingSupplier=" + ReflectionToStringBuilder.toString(inputFromAccountingSupplier));
		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}