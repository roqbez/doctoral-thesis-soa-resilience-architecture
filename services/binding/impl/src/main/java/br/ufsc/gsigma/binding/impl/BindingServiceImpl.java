package br.ufsc.gsigma.binding.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.camel.Consume;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufsc.gsigma.binding.bootstrap.RunBindingService;
import br.ufsc.gsigma.binding.events.BindingConfigurationRequestEvent;
import br.ufsc.gsigma.binding.events.BindingConfiguredEvent;
import br.ufsc.gsigma.binding.interfaces.BindingService;
import br.ufsc.gsigma.binding.model.BindingConfiguration;
import br.ufsc.gsigma.binding.model.BindingConfigurationRequest;
import br.ufsc.gsigma.binding.model.NamespaceServices;
import br.ufsc.gsigma.binding.support.BindingEngineConfiguration;
import br.ufsc.gsigma.binding.support.InfinispanSupport;
import br.ufsc.gsigma.common.hash.HashUtil;
import br.ufsc.gsigma.infrastructure.util.docker.DockerContainerUtil;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.util.messaging.Event;
import br.ufsc.gsigma.infrastructure.util.messaging.EventSender;
import br.ufsc.gsigma.infrastructure.util.messaging.MessageEndpoints;
import br.ufsc.gsigma.infrastructure.ws.HostAddressUtil;
import br.ufsc.gsigma.infrastructure.ws.model.NodeInfo;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;
import br.ufsc.gsigma.services.resilience.model.ResilienceInfo;

@Service
public class BindingServiceImpl implements BindingService {

	private static final Logger logger = Logger.getLogger(BindingServiceImpl.class);

	@Autowired
	private EventSender eventSender;

	@Autowired
	private BindingEngine bindingEngine;

	@Autowired
	private InfinispanSupport infinispanSupport;

	@Override
	public String getPublicIp() {
		return HostAddressUtil.getPublicIp();
	}

	@Override
	public NodeInfo getNodeInfo() {
		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}
		return new NodeInfo(infinispanSupport.getMyAddress().toString(), DockerContainerUtil.getContainerId(), getPublicIp(), hostName, infinispanSupport.isLeader());
	}

	@Consume(uri = MessageEndpoints.EVENT_BUS_ENDPOINT)
	public void receiveBusEvent(Event event) {

		logger.debug("Received event: " + event);

		if (event instanceof br.ufsc.gsigma.binding.events.RequestEvent) {

			logger.info("Received a binding configuration request event, calling operation 'registerBindingConfiguration'");
			registerBindingConfiguration(((BindingConfigurationRequestEvent) event).getConfigurationRequest());
		}
	}

	@Override
	public List<BindingConfiguration> getBindingConfigurations() {
		Collection<BindingEngineConfiguration> bindingConfigurations = bindingEngine.getBindingConfigurations();
		return bindingConfigurations.stream().map(x -> toBindingConfiguration(x)).collect(Collectors.toList());
	}

	@Override
	public void clearBindingConfigurations() {
		bindingEngine.clearBindingConfigurations();
	}

	@Override
	public BindingConfiguration registerBindingConfiguration(BindingConfigurationRequest bindingConfigurationRequest) {

		if (bindingConfigurationRequest == null || StringUtils.isBlank(bindingConfigurationRequest.getApplicationId())) {
			throw new IllegalArgumentException("BindingConfigurationRequest is invalid");
		}

		try {

			final String applicationId = bindingConfigurationRequest.getApplicationId();

			final String token = generateBindingToken(bindingConfigurationRequest);

			ResilienceInfo resilienceInfo = bindingConfigurationRequest.getResilienceInfo();

			BindingEngineConfiguration configuration = new BindingEngineConfiguration(applicationId, token, resilienceInfo, bindingConfigurationRequest.getExecutionContext());

			for (ServiceEndpointInfo s : bindingConfigurationRequest.getServiceEndpoints()) {
				configuration.addServiceForNamespace(s.getServiceNamespace(), s);
			}

			// TODO: improve

			List<String> applicationInstanceIds = bindingConfigurationRequest.getApplicationInstanceIds();

			if (!CollectionUtils.isEmpty(applicationInstanceIds)) {
				for (String applicationInstanceId : applicationInstanceIds) {
					configuration.addInstanceBindingConfiguration(applicationInstanceId, configuration);
				}
			}

			bindingEngine.addConfiguration(configuration);

			if (logger.isInfoEnabled()) {
				logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_SERVICE_BINDING,
						"Binding configured for applicationId=" + applicationId + ", token=" + token + (!CollectionUtils.isEmpty(applicationInstanceIds) ? ", applicationInstanceIds=" + applicationInstanceIds : "") + ", version=" + configuration.getVersion())
						+ (resilienceInfo != null ? ", resilienceInfo=" + resilienceInfo : ""));
			}

			BindingConfiguration result = toBindingConfiguration(configuration);

			eventSender.sendEvent(new BindingConfiguredEvent(result), -1);

			return result;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public BindingConfiguration unregisterBindingConfiguration(String bindingToken) {

		BindingEngineConfiguration bindingEngineConfiguration = bindingEngine.removeConfiguration(bindingToken);

		if (bindingEngineConfiguration != null) {

			BindingConfiguration cfg = toBindingConfiguration(bindingEngineConfiguration);

			if (logger.isInfoEnabled()) {
				logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_SERVICE_BINDING, "Binding removed for applicationId=" + cfg.getApplicationId() + ", token=" + cfg.getBindingToken() + ", version=" + cfg.getVersion()));
			}
			return cfg;

		} else {
			return null;
		}
	}

	@Override
	public String getServiceMediationEndpoint() {

		String scheme = null;
		String serverName = null;
		Integer serverPort = null;

		Message message = PhaseInterceptorChain.getCurrentMessage();

		if (message != null) {

			HttpServletRequest httpRequest = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);

			scheme = httpRequest.getScheme();
			serverName = httpRequest.getServerName();
			serverPort = httpRequest.getServerPort();

		} else {
			scheme = "http";
			serverName = RunBindingService.getServiceHost();
			serverPort = Integer.valueOf(RunBindingService.getServicePort());
		}

		boolean isHttpDefaultPort = "http".equals(scheme) && serverPort == 80;
		boolean isHttpsDefaultPort = "https".equals(scheme) && serverPort == 443;

		return scheme + "://" + serverName + (!isHttpDefaultPort && !isHttpsDefaultPort ? ":" + serverPort : "") + "/" + BindingEngine.SERVICE_MEDIATION_PATH;
	}

	private String generateBindingToken(BindingConfigurationRequest bindingConfigurationRequest) {

		int hash = HashUtil.getHashFromValues( //
				BindingServiceImpl.class.getName(), //
				bindingConfigurationRequest.getApplicationId() //
		);

		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(hash);

		return DigestUtils.sha1Hex(buf.array());
	}

	private BindingConfiguration toBindingConfiguration(BindingEngineConfiguration b) {

		BindingConfiguration cfg = new BindingConfiguration();
		cfg.setApplicationId(b.getApplicationId());
		cfg.setVersion(b.getVersion());
		cfg.setBindingToken(b.getToken());
		cfg.setServiceMediationEndpoint(getServiceMediationEndpoint());

		for (Entry<String, Collection<ServiceEndpointInfo>> e : b.getServicesPerNamespace().entrySet()) {
			cfg.getBoundServices().add(new NamespaceServices(e.getKey(), e.getValue()));
		}
		return cfg;
	}

}
