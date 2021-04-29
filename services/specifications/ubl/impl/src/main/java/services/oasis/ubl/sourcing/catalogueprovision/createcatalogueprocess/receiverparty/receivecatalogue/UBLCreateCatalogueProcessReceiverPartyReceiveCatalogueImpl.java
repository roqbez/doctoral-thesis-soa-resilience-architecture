package services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.receivecatalogue;

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
import oasis.names.specification.ubl.schema.xsd.catalogue_2.CatalogueType;

public class UBLCreateCatalogueProcessReceiverPartyReceiveCatalogueImpl extends AbstractUBLService implements UBLCreateCatalogueProcessReceiverPartyReceiveCatalogue {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLCreateCatalogueProcessReceiverPartyReceiveCatalogueImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public CatalogueType receiveCatalogue(CatalogueType inputCatalogue, ApplicationResponseType inputApplicationResponse) {
		logRequest("receiveCatalogue", null, inputCatalogue, inputApplicationResponse);
		return inputCatalogue;
	}

	@Override
	public void receiveCatalogueAsync(ProcessContext processContext, CatalogueType inputCatalogue, ApplicationResponseType inputApplicationResponse) {

		logRequest("receiveCatalogueAsync", processContext, inputCatalogue, inputApplicationResponse);

		ReceiveCatalogueAsyncCallback callback = new ReceiveCatalogueAsyncCallback();
		callback.processContext = processContext;
		callback.output = inputCatalogue;

		if (interactive)
			UIUtil.createQuestionUI("ReceiverParty/receiveCatalogueAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process",
					new Object[] { inputCatalogue, inputApplicationResponse }, new String[] { "OK" });
		else
			

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
