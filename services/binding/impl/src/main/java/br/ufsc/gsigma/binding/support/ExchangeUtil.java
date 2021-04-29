package br.ufsc.gsigma.binding.support;

import java.util.Objects;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import br.ufsc.gsigma.binding.impl.ExchangeHeaders;

public abstract class ExchangeUtil {

	public static void error(Exchange exchange, Throwable e) {
		error(exchange, e.getMessage());
	}

	public static boolean isError(Exchange exchange) {
		return Objects.equals(exchange.getOut().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class), 500);
	}

	public static void error(Exchange exchange, String errorMessage) {
		Message answer = exchange.getOut();
		answer.setHeader(Exchange.CONTENT_TYPE, "text/plain;charset=UTF-8");
		answer.setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
		answer.setBody(errorMessage);
		exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
	}

	public static void removeUnusedHeaders(Exchange exchange) {
		exchange.getIn().removeHeader(ExchangeHeaders.HEADER_EXECUTION_CONTEXT);
		exchange.getIn().removeHeader(ExchangeHeaders.HEADER_REQUEST_BINDING_CONTEXT);
		// exchange.getIn().removeHeader(ExchangeHeaders.HEADER_SERVICE_NAMESPACE);
		exchange.getIn().removeHeader(org.apache.cxf.headers.Header.HEADER_LIST);
		exchange.getIn().removeHeader("breadcrumbId");
	}

}
