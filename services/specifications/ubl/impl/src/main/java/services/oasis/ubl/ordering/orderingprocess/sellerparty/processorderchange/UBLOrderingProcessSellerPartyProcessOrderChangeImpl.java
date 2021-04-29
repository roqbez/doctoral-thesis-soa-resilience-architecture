package services.oasis.ubl.ordering.orderingprocess.sellerparty.processorderchange;

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
import oasis.names.specification.ubl.schema.xsd.orderchange_2.OrderChangeType;

public class UBLOrderingProcessSellerPartyProcessOrderChangeImpl extends AbstractUBLService implements UBLOrderingProcessSellerPartyProcessOrderChange {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLOrderingProcessSellerPartyProcessOrderChangeImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public void processOrderChange(OrderChangeType input, Holder<OrderType> outputDocument, Holder<String> outputDecision) {

		logRequest("processOrderChange", null, input);

		outputDocument.value = new OrderType();

		outputDecision.value = getDecision("accepted", "rejected", "modified");
	}

	@Override
	public void processOrderChangeAsync(ProcessContext processContext, OrderChangeType input) {

		logRequest("processOrderChangeAsync", processContext, input);

		ProcessOrderChangeAsyncCallback callback = new ProcessOrderChangeAsyncCallback();
		callback.processContext = processContext;
		callback.outputDocument = new OrderType();

		String[] decisions = new String[] { "accepted", "rejected", "modified" };

		if (interactive)
			callback.outputDecision = UIUtil.createQuestionUI("SellerParty/processOrderAsync " + "[processId=" + processContext.getProcessId() + "]", "Please choose you option", new Object[] { input }, decisions);
		else {
			callback.outputDecision = getDecision(decisions);
		}

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
