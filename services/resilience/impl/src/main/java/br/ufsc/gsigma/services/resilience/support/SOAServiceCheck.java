package br.ufsc.gsigma.services.resilience.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class SOAServiceCheck {

	private static Logger logger = Logger.getLogger(SOAServiceCheck.class);

	private SOAService service;

	private Map<String, Integer> reconfigurationTimestamps = new HashMap<String, Integer>();

	public SOAServiceCheck(SOAService service) {
		this.service = service;

		Collection<SOAApplication> soaApplications = service.getSOAApplications();

		if (soaApplications != null) {
			for (SOAApplication app : soaApplications) {
				Integer tstmp = app.getReconfigurationTimestamp();
				if (tstmp != null)
					reconfigurationTimestamps.put(app.getApplicationId(), tstmp);
			}
		}
	}

	public SOAService getService() {
		return service;
	}

	public boolean isRelevantForApplication(SOAApplication application) {

		int tstmp = reconfigurationTimestamps.get(application.getApplicationId());

		int appTstmp = application.getReconfigurationTimestamp();

		if (appTstmp > tstmp) {
			logger.info("SOAService check for " + getService().getServiceKey() + " contains a timestamp smaller (" + tstmp + ") than application's timestamp (" + appTstmp + "), so ignoring it");
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "Service check for " + service + " with timestamps: " + reconfigurationTimestamps;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SOAServiceCheck other = (SOAServiceCheck) obj;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		return true;
	}

}
