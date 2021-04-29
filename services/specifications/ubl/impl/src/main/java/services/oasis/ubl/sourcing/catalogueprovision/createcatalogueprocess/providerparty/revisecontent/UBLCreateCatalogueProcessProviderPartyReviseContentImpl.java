package services.oasis.ubl.sourcing.catalogueprovision.createcatalogueprocess.providerparty.revisecontent;

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
import oasis.names.specification.ubl.schema.xsd.cataloguerequest_2.CatalogueRequestType;

public class UBLCreateCatalogueProcessProviderPartyReviseContentImpl extends AbstractUBLService implements UBLCreateCatalogueProcessProviderPartyReviseContent {

	private final Logger logger = Logger.getLogger(getClass());

	private boolean interactive;

	public UBLCreateCatalogueProcessProviderPartyReviseContentImpl(boolean interactive) {
		this.interactive = interactive;
	}

	@Override
	public CatalogueRequestType reviseContent(CatalogueType input) {
		logRequest("reviseContent", null, input);
		return new CatalogueRequestType();
	}

	@Override
	public void reviseContentAsync(ProcessContext processContext, CatalogueType input) {

		logRequest("reviseContentAsync", processContext, input);

		ReviseContentAsyncCallback callback = new ReviseContentAsyncCallback();
		callback.processContext = processContext;
		callback.output = new CatalogueRequestType();

		if (interactive)
			UIUtil.createQuestionUI("ProviderParty/reviseContentAsync " + "[processId=" + processContext.getProcessId() + "]", "Choose OK to continue the process", new Object[] { input },
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
