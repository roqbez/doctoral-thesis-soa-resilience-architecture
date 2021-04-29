package services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.decideonaction;

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
import oasis.names.specification.ubl.schema.xsd.applicationresponse_2.ApplicationResponseType;
import oasis.names.specification.ubl.schema.xsd.catalogue_2.CatalogueType;

public class UBLCreateCatalogueProcessProviderPartyDecideOnActionImpl extends AbstractUBLService implements UBLCreateCatalogueProcessProviderPartyDecideOnAction {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLCreateCatalogueProcessProviderPartyDecideOnActionImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public void decideOnAction(CatalogueType inputCatalogue, ApplicationResponseType inputApplicationResponse, Holder<CatalogueType> outputDocument, Holder<String> outputDecision) {

		logRequest("decideOnAction", null, inputCatalogue, inputApplicationResponse);

		outputDocument.value = inputCatalogue;

		outputDecision.value = getDecision("reviseContent", "cancelTransaction");
	}

	@Override
	public void decideOnActionAsync(ProcessContext processContext, CatalogueType inputCatalogue, ApplicationResponseType inputApplicationResponse) {
		logRequest("decideOnActionAsync", processContext, inputCatalogue, inputApplicationResponse);

		DecideOnActionAsyncCallback callback = new DecideOnActionAsyncCallback();
		callback.processContext = processContext;
		callback.outputDocument = inputCatalogue;

		String[] decisions = new String[] { "reviseContent", "cancelTransaction" };

		if (interactive)
			callback.outputDecision = UIUtil.createQuestionUI("ProviderParty/decideOnActionAsync " + "[processId=" + processContext.getProcessId() + "]", "Please choose you option", new Object[] { inputCatalogue, inputApplicationResponse }, decisions);
		else {
			callback.outputDecision = getDecision(decisions);
		}

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());
	}

	private void logRequest(String operation, ProcessContext processContext, CatalogueType inputCatalogue, ApplicationResponseType inputApplicationResponse) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinputCatalogue=" + ReflectionToStringBuilder.toString(inputCatalogue));
		pw.println("\tinputApplicationResponse=" + ReflectionToStringBuilder.toString(inputApplicationResponse));
		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}
