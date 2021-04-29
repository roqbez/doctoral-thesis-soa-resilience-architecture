package br.ufsc.gsigma.infrastructure.ws.access;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

public interface ServiceAccessContract {

	@ResponseWrapper(localName = "requestAccessResponse", targetNamespace = "http://gsigma.ufsc.br")
	@WebResult(name = "serviceAccessToken", targetNamespace = "http://gsigma.ufsc.br")
	@RequestWrapper(localName = "requestAccess", targetNamespace = "http://gsigma.ufsc.br")
	@WebMethod(action = "http://gsigma.ufsc.br/requestAccess")
	public ServiceAccessToken requestAccess(@WebParam(name = "request") ServiceAccessRequest request);

}
