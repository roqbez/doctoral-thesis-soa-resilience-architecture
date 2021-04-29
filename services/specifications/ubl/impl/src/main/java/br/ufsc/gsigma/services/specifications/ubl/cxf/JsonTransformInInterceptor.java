package br.ufsc.gsigma.services.specifications.ubl.cxf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.io.DelegatingInputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.infrastructure.util.PayloadConverterUtil;
import br.ufsc.gsigma.infrastructure.util.xml.JAXBSerializerUtil;
import br.ufsc.gsigma.infrastructure.ws.Headers;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

public class JsonTransformInInterceptor extends AbstractPhaseInterceptor<Message> {

	private static final Logger logger = Logger.getLogger(JsonTransformInInterceptor.class);

	public static final JsonTransformInInterceptor INSTANCE = new JsonTransformInInterceptor();

	private JsonTransformInInterceptor() {
		super(Phase.POST_STREAM);
		addBefore(StaxInInterceptor.class.getName());
	}

	@Override
	public void handleMessage(Message message) throws Fault {

		HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);

		if (StringUtils.startsWith(request.getHeader("Content-Type"), "application/json")) {

			message.put("JSON_REQUEST", true);

			DelegatingInputStream is = message.getContent(DelegatingInputStream.class);

			if (is != null) {

				try {

					String ns = request.getHeader(Headers.HEADER_SERVICE_NAMESPACE);

					MessageFactory messageFactory = MessageFactory.newInstance();

					SOAPMessage soapMessage = messageFactory.createMessage();

					String executionContextBase64 = request.getHeader(Headers.HEADER_EXECUTION_CONTEXT);

					if (executionContextBase64 != null) {
						JAXBSerializerUtil.write(ExecutionContext.fromBase64(executionContextBase64), soapMessage.getSOAPHeader());
					}

					String requestBindingContextBase64 = request.getHeader(Headers.HEADER_REQUEST_BINDING_CONTEXT);

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

					is.setInputStream(new ByteArrayInputStream(payload));

					org.eclipse.jetty.server.Request jettyRequest = (Request) request;
					jettyRequest.setContentType("text/xml; charset=UTF-8");

					jettyRequest.getMetaData().getFields().put(HttpHeader.CONTENT_LENGTH.toString(), String.valueOf(payload.length));

					if (logger.isDebugEnabled()) {
						logger.debug("Converting JSON message to XML" //
								+ "\n\t" + new String(payload).trim().replaceAll("\n", ""));
					}

				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			}

		}
	}

}
