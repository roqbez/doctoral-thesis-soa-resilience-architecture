package br.ufsc.gsigma.infrastructure.ws.cxf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class ServiceAvailabilityInInterceptor extends AbstractPhaseInterceptor<Message> {

	private Map<String, Boolean> simulateError = new HashMap<String, Boolean>();

	public ServiceAvailabilityInInterceptor() {
		super(Phase.RECEIVE);
	}

	public boolean isServiceAvailable(String path) {
		if (path != null) {
			return !BooleanUtils.isTrue(this.simulateError.get(path));
		} else {
			for (Boolean b : this.simulateError.values()) {
				if (b) {
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public void handleMessage(Message message) throws Fault {

		boolean simulateError = BooleanUtils.isTrue(this.simulateError.get(message.getContextualProperty(Message.PATH_INFO)));

		if (simulateError)
			throw new RuntimeException("Service unavailable");

	}

	public void setServiceAvailable() {
		for (String key : new ArrayList<String>(simulateError.keySet())) {
			setServiceAvailable(key);
		}
	}

	public void setServiceUnvailable() {
		for (String key : new ArrayList<String>(simulateError.keySet())) {
			setServiceUnvailable(key);
		}
	}

	public void setServiceAvailable(String path) {
		if (path != null)
			simulateError.put(path, false);
		else
			setServiceAvailable();
	}

	public void setServiceUnvailable(String path) {
		if (path != null)
			simulateError.put(path, true);
		else
			setServiceUnvailable();
	}

	public void setServicesAvailability(Collection<String> availableServicesPaths, Collection<String> unavailableServicesPaths) {
		if (availableServicesPaths != null) {
			for (String s : availableServicesPaths)
				simulateError.put(s, false);
		}
		if (unavailableServicesPaths != null) {
			for (String s : unavailableServicesPaths)
				simulateError.put(s, true);
		}
	}

}