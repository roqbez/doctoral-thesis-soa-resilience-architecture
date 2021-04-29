package br.ufsc.gsigma.binding.converters;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.infrastructure.util.PayloadConverterUtil;
import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.infrastructure.ws.Headers;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

public class JsonServiceProtocolConverter implements ServiceProtocolConverter {

	private static final Logger logger = Logger.getLogger(JsonServiceProtocolConverter.class);

	@Override
	public String getName() {
		return "JSON";
	}

	@Override
	public ConvertedMessage convertRequestMessage(SOAPMessage soapMessage, Map<String, Object> headers, ExecutionContext ectx, RequestBindingContext reqBindingContext) throws Exception {

		byte[] outputJson = PayloadConverterUtil.xmlNodeToJson(soapMessage.getSOAPBody());

		ConvertedMessage result = new ConvertedMessage(outputJson);

		result.getHeaders().put("Content-Type", "application/json; charset=UTF-8");

		if (ectx != null) {
			result.getHeaders().put(Headers.HEADER_EXECUTION_CONTEXT, ectx.toBase64());
		}

		if (reqBindingContext != null) {
			result.getHeaders().put(Headers.HEADER_REQUEST_BINDING_CONTEXT, reqBindingContext.toBase64());
		}

		result.getRemovedHeaders().add("SOAPAction");

		if (logger.isDebugEnabled()) {
			logger.debug("Converting output message to JSON" //
					+ "\n\tinput=" + PayloadConverterUtil.xmlNodeToString(soapMessage.getSOAPBody()).trim().replaceAll("\n", "") //
					+ "\n\toutput=" + new String(outputJson).trim().replaceAll("\n", "") //
			);
		}

		return result;
	}

	@Override
	public ConvertedMessage convertReplyMessage(InputStream is, Map<String, Object> headers) throws Exception {

		String ns = (String) headers.get(Headers.HEADER_SERVICE_NAMESPACE);

		MessageFactory messageFactory = MessageFactory.newInstance();

		SOAPMessage soapMessage = messageFactory.createMessage();

		String executionContextBase64 = (String) headers.get(Headers.HEADER_EXECUTION_CONTEXT);

		if (executionContextBase64 != null) {
			JAXBSerializerUtil.write(ExecutionContext.fromBase64(executionContextBase64), soapMessage.getSOAPHeader());
		}

		String requestBindingContextBase64 = (String) headers.get(Headers.HEADER_REQUEST_BINDING_CONTEXT);

		if (requestBindingContextBase64 != null) {
			JAXBSerializerUtil.write(RequestBindingContext.fromBase64(requestBindingContextBase64), soapMessage.getSOAPHeader());
		}

		Node node = PayloadConverterUtil.jsonStreamToNode(is);

		NodeList childs = node.getChildNodes();

		Document doc = soapMessage.getSOAPBody().getOwnerDocument();

		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			Node n = doc.importNode(child, true);

			soapMessage.getSOAPBody().appendChild(n);

			if (ns != null && n instanceof Element) {
				doc.renameNode(n, ns, n.getNodeName());
			}

		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		soapMessage.writeTo(baos);

		byte[] payload = baos.toByteArray();

		return new ConvertedMessage(payload, headers);

	}

}
