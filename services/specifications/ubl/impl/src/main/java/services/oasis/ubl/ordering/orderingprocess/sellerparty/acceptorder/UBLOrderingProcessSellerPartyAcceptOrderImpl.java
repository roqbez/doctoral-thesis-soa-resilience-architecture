package services.oasis.ubl.ordering.orderingprocess.sellerparty.acceptorder;

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
import oasis.names.specification.ubl.schema.xsd.order_2.OrderType;
import oasis.names.specification.ubl.schema.xsd.orderresponse_2.OrderResponseType;
import oasis.names.specification.ubl.schema.xsd.orderresponsesimple_2.OrderResponseSimpleType;

public class UBLOrderingProcessSellerPartyAcceptOrderImpl extends AbstractUBLService implements UBLOrderingProcessSellerPartyAcceptOrder {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLOrderingProcessSellerPartyAcceptOrderImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public void acceptOrder(OrderType input, Holder<OrderResponseType> outputOrderResponse, Holder<OrderResponseSimpleType> outputOrderResponseSimple) {

		logRequest("acceptOrder", null, input);

		outputOrderResponseSimple.value = new OrderResponseSimpleType();
		outputOrderResponse.value = new OrderResponseType();

	}

	@Override
	public void acceptOrderAsync(ProcessContext processContext, OrderType input) {

		logRequest("acceptOrderAsync", processContext, input);

		AcceptOrderAsyncCallback callback = new AcceptOrderAsyncCallback();
		callback.processContext = processContext;

		callback.outputOrderResponseSimple = new OrderResponseSimpleType();
		callback.outputOrderResponse = new OrderResponseType();

		if (interactive)
			UIUtil.createQuestionUI("SellerParty/acceptOrderAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input },
					new String[] { "OK" });
		else
			

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
