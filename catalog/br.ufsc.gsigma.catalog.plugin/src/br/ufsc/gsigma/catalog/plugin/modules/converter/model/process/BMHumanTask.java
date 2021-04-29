package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.io.PrintStream;

public class BMHumanTask extends BMTask {
	
	private static final long serialVersionUID = 1L;

	public BMHumanTask() {
	}

	public String getType() {
		return "humanTask";
	}

	public void printInputRepository(PrintStream file) {
		file.println("            <wbim:resources>");
		file
				.println("              <wbim:individualResourceRequirement individualResource=\"Person\" name=\"Primary owner\" timeRequired=\"P0Y0M0DT0H0M0S\"/>");
		file.println("            </wbim:resources>");
	}
}