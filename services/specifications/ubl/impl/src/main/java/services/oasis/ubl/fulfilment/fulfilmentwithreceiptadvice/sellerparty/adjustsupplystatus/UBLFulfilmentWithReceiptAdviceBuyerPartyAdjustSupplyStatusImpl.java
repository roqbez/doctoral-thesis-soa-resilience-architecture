package services.oasis.ubl.fulfilment.fulfilmentwithreceiptadvice.sellerparty.adjustsupplystatus;

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
import oasis.names.specification.ubl.schema.xsd.ordercancellation_2.OrderCancellationType;
import oasis.names.specification.ubl.schema.xsd.orderchange_2.OrderChangeType;
import oasis.names.specification.ubl.schema.xsd.receiptadvice_2.ReceiptAdviceType;

public class UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusImpl extends AbstractUBLService implements UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatus {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLFulfilmentWithReceiptAdviceBuyerPartyAdjustSupplyStatusImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public String adjustSupplyStatus(ReceiptAdviceType inputReceiptAdvice, OrderType inputOrder, OrderChangeType inputOrderChange, OrderCancellationType inputOrderCancellation) {
		logRequest("adjustSupplyStatus", null, inputReceiptAdvice, inputOrder, inputOrderChange, inputOrderCancellation);
		return "ok";
	}

	@Override
	public void adjustSupplyStatusAsync(ProcessContext processContext, ReceiptAdviceType inputReceiptAdvice, OrderType inputOrder, OrderChangeType inputOrderChange, OrderCancellationType inputOrderCancellation) {
		logRequest("adjustSupplyStatusAsync", processContext, inputReceiptAdvice, inputOrder, inputOrderChange, inputOrderCancellation);

		AdjustSupplyStatusAsyncCallback callback = new AdjustSupplyStatusAsyncCallback();
		callback.processContext = processContext;
		callback.output = "ok";

		if (interactive)
			UIUtil.createQuestionUI("DeliverParty/sendReceiptAdviceAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process",
					new Object[] { inputReceiptAdvice, inputOrder, inputOrderChange, inputOrderCancellation }, new String[] { "OK" });

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());

	}

	private void logRequest(String operation, ProcessContext processContext, ReceiptAdviceType inputReceiptAdvice, OrderType inputOrder, OrderChangeType inputOrderChange, OrderCancellationType inputOrderCancellation) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinputReceiptAdvice=" + ReflectionToStringBuilder.toString(inputReceiptAdvice));
		pw.println("\tinputOrder=" + ReflectionToStringBuilder.toString(inputOrder));
		pw.println("\tinputOrderChange=" + ReflectionToStringBuilder.toString(inputOrderChange));
		pw.println("\tinputOrderCancellation=" + ReflectionToStringBuilder.toString(inputOrderCancellation));

		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}
}
