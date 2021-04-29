package br.ufsc.gsigma.infrastructure.ws.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

public class ThreadLocalHolderFeature extends AbstractFeature {

	protected void initializeProvider(InterceptorProvider provider, Bus bus) {
		configure(provider);
	}

	public static void configure(InterceptorProvider provider) {
		configureIn(provider);
		configureOut(provider);
	}

	public static void configureOut(InterceptorProvider provider) {
		ThreadLocalHolderOutInterceptor out = new ThreadLocalHolderOutInterceptor();
		provider.getOutInterceptors().add(out);
		provider.getOutFaultInterceptors().add(out);
	}

	public static void configureIn(InterceptorProvider provider) {
		ThreadLocalHolderInInterceptor in = new ThreadLocalHolderInInterceptor();
		provider.getInInterceptors().add(in);
		provider.getInFaultInterceptors().add(in);
	}

}
