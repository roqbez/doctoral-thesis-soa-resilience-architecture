package br.ufsc.gsigma.infrastructure.ws.cxf;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Element;

import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

public class ExecutionContextInInterceptor extends AbstractPhaseInterceptor<SoapMessage> {

	public ExecutionContextInInterceptor() {
		super(Phase.PRE_INVOKE);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {

		ExecutionContext executionContext = readExecutionContext(message);

		if (executionContext != null) {
			ExecutionContext.set(executionContext);
		}

	}

	private static ExecutionContext readExecutionContext(SoapMessage soapMessage) {

		Element element = CxfUtil.getRequestSoapHeaderElement(soapMessage, ExecutionContext.QNAME);

		if (element != null) {
			try {
				return JAXBSerializerUtil.read(ExecutionContext.class, element);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

}
