package br.ufsc.gsigma.architecture.experiments.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.ObjectUtils;

import br.ufsc.gsigma.architecture.experiments.ComponentUnavailabilityConfig;
import br.ufsc.gsigma.architecture.experiments.ExecutionExperimentParams;

public abstract class EventsTimelineUtil {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) {

		ExecutionExperimentParams expParams = new ExecutionExperimentParams();
		expParams.componentUnavailabilityConfigs.add(new ComponentUnavailabilityConfig("binding-service", 10000, 60000, 10));
		expParams.componentUnavailabilityConfigs.add(new ComponentUnavailabilityConfig("resilience-service", 20000, 60000, 10));
		expParams.componentUnavailabilityConfigs.add(new ComponentUnavailabilityConfig("execution-service", 30000, 60000, 10));

		List<String> processNames = Arrays.asList( //
				"BillingWithCreditNoteProcess", //
				"CreateCatalogueProcess", //
				"FulfilmentWithReceiptAdviceProcess", //
				"OrderingProcess", //
				"PaymentProcess");

		PeriodicEvent previous = null;

		Collection<PeriodicEvent> events = getEvents(expParams, processNames);

		Map<Integer, List<PeriodicEvent>> groupByTime = new TreeMap(events.stream().collect(Collectors.groupingBy(x -> x.startTime)));

		for (Entry<Integer, List<PeriodicEvent>> e : groupByTime.entrySet()) {
			if (e.getValue().size() > 1) {
				System.err.println(e.getKey());
				for (PeriodicEvent evt : e.getValue()) {
					System.err.println("\t" + evt.name);
				}

			}
		}

		for (PeriodicEvent e : events) {

			System.out.print(e);

			if (previous != null && previous.startTime == e.startTime) {
				System.out.print(" ###########-REPEATED-####################");
			}

			previous = e;

			System.out.println();

		}

	}

	public static Collection<PeriodicEvent> getEvents(ExecutionExperimentParams expParams, List<String> processNames) {

		List<PeriodicEvent> events = new LinkedList<PeriodicEvent>();

		int m = 1;

		for (String processName : processNames) {

			int offset = expParams.instanceCreationInitialDelay * m++;

			for (int i = 1; i <= expParams.repeats; i++) {

				int t = offset + ((i - 1) * expParams.instanceCreationInterval);

				events.add(new PeriodicEvent(t, processName + " (" + i + ")"));

				if (i == expParams.serviceUnavailabilityStartOffset) {
					for (int i2 = 1; i2 <= expParams.repeats; i2++) {
						events.add(new PeriodicEvent(t + expParams.serviceUnavailabilityInitialDelay + ((i2 - 1) * expParams.serviceUnavailabilityInterval), "Service unavailability (" + i2 + ") for " + processName));
					}

				} else if (i == expParams.serviceContainerUnavailabilityStartOffset) {
					for (int i2 = 1; i2 <= expParams.repeats; i2++) {
						events.add(new PeriodicEvent(t + expParams.serviceContainerUnavailabilityInitialDelay + ((i2 - 1) * expParams.serviceContainerUnavailabilityInterval), "Service container unavailability (" + i2 + ") for " + processName));
					}
				}
			}

		}

		for (ComponentUnavailabilityConfig c : expParams.componentUnavailabilityConfigs) {
			for (int i = 1; i <= c.repeats; i++) {
				events.add(new PeriodicEvent(c.initialDelay + ((i - 1) * c.period), "Component unavailability (" + i + ") for " + c.componentName));
			}
		}

		Collections.sort(events);

		return events;

	}

	private static class PeriodicEvent implements Comparable<PeriodicEvent> {

		int startTime;

		String name;

		public PeriodicEvent(int startTime, String name) {
			this.startTime = startTime;
			this.name = name;
		}

		@Override
		public String toString() {
			return (startTime / 1000) + " - " + name;
		}

		@Override
		public int compareTo(PeriodicEvent o) {
			return ObjectUtils.compare(startTime, o.startTime);
		}
	}

}
