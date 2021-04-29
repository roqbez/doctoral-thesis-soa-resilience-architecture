package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

public class BMConnection {

	private BMOutputContactPoint source;
	private BMInputContactPoint destination;

	public BMConnection() {

	}

	public BMConnection(BMOutputContactPoint source, BMInputContactPoint destination) {
		this.source = source;
		this.destination = destination;
	}

	public BMOutputContactPoint getSource() {
		return source;
	}

	public void setSource(BMOutputContactPoint source) {
		this.source = source;
	}

	public BMInputContactPoint getDestination() {
		return destination;
	}

	public void setDestination(BMInputContactPoint destination) {
		this.destination = destination;
	}

}
