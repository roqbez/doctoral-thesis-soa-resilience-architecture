package br.ufsc.gsigma.infrastructure.ws.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;

public class CxfLoggingFeature extends LoggingFeature {

	private static final int DEFAULT_LIMIT = 1024 * 1024 * 64;

	private boolean logIn = true;

	private boolean logOut;

	private boolean prettyLogging;

	private int limit = DEFAULT_LIMIT;

	public CxfLoggingFeature() {
	}

	public CxfLoggingFeature(boolean logIn, boolean logOut, boolean prettyLogging, int limit) {
		this.logIn = logIn;
		this.logOut = logOut;
		this.prettyLogging = prettyLogging;
		this.limit = limit;
	}

	public CxfLoggingFeature(boolean logIn, boolean logOut, boolean prettyLogging) {
		this.logIn = logIn;
		this.logOut = logOut;
		this.prettyLogging = prettyLogging;
	}

	@Override
	protected void initializeProvider(InterceptorProvider provider, Bus bus) {

		if (logIn) {
			configureIn(provider, limit, prettyLogging);
		}

		if (logOut) {
			configureOut(provider, limit, prettyLogging);
		}
	}

	public static void configureIn(InterceptorProvider provider) {
		configureIn(provider, DEFAULT_LIMIT, false);
	}

	public static void configureIn(InterceptorProvider provider, int limit, boolean prettyLogging) {
		LoggingInInterceptor in = new LoggingInInterceptor(limit);
		in.setPrettyLogging(prettyLogging);
		provider.getInInterceptors().add(in);
		provider.getInFaultInterceptors().add(in);
	}

	public static void configureOut(InterceptorProvider provider) {
		configureOut(provider, DEFAULT_LIMIT, false);
	}

	public static void configureOut(InterceptorProvider provider, int limit, boolean prettyLogging) {
		LoggingOutInterceptor out = new LoggingOutInterceptor(limit);
		out.setPrettyLogging(prettyLogging);
		provider.getOutInterceptors().add(out);
		provider.getOutFaultInterceptors().add(out);
	}

}
