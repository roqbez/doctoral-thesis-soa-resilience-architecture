package br.ufsc.gsigma.architecture.experiments;

public class ComponentUnavailabilityConfig {

	public String componentName;

	public int initialDelay;

	public int period;

	public int repeats;

	public ComponentUnavailabilityConfig(String componentName, int initialDelay, int period, int repeats) {
		this.componentName = componentName;
		this.initialDelay = initialDelay;
		this.period = period;
		this.repeats = repeats;
	}

	public ComponentUnavailabilityConfig(String componentName, int initialDelay, int period) {
		this.componentName = componentName;
		this.initialDelay = initialDelay;
		this.period = period;
	}

}
