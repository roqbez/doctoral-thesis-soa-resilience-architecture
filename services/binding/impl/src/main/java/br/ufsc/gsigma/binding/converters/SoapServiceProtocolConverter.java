package br.ufsc.gsigma.binding.converters;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import javax.xml.soap.SOAPMessage;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

public class SoapServiceProtocolConverter implements ServiceProtocolConverter {

	@Override
	public String getName() {
		return "SOAP";
	}

	@Override
	public ConvertedMessage convertRequestMessage(SOAPMessage soapMessage, Map<String, Object> headers, ExecutionContext ectx, RequestBindingContext reqBindingContext) throws Exception {

		JAXBSerializerUtil.write(ectx, soapMessage.getSOAPHeader());
		JAXBSerializerUtil.write(reqBindingContext, soapMessage.getSOAPHeader());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		soapMessage.writeTo(baos);

		ConvertedMessage result = new ConvertedMessage(baos.toByteArray());

		result.getHeaders().put("Content-Type", "text/xml; charset=UTF-8");
		result.getHeaders().put("SOAPAction", headers.get("SOAPAction"));

		return result;
	}

	@Override
	public ConvertedMessage convertReplyMessage(InputStream in, Map<String, Object> headers) throws Exception {
		throw new UnsupportedOperationException("Not supported");
	}

}
