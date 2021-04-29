package br.ufsc.gsigma.servicediscovery.impl;

import org.uddi.v3_service.UDDIInquiryPortType;

import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;

public class TesteUDDILocator {

	public static void main(String[] args) {

		UDDIInquiryPortType inquiry = WebServiceLocator.locateService(WebServiceLocator.UDDI_FEDERATION_UDDI_SERVICE_KEY, UDDIInquiryPortType.class);

		System.out.println(inquiry);

	}

}
