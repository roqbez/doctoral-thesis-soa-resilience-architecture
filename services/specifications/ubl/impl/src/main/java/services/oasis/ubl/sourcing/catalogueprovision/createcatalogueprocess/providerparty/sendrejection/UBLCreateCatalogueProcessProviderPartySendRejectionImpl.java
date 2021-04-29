package services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.sendrejection;

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
import oasis.names.specification.ubl.schema.xsd.cataloguerequest_2.CatalogueRequestType;

public class UBLCreateCatalogueProcessProviderPartySendRejectionImpl extends AbstractUBLService implements UBLCreateCatalogueProcessProviderPartySendRejection {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLCreateCatalogueProcessProviderPartySendRejectionImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public ApplicationResponseType sendRejection(CatalogueRequestType input) {
		logRequest("sendRejection", null, input);
		return new ApplicationResponseType();
	}

	@Override
	public void sendRejectionAsync(ProcessContext processContext, CatalogueRequestType input) {
		logRequest("sendRejectionAsync", processContext, input);

		SendRejectionAsyncCallback callback = new SendRejectionAsyncCallback();
		callback.processContext = processContext;
		callback.output = new ApplicationResponseType();

		if (interactive)
			UIUtil.createQuestionUI("ProviderParty/sendRejectionAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input },
					new String[] { "OK" });
		else
			


		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());
	}

	private void logRequest(String operation, ProcessContext processContext, CatalogueRequestType input) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinput=" + ReflectionToStringBuilder.toString(input));
		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}
}
