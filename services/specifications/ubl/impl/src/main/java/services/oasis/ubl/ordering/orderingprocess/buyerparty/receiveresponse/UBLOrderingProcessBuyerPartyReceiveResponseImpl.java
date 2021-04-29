package services.oasis.ubl.ordering.orderingprocess.buyerparty.receiveresponse;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.ws.Holder;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.processcontext.ProcessContext;
import br.ufsc.gsigma.services.specifications.ubl.impl.AbstractUBLService;
import br.ufsc.gsigma.services.specifications.ubl.util.ReplyUtil;
import br.ufsc.gsigma.services.specifications.ubl.util.UIUtil;
import oasis.names.specification.ubl.schema.xsd.orderresponse_2.OrderResponseType;
import oasis.names.specification.ubl.schema.xsd.orderresponsesimple_2.OrderResponseSimpleType;

public class UBLOrderingProcessBuyerPartyReceiveResponseImpl extends AbstractUBLService implements UBLOrderingProcessBuyerPartyReceiveResponse {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLOrderingProcessBuyerPartyReceiveResponseImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public void receiveResponse(OrderResponseSimpleType inputOrderResponseSimple, OrderResponseType inputOrderResponse, Holder<OrderResponseSimpleType> outputOrderResponseSimple, Holder<OrderResponseType> outputOrderResponse, Holder<String> outputDecision) {

		logRequest("receiveResponse", null, inputOrderResponseSimple, inputOrderResponse);

		outputDecision.value = getDecision("acceptOrder", "rejectOrder", "changeOrder", "cancelOrder");

		outputOrderResponseSimple.value = inputOrderResponseSimple;
		outputOrderResponse.value = inputOrderResponse;
	}

	@Override
	public void receiveResponseAsync(ProcessContext processContext, OrderResponseSimpleType inputOrderResponseSimple, OrderResponseType inputOrderResponse) {

		logRequest("receiveResponseAsync", processContext, inputOrderResponseSimple, inputOrderResponse);

		ReceiveResponseAsyncCallback callback = new ReceiveResponseAsyncCallback();
		callback.processContext = processContext;

		String[] decisions = new String[] { "acceptOrder", "rejectOrder", "changeOrder", "cancelOrder" };

		callback.outputOrderResponseSimple = inputOrderResponseSimple;
		callback.outputOrderResponse = inputOrderResponse;

		if (interactive)
			callback.outputDecision = UIUtil.createQuestionUI("BuyerParty/receiveResponseAsync " + "[processId=" + processContext.getProcessId() + "]", "Please choose you option", new Object[] { inputOrderResponseSimple, inputOrderResponse }, decisions);
		else {
			callback.outputDecision = getDecision(decisions);
		}

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
