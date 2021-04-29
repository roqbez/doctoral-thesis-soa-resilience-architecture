package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

public class BMEnd extends BMAbstractComponent {
	
	private static final long serialVersionUID = 1L;

	public BMEnd() {
	}

	public BMEnd(String name) {
		this.name = name;
	}

	protected void buidDefaultContactPoints() {
		inputContactPoint.add(new BMInputContactPoint(this, "Input"));
	}

}