package br.ufsc.gsigma.binding.camel;

import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.DelegatingInputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;

import br.ufsc.gsigma.binding.converters.ConvertedMessage;
import br.ufsc.gsigma.binding.converters.ServiceProtocolConverter;
import br.ufsc.gsigma.binding.converters.ServiceProtocolConverterFactory;
import br.ufsc.gsigma.infrastructure.ws.Headers;

public class ServiceProtocolConverterInInterceptor extends AbstractPhaseInterceptor<Message> {

	private static final Logger logger = Logger.getLogger(ServiceProtocolConverterInInterceptor.class);

	public ServiceProtocolConverterInInterceptor() {
		super(Phase.POST_STREAM);
		addBefore(CacheInputStreamInInterceptor.class.getName());
	}

	@Override
	public void handleMessage(Message message) throws Fault {

		HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);

		String protocol = request.getHeader(Headers.HEADER_SERVICE_PROTOCOL);

		String serviceReply = request.getHeader(Headers.HEADER_SERVICE_REPLY);

		if (!StringUtils.isBlank(protocol) && "true".equals(serviceReply)) {

			DelegatingInputStream is = message.getContent(DelegatingInputStream.class);

			if (is != null) {

				try {

					ServiceProtocolConverter converter = ServiceProtocolConverterFactory.get(protocol);

					Map<String, Object> headers = new HashMap<String, Object>();

					Enumeration<String> en = request.getHeaderNames();

					while (en.hasMoreElements()) {
						String header = en.nextElement();
						headers.put(header, request.getHeader(header));
					}

					ConvertedMessage convertedMessage = converter.convertReplyMessage(is, headers);

					is.setInputStream(new ByteArrayInputStream(convertedMessage.getPayload()));

					org.eclipse.jetty.server.Request jettyRequest = (Request) request;
					jettyRequest.setContentType("text/xml; charset=UTF-8");

					jettyRequest.getMetaData().getFields().put(HttpHeader.CONTENT_LENGTH.toString(), String.valueOf(convertedMessage.getPayload().length));

					if (logger.isDebugEnabled()) {
						logger.debug("Converting JSON message to XML" //
								+ "\n\t" + new String(convertedMessage.getPayload()).trim().replaceAll("\n", ""));
					}

				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			}

		}
	}

}
