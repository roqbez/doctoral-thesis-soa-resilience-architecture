package br.ufsc.gsigma.services.specifications.ubl.cxf;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Element;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfUtil;

public class RequestBindingContextOutInterceptor extends AbstractPhaseInterceptor<SoapMessage> {

	public RequestBindingContextOutInterceptor() {
		super(Phase.WRITE);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		RequestBindingContext requestBindingContext = RequestBindingContext.get(false);
		setRequestBindingContext(requestBindingContext);
	}

	public static void setRequestBindingContext(RequestBindingContext requestBindingContext) {
		if (requestBindingContext != null) {
			try {
				Element element = JAXBSerializerUtil.toElement(requestBindingContext.clone());
				CxfUtil.setRequestSoapHeader(ExecutionContext.QNAME, element);
			} catch (Exception e) {
				throw new Fault(e);
			}
		}
	}
}
