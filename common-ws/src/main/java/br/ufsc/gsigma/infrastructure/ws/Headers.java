package br.ufsc.gsigma.infrastructure.ws;

import javax.xml.namespace.QName;

public interface Headers {

	public static final String NAMESPACE = "http://gsigma.ufsc.br";

	public static final QName HEADER_REQUEST_TIMESTAMP = new QName(NAMESPACE, "REQUEST_TIMESTAMP");

	public static final String HEADER_REQUEST_BINDING_CONTEXT = "REQUEST_BINDING_CONTEXT";

	public static final String HEADER_EXECUTION_CONTEXT = "EXECUTION_CONTEXT";

	public static final String HEADER_SERVICE_NAMESPACE = "SERVICE_NAMESPACE";

	public static final String HEADER_SERVICE_PROTOCOL = "SERVICE_PROTOCOL";

	public static final String HEADER_SERVICE_REPLY = "SERVICE_REPLY";

}
