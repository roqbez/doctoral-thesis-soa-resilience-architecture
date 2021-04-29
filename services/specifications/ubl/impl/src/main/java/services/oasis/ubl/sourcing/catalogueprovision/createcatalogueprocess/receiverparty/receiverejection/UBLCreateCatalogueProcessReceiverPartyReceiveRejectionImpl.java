package services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.receiverejection;

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
import oasis.names.specification.ubl.schema.xsd.applicationresponse_2.ApplicationResponseType;

public class UBLCreateCatalogueProcessReceiverPartyReceiveRejectionImpl extends AbstractUBLService implements UBLCreateCatalogueProcessReceiverPartyReceiveRejection {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLCreateCatalogueProcessReceiverPartyReceiveRejectionImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public String receiveRejection(ApplicationResponseType input) {
		logRequest("receiveRejection", null, input);
		return "Rejected";
	}

	@Override
	public void receiveRejectionAsync(ProcessContext processContext, ApplicationResponseType input) {
		logRequest("receiveRejectionAsync", processContext, input);

		ReceiveRejectionAsyncCallback callback = new ReceiveRejectionAsyncCallback();
		callback.processContext = processContext;
		callback.output = "Rejected";

		if (interactive)
			UIUtil.createQuestionUI("ReceiverParty/receiveRejectionAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input },
					new String[] { "OK" });
		else
			

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());
	}

	private void logRequest(String operation, ProcessContext processContext, ApplicationResponseType input) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinput=" + ReflectionToStringBuilder.toString(input));
		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}
