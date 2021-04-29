package br.ufsc.gsigma.infrastructure.util.messaging;

public class BootstrapCompleteEvent extends SystemComponentEvent {

	private static final long serialVersionUID = 1L;

	public enum ComponentName {
		BINDING_SERVICE, RESILIENCE_SERVICE, EXECUTION_SERVICE, UBL_SERVICES
	}

	private ComponentName componentName;

	private String senderId;

	public BootstrapCompleteEvent(ComponentName componentName, String senderId) {
		this.componentName = componentName;
		this.senderId = senderId;
	}

	public ComponentName getComponentName() {
		return componentName;
	}

	public void setComponentName(ComponentName componentName) {
		this.componentName = componentName;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

}
