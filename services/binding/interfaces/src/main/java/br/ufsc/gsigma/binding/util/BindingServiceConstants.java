package br.ufsc.gsigma.binding.util;

import javax.xml.namespace.QName;

public interface BindingServiceConstants {

	public static final String WS_NAMESPACE = "http://gsigma.ufsc.br/bindingService";

	public static final String WS_REQUEST_BINDING_CONTEXT_NAME = "requestBindingContext";

	public static final QName WS_REQUEST_BINDING_CONTEXT = new QName(WS_NAMESPACE, WS_REQUEST_BINDING_CONTEXT_NAME);

	public static final QName WS_SERVICE_NAME = new QName(WS_NAMESPACE, "BindingService");

	public static final QName WS_SERVICE_PORT_NAME = new QName(WS_NAMESPACE, WS_SERVICE_NAME.getLocalPart() + "Port");

}
