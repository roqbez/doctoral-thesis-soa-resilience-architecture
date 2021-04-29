package services.oasis.ubl.ordering.orderingprocess.sellerparty.processorder;

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

public class UBLOrderingProcessSellerPartyProcessOrderImpl extends AbstractUBLService implements UBLOrderingProcessSellerPartyProcessOrder {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLOrderingProcessSellerPartyProcessOrderImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public void processOrder(OrderType input, Holder<OrderType> outputDocument, Holder<String> outputDecision) {

		logRequest("processOrder", null, input);

		outputDocument.value = input;

		outputDecision.value = getDecision("accepted", "rejected", "modified");
	}

	@Override
	public void processOrderAsync(ProcessContext processContext, OrderType input) {

		logRequest("processOrderAsync", processContext, input);

		ProcessOrderAsyncCallback callback = new ProcessOrderAsyncCallback();
		callback.processContext = processContext;
		callback.outputDocument = input;

		String[] decisions = new String[] { "accepted", "rejected", "modified" };

		if (interactive)
			callback.outputDecision = UIUtil.createQuestionUI("SellerParty/processOrderAsync " + "[processId=" + processContext.getProcessId() + "]", "Please choose you option", new Object[] { input }, decisions);
		else {
			callback.outputDecision = getDecision(decisions);
		}

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
