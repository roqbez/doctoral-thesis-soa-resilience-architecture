package br.ufsc.gsigma.services.resilience.impl;

import java.util.List;

import br.ufsc.gsigma.binding.model.BindingConfigurationRequest;
import br.ufsc.gsigma.services.resilience.model.ResilienceInfo;
import br.ufsc.gsigma.services.resilience.support.SOAApplication;

public class SubmitBindingConfigurationRequestArgs {
	public BindingConfigurationRequest bindingConfigurationRequest;
	public long currentReconfigurationTimestamp;
	public boolean checkTimestamp;
	public long startTime;
	public boolean temporary;
	public SOAApplication application;
	public List<String> applicationInstanceIds;
	public ResilienceInfo resilienceInfo;

	public SubmitBindingConfigurationRequestArgs(BindingConfigurationRequest bindingConfigurationRequest, long currentReconfigurationTimestamp, boolean checkTimestamp, long startTime, boolean temporary, SOAApplication application, List<String> applicationInstanceIds, ResilienceInfo resilienceInfo) {
		this.bindingConfigurationRequest = bindingConfigurationRequest;
		this.currentReconfigurationTimestamp = currentReconfigurationTimestamp;
		this.checkTimestamp = checkTimestamp;
		this.startTime = startTime;
		this.temporary = temporary;
		this.application = application;
		this.applicationInstanceIds = applicationInstanceIds;
		this.resilienceInfo = resilienceInfo;
	}
}