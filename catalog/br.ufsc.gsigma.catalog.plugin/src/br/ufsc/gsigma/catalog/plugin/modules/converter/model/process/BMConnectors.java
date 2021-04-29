package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

public class BMConnectors extends BMAbstractComponent {
	
	private static final long serialVersionUID = 1L;

	public int startId;
	public int startPoint;
	public int endId;
	public int endPoint;
	public String sourceContactPoint;
	public String endContactPoint;

	@Override
	protected void buidDefaultContactPoints() {
		// TODO Auto-generated method stub

	}

}