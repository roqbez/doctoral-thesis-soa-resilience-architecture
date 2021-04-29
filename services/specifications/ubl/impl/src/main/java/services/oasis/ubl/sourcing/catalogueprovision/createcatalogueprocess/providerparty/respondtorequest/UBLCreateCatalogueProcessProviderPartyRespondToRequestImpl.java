package services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.respondtorequest;

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
import oasis.names.specification.ubl.schema.xsd.cataloguerequest_2.CatalogueRequestType;

public class UBLCreateCatalogueProcessProviderPartyRespondToRequestImpl extends AbstractUBLService implements UBLCreateCatalogueProcessProviderPartyRespondToRequest {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLCreateCatalogueProcessProviderPartyRespondToRequestImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public CatalogueRequestType respondToRequest(CatalogueRequestType input) {
		logRequest("respondToRequest", null, input);
		return input;
	}

	@Override
	public void respondToRequestAsync(ProcessContext processContext, CatalogueRequestType input) {
		logRequest("respondToRequestAsync", processContext, input);

		RespondToRequestAsyncCallback callback = new RespondToRequestAsyncCallback();
		callback.processContext = processContext;
		callback.output = input;

		if (interactive)
			UIUtil.createQuestionUI("ProviderParty/respondToRequestAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input },
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
