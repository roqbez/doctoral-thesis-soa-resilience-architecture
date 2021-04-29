package services.oasis.ubl.ordering.orderingprocess.sellerparty.changeorder;

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
import oasis.names.specification.ubl.schema.xsd.orderchange_2.OrderChangeType;

public class UBLOrderingProcessSellerPartyChangeOrderImpl extends AbstractUBLService implements UBLOrderingProcessSellerPartyChangeOrder {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLOrderingProcessSellerPartyChangeOrderImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public OrderType changeOrder(OrderChangeType input) {
		logRequest("changeOrder", null, input);
		return new OrderType();
	}

	@Override
	public void changeOrderAsync(ProcessContext processContext, OrderChangeType input) {

		logRequest("changeOrderAsync", processContext, input);

		ChangeOrderAsyncCallback callback = new ChangeOrderAsyncCallback();
		callback.processContext = processContext;
		callback.output = new OrderType();

		if (interactive)
			UIUtil.createQuestionUI("SellerParty/changeOrderAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input },
					new String[] { "OK" });
		else
			

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());
	}

	private void logRequest(String operation, ProcessContext processContext, OrderChangeType input) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinput=" + ReflectionToStringBuilder.toString(input));
		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}
