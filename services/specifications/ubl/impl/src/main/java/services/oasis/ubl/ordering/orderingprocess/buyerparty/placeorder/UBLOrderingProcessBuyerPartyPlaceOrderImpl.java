package services.oasis.ubl.ordering.orderingprocess.buyerparty.placeorder;

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

public class UBLOrderingProcessBuyerPartyPlaceOrderImpl extends AbstractUBLService implements UBLOrderingProcessBuyerPartyPlaceOrder {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLOrderingProcessBuyerPartyPlaceOrderImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public OrderType placeOrder() {
		logRequest("placeOrder", null);
		OrderType order = new OrderType();
		return order;
	}

	@Override
	public void placeOrderAsync(ProcessContext processContext) {

		logRequest("placeOrderAsync", processContext);

		PlaceOrderAsyncCallback callback = new PlaceOrderAsyncCallback();
		callback.processContext = processContext;
		callback.output = new OrderType();

		if (interactive)
			UIUtil.createQuestionUI("BuyerParty/placeOrderAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] {}, new String[] { "OK" });
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
