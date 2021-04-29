package br.ufsc.gsigma.services.specifications.ubl.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

public class RequestBindingContextFeature extends AbstractFeature {

	private boolean interceptIn = true;

	private boolean interceptOut = true;

	public RequestBindingContextFeature() {
	}

	public RequestBindingContextFeature(boolean interceptIn, boolean interceptOut) {
		this.interceptIn = interceptIn;
		this.interceptOut = interceptOut;
	}

	protected void initializeProvider(InterceptorProvider provider, Bus bus) {
		configure(provider, interceptIn, interceptOut);
	}

	public static void configure(InterceptorProvider provider) {
		configure(provider, true, true);
	}

	public static void configure(InterceptorProvider provider, boolean interceptIn, boolean interceptOut) {
		if (interceptIn) {
			configureIn(provider);
		}

		if (interceptOut) {
			configureOut(provider);
		}
	}

	public static void configureIn(InterceptorProvider provider) {
		RequestBindingContextInInterceptor in = new RequestBindingContextInInterceptor();
		provider.getInInterceptors().add(in);
		provider.getInFaultInterceptors().add(in);
	}

	public static void configureOut(InterceptorProvider provider) {
		RequestBindingContextOutInterceptor out = new RequestBindingContextOutInterceptor();
		provider.getOutInterceptors().add(out);
		provider.getOutFaultInterceptors().add(out);
	}

}
