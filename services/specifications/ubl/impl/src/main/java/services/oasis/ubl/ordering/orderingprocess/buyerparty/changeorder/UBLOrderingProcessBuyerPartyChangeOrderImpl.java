package services.oasis.ubl.ordering.orderingprocess.buyerparty.changeorder;

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
import oasis.names.specification.ubl.schema.xsd.orderchange_2.OrderChangeType;
import oasis.names.specification.ubl.schema.xsd.orderresponse_2.OrderResponseType;
import oasis.names.specification.ubl.schema.xsd.orderresponsesimple_2.OrderResponseSimpleType;

public class UBLOrderingProcessBuyerPartyChangeOrderImpl extends AbstractUBLService implements UBLOrderingProcessBuyerPartyChangeOrder {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLOrderingProcessBuyerPartyChangeOrderImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public OrderChangeType changeOrder(OrderResponseSimpleType inputOrderResponseSimple, OrderResponseType inputOrderResponse) {
		logRequest("changeOrder", null, inputOrderResponseSimple, inputOrderResponse);
		return new OrderChangeType();
	}


	@Override
	public void changeOrderAsync(ProcessContext processContext, OrderResponseSimpleType inputOrderResponseSimple, OrderResponseType inputOrderResponse) {
		logRequest("changeOrderAsync", processContext, inputOrderResponseSimple, inputOrderResponse);

		ChangeOrderAsyncCallback callback = new ChangeOrderAsyncCallback();
		callback.processContext = processContext;
		callback.output = new OrderChangeType();

		if (interactive)
			UIUtil.createQuestionUI("BuyerParty/changeOrderAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process",
					new Object[] { inputOrderResponseSimple, inputOrderResponse }, new String[] { "OK" });
		else
			

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());
	}

	private void logRequest(String operation, ProcessContext processContext, OrderResponseSimpleType inputOrderResponseSimple, OrderResponseType inputOrderResponse) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinputOrderResponseSimple=" + ReflectionToStringBuilder.toString(inputOrderResponseSimple));
		pw.println("\tinputOrderResponse=" + ReflectionToStringBuilder.toString(inputOrderResponse));
		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}
