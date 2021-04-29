package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.deliveryparty.checkstatusofitems;

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

public class UBLFulfilmentWithReceiptAdviceDeliveryPartyCheckStatusOfItemsImpl extends AbstractUBLService implements UBLFulfilmentWithReceiptAdviceDeliveryPartyCheckStatusOfItems {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLFulfilmentWithReceiptAdviceDeliveryPartyCheckStatusOfItemsImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public OrderType checkStatusOfItems(OrderType input) {
		logRequest("adviseBuyerOfStatus", null, input);
		return input;
	}

	@Override
	public void checkStatusOfItemsAsync(ProcessContext processContext, OrderType input) {

		logRequest("checkStatusOfItemsAsync", processContext, input);

		CheckStatusOfItemsAsyncCallback callback = new CheckStatusOfItemsAsyncCallback();
		callback.processContext = processContext;
		callback.output = input;

		if (interactive)
			UIUtil.createQuestionUI("DeliverParty/checkStatusOfItemsAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input }, new String[] { "OK" });

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
