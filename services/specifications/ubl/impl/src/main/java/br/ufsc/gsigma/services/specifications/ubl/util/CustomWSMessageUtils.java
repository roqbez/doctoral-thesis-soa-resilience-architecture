package br.ufsc.gsigma.services.specifications.ubl.util;

import java.util.List;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;

import br.ufsc.gsigma.infrastructure.ws.WSMessageUtils;
import br.ufsc.gsigma.infrastructure.ws.cxf.CxfLoggingFeature;
import br.ufsc.gsigma.infrastructure.ws.cxf.ExecutionContextFeature;
import br.ufsc.gsigma.services.specifications.ubl.cxf.RequestBindingContextFeature;

public class CustomWSMessageUtils extends WSMessageUtils {

	private static final CustomWSMessageUtils INSTANCE = new CustomWSMessageUtils();

	public static CustomWSMessageUtils getInstance() {
		return INSTANCE;
	}

	@Override
	protected void configureClientFeatures(Client client, List<Interceptor<? extends Message>> outInterceptors) {

		CxfLoggingFeature.configureIn(client);
		CxfLoggingFeature.configureOut(client);

		ExecutionContextFeature.configureIn(client);
		RequestBindingContextFeature.configureIn(client);

		if (outInterceptors != null) {
			client.getOutInterceptors().addAll(outInterceptors);
		}
	}
}
