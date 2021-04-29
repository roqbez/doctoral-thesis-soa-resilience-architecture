package services.oasis.ubl.ordering.orderingprocess.sellerparty.cancelorder;

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
import oasis.names.specification.ubl.schema.xsd.ordercancellation_2.OrderCancellationType;

public class UBLOrderingProcessSellerPartyCancelOrderImpl extends AbstractUBLService implements UBLOrderingProcessSellerPartyCancelOrder {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLOrderingProcessSellerPartyCancelOrderImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public String cancelOrder(OrderCancellationType input) {
		logRequest("cancelOrder", null, input);
		return "Order cancelled";
	}

	@Override
	public void cancelOrderAsync(ProcessContext processContext, OrderCancellationType input) {
		logRequest("cancelOrderAsync", processContext, input);

		CancelOrderAsyncCallback callback = new CancelOrderAsyncCallback();
		callback.processContext = processContext;
		callback.output = "Order cancelled";

		if (interactive)
			UIUtil.createQuestionUI("SellerParty/cancelOrderAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input },
					new String[] { "OK" });
		else
			

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());
	}

	private void logRequest(String operation, ProcessContext processContext, OrderCancellationType input) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinput=" + ReflectionToStringBuilder.toString(input));
		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}
}
