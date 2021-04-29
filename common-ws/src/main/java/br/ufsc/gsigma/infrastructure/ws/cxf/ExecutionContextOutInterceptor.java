package br.ufsc.gsigma.infrastructure.ws.cxf;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Element;

import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

public class ExecutionContextOutInterceptor extends AbstractPhaseInterceptor<SoapMessage> {

	public ExecutionContextOutInterceptor() {
		super(Phase.WRITE);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {

		ExecutionContext executionContext = ExecutionContext.get();

		if (executionContext != null) {
			setExecutionContext(executionContext);
		}
	}

	public static void setExecutionContext(ExecutionContext executionContext) {
		try {
			Element element = JAXBSerializerUtil.toElement(executionContext.clone());
			CxfUtil.setRequestSoapHeader(ExecutionContext.QNAME, element);
			// executionContext.remove();
		} catch (Exception e) {
			throw new Fault(e);
		}
	}
}
