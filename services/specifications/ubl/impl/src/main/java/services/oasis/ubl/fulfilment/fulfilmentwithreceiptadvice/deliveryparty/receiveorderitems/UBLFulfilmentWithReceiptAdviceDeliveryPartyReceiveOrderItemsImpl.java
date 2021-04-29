package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.receiveorderitems;

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

public class UBLFulfilmentWithReceiptAdviceDeliveryPartyReceiveOrderItemsImpl extends AbstractUBLService implements UBLFulfilmentWithReceiptAdviceDeliveryPartyReceiveOrderItems {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLFulfilmentWithReceiptAdviceDeliveryPartyReceiveOrderItemsImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public OrderType receiveOrderItems() {
		logRequest("receiveOrderItems", null);
		return new OrderType();
	}

	@Override
	public void receiveOrderItemsAsync(ProcessContext processContext) {

		logRequest("receiveOrderItemsAsync", processContext);

		ReceiveOrderItemsAsyncCallback callback = new ReceiveOrderItemsAsyncCallback();
		callback.processContext = processContext;
		callback.output = new OrderType();

		if (interactive)
			UIUtil.createQuestionUI("DeliverParty/receiveOrderItemsAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] {}, new String[] { "OK" });

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
