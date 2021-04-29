package br.ufsc.gsigma.binding.converters;

import java.io.InputStream;
import java.util.Map;

import javax.xml.soap.SOAPMessage;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

public interface ServiceProtocolConverter {

	public String getName();

	public ConvertedMessage convertRequestMessage(SOAPMessage soapMessage, Map<String, Object> headers, ExecutionContext ectx, RequestBindingContext reqBindingContext) throws Exception;

	public ConvertedMessage convertReplyMessage(InputStream in, Map<String, Object> headers) throws Exception;

}
