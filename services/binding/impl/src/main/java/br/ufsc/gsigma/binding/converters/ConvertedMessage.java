package br.ufsc.gsigma.binding.converters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConvertedMessage {

	private byte[] payload;

	private Map<String, Object> headers = new HashMap<String, Object>();

	private Set<String> removedHeaders = new HashSet<String>();

	public ConvertedMessage(byte[] payload) {
		this.payload = payload;
	}

	public ConvertedMessage(byte[] payload, Map<String, Object> headers) {
		this.payload = payload;
		this.headers.putAll(headers);
	}

	public byte[] getPayload() {
		return payload;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public Set<String> getRemovedHeaders() {
		return removedHeaders;
	}

}
