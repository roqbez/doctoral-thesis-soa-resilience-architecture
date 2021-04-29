package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.io.PrintStream;

public class BMRepository extends BMAbstractComponent {
	
	private static final long serialVersionUID = 1L;
	
	int capacity;
	boolean ordered;
	boolean unique;
	String type;

	public BMRepository() {
		this.capacity = -1;
		this.ordered = false;
		this.unique = false;
		this.type = "String";
	}

	public void printInputRepository(PrintStream file, int id, String title) {
		file.println("              <wbim:input associatedData=\"String\" isOrdered=\"" + this.ordered + "\" isUnique=\"" + this.unique
				+ "\" maximum=\"" + 1 + "\" minimum=\"" + 1 + "\" name=\"" + "Input" + ":" + id + "\">");
		file.println("                <wbim:repositoryValue atBeginning=\"true\" isRemove=\"false\">");
		file.println("                  <wbim:localRepository name=\"" + this.name + "\" path=\"" + title + "/" + this.name + "\"/>");
		file.println("                </wbim:repositoryValue>");
		file.println("              </wbim:input>");
	}

	public void printOutputRepository(PrintStream file, int id, String title) {
		file.println("              <wbim:output associatedData=\"String\" isOrdered=\"" + this.ordered + "\" isUnique=\"" + this.unique
				+ "\" maximum=\"" + 1 + "\" minimum=\"" + 1 + "\" name=\"" + "Input" + ":" + id + "\">");
		file.println("                <wbim:repositoryValue atBeginning=\"true\" isRemove=\"false\">");
		file.println("                  <wbim:localRepository name=\"" + this.name + "\" path=\"" + title + "/" + this.name + "\"/>");
		file.println("                </wbim:repositoryValue>");
		file.println("              </wbim:output>");
	}

	@Override
	protected void buidDefaultContactPoints() {
		// TODO Auto-generated method stub

	}
}