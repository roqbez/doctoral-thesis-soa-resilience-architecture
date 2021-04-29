package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.buyerparty.acceptitems;

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

public class UBLFulfilmentWithReceiptAdviceBuyerPartyAcceptItemsImpl extends AbstractUBLService implements UBLFulfilmentWithReceiptAdviceBuyerPartyAcceptItems {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLFulfilmentWithReceiptAdviceBuyerPartyAcceptItemsImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public OrderType acceptItems(OrderType input) {
		logRequest("acceptItems", null, input);
		return input;
	}

	@Override
	public void acceptItemsAsync(ProcessContext processContext, OrderType input) {

		logRequest("acceptItemsAsync", processContext, input);

		AcceptItemsAsyncCallback callback = new AcceptItemsAsyncCallback();
		callback.processContext = processContext;
		callback.outputOrder = input;

		if (interactive)
			UIUtil.createQuestionUI("BuyerParty/acceptItemsAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input }, new String[] { "OK" });

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
