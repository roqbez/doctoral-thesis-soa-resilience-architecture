package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.util.ArrayList;

public class BMAnnotation extends BMAbstractComponent {
	
	private static final long serialVersionUID = 1L;
	
	public String text;
	public ArrayList<String> annotatedNodes = new ArrayList<String>();

	public BMAnnotation() {
	}

	@Override
	protected void buidDefaultContactPoints() {
		// TODO Auto-generated method stub

	}

}