package br.ufsc.gsigma.services.specifications.ubl.cxf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import javax.xml.soap.SOAPMessage;

import org.apache.activemq.util.ByteArrayOutputStream;
import org.apache.cxf.binding.soap.saaj.SAAJFactoryResolver;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.infrastructure.util.PayloadConverterUtil;
import br.ufsc.gsigma.infrastructure.ws.Headers;
import br.ufsc.gsigma.infrastructure.ws.WSMessageUtils;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

public class JsonTransformOutInterceptor extends AbstractPhaseInterceptor<Message> {

	public static final JsonTransformOutInterceptor INSTANCE = new JsonTransformOutInterceptor();

	public static final String ORIGINAL_OUTPUT_STREAM_KEY = JsonTransformOutInterceptor.class.getName() + ".originalOutputStream";

	private JsonTransformOutInterceptor() {
		super(Phase.PREPARE_SEND);
		addAfter(MessageSenderInterceptor.class.getName());
	}

	@Override
	public void handleMessage(Message message) throws Fault {

		final OutputStream os = message.getContent(OutputStream.class);

		if (os == null) {
			return;
		}

		message.put(Message.CONTENT_TYPE, "application/json; charset=UTF-8");

		message.put(ORIGINAL_OUTPUT_STREAM_KEY, os);

		ByteArrayOutputStream baos = new ByteArrayOutputStream() {
			@Override
			@SuppressWarnings("unchecked")
			public void close() throws IOException {

				try {

					byte[] xmlBytes = toByteArray();

					SOAPMessage soapMessage = SAAJFactoryResolver.createMessageFactory(null).createMessage(null, new ByteArrayInputStream(xmlBytes));

					byte[] jsonBytes = PayloadConverterUtil.xmlNodeToJson(soapMessage.getSOAPBody());

					Map<String, Object> headers = (Map<String, Object>) message.get(Message.PROTOCOL_HEADERS);

					String ns = soapMessage.getSOAPBody().getFirstChild().getNamespaceURI();

					headers.put(Headers.HEADER_SERVICE_NAMESPACE, Collections.singletonList(ns));

					headers.put(Headers.HEADER_SERVICE_PROTOCOL, Collections.singletonList("br.ufsc.gsigma.binding.converters.JsonServiceProtocolConverter"));

					ExecutionContext executionContext = WSMessageUtils.getObjectFromSoapHeader(soapMessage, ExecutionContext.QNAME, ExecutionContext.class);

					RequestBindingContext requestBindingContext = WSMessageUtils.getObjectFromSoapHeader(soapMessage, RequestBindingContext.QNAME, RequestBindingContext.class);

					if (executionContext != null) {
						headers.put(Headers.HEADER_EXECUTION_CONTEXT, Collections.singletonList(executionContext.toBase64()));
					}

					if (requestBindingContext != null) {
						headers.put(Headers.HEADER_REQUEST_BINDING_CONTEXT, Collections.singletonList(requestBindingContext.toBase64()));
					}

					os.write(jsonBytes);
					os.close();

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		message.setContent(OutputStream.class, baos);
	}

}
