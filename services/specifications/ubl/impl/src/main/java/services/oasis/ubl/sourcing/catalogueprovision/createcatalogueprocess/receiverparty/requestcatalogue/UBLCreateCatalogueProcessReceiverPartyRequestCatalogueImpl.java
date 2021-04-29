package services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.receiverparty.requestcatalogue;

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

public class UBLCreateCatalogueProcessReceiverPartyRequestCatalogueImpl extends AbstractUBLService implements UBLCreateCatalogueProcessReceiverPartyRequestCatalogue {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLCreateCatalogueProcessReceiverPartyRequestCatalogueImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public CatalogueRequestType requestCatalogue() {
		logRequest("requestCatalogue", null);
		return new CatalogueRequestType();
	}

	@Override
	public void requestCatalogueAsync(ProcessContext processContext) {
		logRequest("requestCatalogueAsync", processContext);

		RequestCatalogueAsyncCallback callback = new RequestCatalogueAsyncCallback();
		callback.processContext = processContext;
		callback.output = new CatalogueRequestType();

		if (interactive)
			UIUtil.createQuestionUI("ReceiverParty/requestCatalogueAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] {},
					new String[] { "OK" });
		else
			

		ReplyUtil.sendReplySoapMessage(interactive, logger, callback, processContext.getCallbackEndpoint(), processContext.getCallbackSOAPAction());
	}

	private void logRequest(String operation, ProcessContext processContext) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Invoking operation: " + operation);

		if (processContext != null)
			pw.println("\tprocessContext=" + ReflectionToStringBuilder.toString(processContext));

		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_INVOKE, sw.toString()));
	}

}
