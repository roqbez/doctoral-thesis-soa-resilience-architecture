package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.sendreceiptadvice;

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
import oasis.names.specification.ubl.schema.xsd.order_2.OrderType;
import oasis.names.specification.ubl.schema.xsd.receiptadvice_2.ReceiptAdviceType;

public class UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceImpl extends AbstractUBLService implements UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdvice {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLFulfilmentWithReceiptAdviceDeliveryPartySendReceiptAdviceImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public ReceiptAdviceType sendReceiptAdvice(OrderType input) {
		logRequest("sendReceiptAdvice", null, input);
		return new ReceiptAdviceType();
	}

	@Override
	public void sendReceiptAdviceAsync(ProcessContext processContext, OrderType input) {

		logRequest("checkStatusOfItemsAsync", processContext, input);

		SendReceiptAdviceAsyncCallback callback = new SendReceiptAdviceAsyncCallback();
		callback.processContext = processContext;
		callback.output = new ReceiptAdviceType();

		if (interactive)
			UIUtil.createQuestionUI("DeliverParty/sendReceiptAdviceAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input }, new String[] { "OK" });

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());
	}

	private void logRequest(String operation, ProcessContext processContext, OrderType input) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinput=" + ReflectionToStringBuilder.toString(input));
		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}
}
