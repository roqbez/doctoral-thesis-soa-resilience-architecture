package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

public class BMStart extends BMAbstractComponent {
	
	private static final long serialVersionUID = 1L;

	public BMStart() {
	}

	public BMStart(String name) {
		this.name = name;
	}

	protected void buidDefaultContactPoints() {
		outputContactPoint.add(new BMOutputContactPoint(this, "Output"));
	}

	public static boolean mapToStart(String elementType) {
		return (elementType.equals("flowChartConnector"));
	}
}