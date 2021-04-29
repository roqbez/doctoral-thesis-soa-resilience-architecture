package br.ufsc.gsigma.services.bpelexport.model;

import java.util.ArrayList;
import java.util.List;

public class BPELVariable {

	private String name;

	private String messageType;

	private String messagePayload;

	private List<String> payloadAttributes = new ArrayList<String>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getMessagePayload() {
		return messagePayload;
	}

	public void setMessagePayload(String messagePayload) {
		this.messagePayload = messagePayload;
	}

	public List<String> getPayloadAttributes() {
		return payloadAttributes;
	}

}
