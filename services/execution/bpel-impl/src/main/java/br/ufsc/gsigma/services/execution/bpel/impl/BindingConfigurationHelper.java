package br.ufsc.gsigma.services.execution.bpel.impl;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.ode.bpel.iapi.ProcessConf;

import br.ufsc.gsigma.binding.interfaces.BindingService;
import br.ufsc.gsigma.binding.locator.BindingServiceLocator;
import br.ufsc.gsigma.binding.model.BindingConfiguration;
import br.ufsc.gsigma.binding.model.BindingConfigurationRequest;
import br.ufsc.gsigma.binding.util.ServiceEndpointUtil;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.ServicesInformation;
import br.ufsc.gsigma.infrastructure.util.log.ExecutionLogMessage;
import br.ufsc.gsigma.infrastructure.util.log.LogConstants;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.model.ServiceEndpointInfo;

public abstract class BindingConfigurationHelper {

	private static final Logger logger = Logger.getLogger(BindingConfigurationHelper.class);

	static BindingConfiguration configure(String applicationId, ProcessConf processConf, Process businessProcess, ServicesInformation servicesInformation, ExecutionContext executionContext) {

		BindingServiceProcessCorrelator bindingCorrelator = BindingServiceProcessCorrelator.getInstance();

		BindingConfiguration bindingConfiguration = bindingCorrelator.getBindingConfigurationNoWait(processConf);

		if (bindingConfiguration == null) {

			// Map<String, Collection<ServiceEndpointInfo>> boundServices = checkAndDiscoverServices(businessProcess, servicesInformation);

			Map<String, Collection<ServiceEndpointInfo>> boundServices = ServiceEndpointUtil.toMapServiceEndpointInfo(servicesInformation);

			// Call BindingService to register binding configuration
			BindingConfigurationRequest bindingConfigurationRequest = new BindingConfigurationRequest(applicationId, executionContext);

			for (Collection<ServiceEndpointInfo> services : boundServices.values()) {
				bindingConfigurationRequest.addServiceEndpoints(services);
			}

			BindingService bindingService = BindingServiceLocator.get();

			if (bindingService == null) {
				throw new IllegalStateException("Binding Service not available");
			}

			bindingConfiguration = bindingService.registerBindingConfiguration(bindingConfigurationRequest);

			bindingCorrelator.correlate(processConf, bindingConfiguration);

			if (logger.isInfoEnabled()) {
				String serviceMediationEndpoint = bindingConfiguration.getServiceMediationEndpoint();
				String bindingToken = bindingConfiguration.getBindingToken();

				logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_SERVICE_BINDING, "Process " + processConf.getProcessId() + " was configured with binding endpoint " + serviceMediationEndpoint + " and token " + bindingToken) //
						.addAttribute(LogConstants.PROCESS_NAME, businessProcess.getName()) //
				);
			}
		} else {

			if (logger.isInfoEnabled()) {
				String serviceMediationEndpoint = bindingConfiguration.getServiceMediationEndpoint();
				String bindingToken = bindingConfiguration.getBindingToken();

				logger.info(new ExecutionLogMessage(LogConstants.MESSAGE_ID_PROCESS_SERVICE_BINDING, "Process " + processConf.getProcessId() + " already configured with binding endpoint " + serviceMediationEndpoint + " and token " + bindingToken) //
						.addAttribute(LogConstants.PROCESS_NAME, businessProcess.getName()) //
				);
			}

		}

		return bindingConfiguration;
	}

	static BindingConfiguration remove(ProcessConf processConf) {

		BindingConfiguration bindingConfiguration = BindingServiceProcessCorrelator.getInstance().getBindingConfigurationNoWait(processConf);

		if (bindingConfiguration != null) {
			BindingService bindingService = BindingServiceLocator.get();
			bindingService.unregisterBindingConfiguration(bindingConfiguration.getBindingToken());
		}

		return bindingConfiguration;
	}
}
