package br.ufsc.gsigma.services.specifications.ubl.cxf;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Element;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;

public class RequestBindingContextInInterceptor extends AbstractPhaseInterceptor<SoapMessage> {

	public RequestBindingContextInInterceptor() {
		super(Phase.PRE_INVOKE);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {

		RequestBindingContext requestBindingContext = readRequestBindingContext(message);

		if (requestBindingContext != null) {
			RequestBindingContext.set(requestBindingContext);
		}

	}

	private static RequestBindingContext readRequestBindingContext(SoapMessage soapMessage) {

		Element element = CxfUtil.getRequestSoapHeaderElement(soapMessage, RequestBindingContext.QNAME);

		if (element != null) {
			try {
				return JAXBSerializerUtil.read(RequestBindingContext.class, element);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

}
