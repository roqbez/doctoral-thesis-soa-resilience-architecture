package br.ufsc.gsigma.infrastructure.ws.cxf;

import java.util.Collection;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

public class ServiceAvailabilityFeature extends AbstractFeature {

	private ServiceAvailabilityInInterceptor interceptor = new ServiceAvailabilityInInterceptor();

	protected void initializeProvider(InterceptorProvider provider, Bus bus) {
		provider.getInInterceptors().add(interceptor);
	}

	public void setServiceAvailable() {
		interceptor.setServiceAvailable();
	}

	public void setServiceUnvailable() {
		interceptor.setServiceUnvailable();
	}

	public void setServiceAvailable(String path) {
		interceptor.setServiceAvailable(path);
	}

	public void setServiceUnvailable(String path) {
		interceptor.setServiceUnvailable(path);
	}

	public boolean isServiceAvailable(String path) {
		return interceptor.isServiceAvailable(path);
	}

	public void setServicesAvailability(Collection<String> availableServicesPaths, Collection<String> unavailableServicesPaths) {
		interceptor.setServicesAvailability(availableServicesPaths, unavailableServicesPaths);
	}

}
