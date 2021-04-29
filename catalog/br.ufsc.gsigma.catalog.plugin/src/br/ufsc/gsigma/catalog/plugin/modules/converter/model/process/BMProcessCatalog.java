package br.ufsc.gsigma.catalog.plugin.modules.converter.model.process;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.AbstractCatalog;

public class BMProcessCatalog extends AbstractCatalog {

	private List<BMProcess> processes = new ArrayList<BMProcess>();

	public BMProcessCatalog() {
	}

	public BMProcessCatalog(String id, String name) {
		super(id, name);
	}

	public BMProcess addProcess(String name) {
		BMProcess p = new BMProcess();
		p.name = getId() + "##" + name;
		processes.add(p);
		return p;
	}

	public List<BMProcess> getProcesses() {
		return processes;
	}

	public void setProcesses(List<BMProcess> processes) {
		this.processes = processes;
	}

	@Override
	public boolean isEmpty() {
		return processes.isEmpty();
	}

}
