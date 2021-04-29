package br.ufsc.gsigma.architecture.experiments.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class UnavailabilityCounter {

	private Map<String, Integer> processServicesUnavailability = new ConcurrentHashMap<String, Integer>();

	private Map<String, Integer> adhocProcessServicesUnavailability = new ConcurrentHashMap<String, Integer>();

	private Map<String, Integer> archComponentUnavailability = new ConcurrentHashMap<String, Integer>();

	private boolean stopped;

	public boolean hasAnyCount() {
		return !processServicesUnavailability.isEmpty() || !adhocProcessServicesUnavailability.isEmpty() || !archComponentUnavailability.isEmpty();
	}

	@Override
	public String toString() {

		StringWriter sw = new StringWriter();

		PrintWriter pw = new PrintWriter(sw);

		if (!processServicesUnavailability.isEmpty()) {
			pw.println("processServicesUnavailability");

			for (Entry<String, Integer> e : new TreeMap<String, Integer>(processServicesUnavailability).entrySet()) {
				pw.println("\t" + e.getKey() + "=" + e.getValue());
			}

			pw.println();
		}

		if (!adhocProcessServicesUnavailability.isEmpty()) {
			pw.println("adhocProcessServicesUnavailability");

			for (Entry<String, Integer> e : new TreeMap<String, Integer>(adhocProcessServicesUnavailability).entrySet()) {
				pw.println("\t" + e.getKey() + "=" + e.getValue());
			}

			pw.println();
		}

		if (!archComponentUnavailability.isEmpty()) {
			pw.println("archComponentUnavailability");

			for (Entry<String, Integer> e : new TreeMap<String, Integer>(archComponentUnavailability).entrySet()) {
				pw.println("\t" + e.getKey() + "=" + e.getValue());
			}

			pw.println();
		}

		return sw.toString();
	}

	public void stopCounter() {
		stopped = true;
	}

	public void incrementProcessServicesUnavailability(String key, int increment) {
		if (!stopped)
			processServicesUnavailability.compute(key, (k, v) -> v != null ? v + increment : increment);
	}

	public void incrementAdhocProcessServicesUnavailability(String key, int increment) {
		if (!stopped)
			adhocProcessServicesUnavailability.compute(key, (k, v) -> v != null ? v + increment : increment);
	}

	public void incrementArchComponentUnavailability(String key, int increment) {
		if (!stopped)
			archComponentUnavailability.compute(key, (k, v) -> v != null ? v + increment : increment);
	}

}
