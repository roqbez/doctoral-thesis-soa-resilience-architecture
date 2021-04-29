package services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.acceptcatalogue;

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
import oasis.names.specification.ubl.schema.xsd.catalogue_2.CatalogueType;

public class UBLCreateCatalogueProcessReceiverPartyAcceptCatalogueImpl extends AbstractUBLService implements UBLCreateCatalogueProcessReceiverPartyAcceptCatalogue {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLCreateCatalogueProcessReceiverPartyAcceptCatalogueImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public String acceptCatalogue(CatalogueType input) {
		logRequest("acceptCatalogue", null, input);
		return "accepted";
	}

	@Override
	public void acceptCatalogueAsync(ProcessContext processContext, CatalogueType input) {
		logRequest("acceptCatalogueAsync", processContext, input);

		AcceptCatalogueAsyncCallback callback = new AcceptCatalogueAsyncCallback();
		callback.processContext = processContext;
		callback.output = "accepted";

		if (interactive)
			UIUtil.createQuestionUI("ReceiverParty/acceptCatalogueAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input },
					new String[] { "OK" });
		else
			

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());
	}

	private void logRequest(String operation, ProcessContext processContext, CatalogueType input) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		pw.println("\tinput=" + ReflectionToStringBuilder.toString(input));
		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}
