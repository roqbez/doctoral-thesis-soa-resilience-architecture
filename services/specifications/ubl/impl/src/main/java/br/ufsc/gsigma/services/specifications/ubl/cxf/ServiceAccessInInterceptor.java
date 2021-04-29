package br.ufsc.gsigma.services.specifications.ubl.cxf;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.log4j.Logger;

import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessToken;
import br.ufsc.services.core.util.json.JsonUtil;

public class ServiceAccessInInterceptor extends AbstractPhaseInterceptor<Message> {

	private static final Logger logger = Logger.getLogger(ServiceAccessInInterceptor.class);

	public static final ServiceAccessInInterceptor INSTANCE = new ServiceAccessInInterceptor();

	private ServiceAccessInInterceptor() {
		super(Phase.POST_STREAM);
		addBefore(StaxInInterceptor.class.getName());
	}

	@Override
	public void handleMessage(Message message) throws Fault {

		HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);

		String authorization = request.getHeader("Authorization");

		if (!StringUtils.isEmpty(authorization)) {

			ServiceAccessToken serviceAccessToken = (ServiceAccessToken) JsonUtil.decode(new String(Base64.decodeBase64(authorization), StandardCharsets.UTF_8), ServiceAccessToken.class);

			message.put(ServiceAccessToken.class.getName(), serviceAccessToken);

			logger.debug("Extracted access token from request --> " + serviceAccessToken);

			// message.getInterceptorChain().add(ServiceAccessEndingInterceptor.INSTANCE);
		}
	}

	@SuppressWarnings("unused")
	private static class ServiceAccessEndingInterceptor extends AbstractPhaseInterceptor<Message> {

		private static final ServiceAccessEndingInterceptor INSTANCE = new ServiceAccessEndingInterceptor();

		public ServiceAccessEndingInterceptor() {
			super(Phase.POST_INVOKE);
		}

		@Override
		public void handleMessage(Message message) throws Fault {
			ServiceAccessToken.remove();
		}
	}

}
