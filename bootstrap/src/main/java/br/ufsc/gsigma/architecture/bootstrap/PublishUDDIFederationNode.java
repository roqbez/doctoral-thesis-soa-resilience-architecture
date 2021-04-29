package br.ufsc.gsigma.architecture.bootstrap;

import org.apache.juddi.v3.client.transport.Transport;
import org.uddi.api_v3.TModelInstanceInfo;

import br.ufsc.gsigma.infrastructure.ws.uddi.UddiKeys;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiLocator;
import br.ufsc.gsigma.infrastructure.ws.uddi.UddiRegister;

public class PublishUDDIFederationNode {

	public static void main(String[] args) throws Exception {

		String host = "localhost";

		Transport transport = UddiLocator.getUDDITransportByServicesBaseURL("http://servicefederation.d-201603244.ufsc.br:8079/services");

		for (int i = 1; i <= 5; i++) {
			final String baseURL = "http://" + host + ":" + (8079 - i);
			publishUDDIFederationNode(transport, baseURL, i);
		}
	}

	public static void publishUDDIFederationNode(Transport uddiFederationTransport, final String baseURL, final int i) throws Exception {
		UddiRegister.publishService(uddiFederationTransport, "root", "", //
				"uddi:uddifederation:repository", "uddi:uddifederation:uddiprovider" + i, //
				"UDDI Inquiry Service - Provider " + i, //
				"Web Service supporting UDDI Inquiry API", //
				baseURL + "/services/inquiry?wsdl", "wsdlDeployment", //
				new TModelInstanceInfo[] { UddiRegister.getTModelInstanceInfo("uddi:uddi.org:v3_inquiry"), //
						UddiRegister.getTModelInstanceInfo(UddiKeys.UDDI_UDDISERVICEPROVIDER),

				});
	}

}
