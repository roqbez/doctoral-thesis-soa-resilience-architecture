package br.ufsc.gsigma.infrastructure.util.messaging;

public class StringEvent extends Event {

	private static final long serialVersionUID = 1L;

	private String message;

	public StringEvent(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
