package br.ufsc.gsigma.services.specifications.ubl.impl;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;

import br.ufsc.gsigma.binding.context.RequestBindingContext;
import br.ufsc.gsigma.binding.model.InvokedServiceInfo;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessContract;
import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessRequest;
import br.ufsc.gsigma.infrastructure.ws.access.ServiceAccessToken;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionAttributes;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.specifications.ubl.util.Util;
import br.ufsc.services.core.util.json.JsonUtil;

public abstract class AbstractUBLService implements ServiceAccessContract {

	private final Logger logger = Logger.getLogger(getClass());

	public Boolean alive() {
		logger.debug(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_CHECK, "Invoking operation 'alive' on '" + Util.getServiceNamespace(this) + "'"));
		return true;
	}

	@Override
	public ServiceAccessToken requestAccess(ServiceAccessRequest request) {

		String tenantId = DigestUtils.sha1Hex(request.getClientId());

		ServiceAccessToken response = new ServiceAccessToken();
		response.setTenantId(tenantId);
		response.setRequest(request);

		String token = Base64.encodeBase64URLSafeString(JsonUtil.encode(response).getBytes(StandardCharsets.UTF_8));

		response.setToken(token);

		logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_SERVICE_ACCESS_REQUEST, "Granted access to clientId=" + request.getClientId() + ", applicationId=" + request.getApplicationId() + " --> " + token));

		return response;
	}

	@SuppressWarnings("unchecked")
	protected String getDecision(String... decisions) {

		String decision = decisions[new Random().nextInt(decisions.length)];

		Map<String, List<String>> mapEctxDecisions = ExecutionContext.getAttribute(ExecutionAttributes.ATT_SERVICES_DECISIONS, Map.class, new HashMap<String, List<String>>());

		if (mapEctxDecisions != null) {
			List<String> ectxDecisions = mapEctxDecisions.get(getClass().getSimpleName());
			if (ectxDecisions != null) {
				InvokedServiceInfo invokedServiceInfo = RequestBindingContext.get().getInvokedServiceInfo();
				decision = ectxDecisions.get(invokedServiceInfo.getRepeatCount() % ectxDecisions.size());

				if (decision.indexOf('!') == 0) {
					decisions = (String[]) ArrayUtils.removeElement(decisions, decision.substring(1));
					decision = decisions[new Random().nextInt(decisions.length)];
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("decision --> " + decision);
		}

		return decision;
	}

}
