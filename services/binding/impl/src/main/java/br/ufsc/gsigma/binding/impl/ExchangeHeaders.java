package br.ufsc.gsigma.binding.impl;

import br.ufsc.gsigma.infrastructure.ws.Headers;

public interface ExchangeHeaders {

	public static final String HEADER_SERVICE_NAMESPACE = Headers.HEADER_SERVICE_NAMESPACE;

	public static final String HEADER_REQUEST_TIMESTAMP = Headers.HEADER_REQUEST_TIMESTAMP.getLocalPart();

	public static final String HEADER_REQUEST_BINDING_CONTEXT = Headers.HEADER_REQUEST_BINDING_CONTEXT;

	public static final String HEADER_EXECUTION_CONTEXT = Headers.HEADER_EXECUTION_CONTEXT;

}
