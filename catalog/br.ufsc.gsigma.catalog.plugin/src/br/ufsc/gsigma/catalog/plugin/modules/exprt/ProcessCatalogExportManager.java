package br.ufsc.gsigma.catalog.plugin.modules.exprt;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.xml.sax.SAXException;

import br.ufsc.gsigma.catalog.plugin.modules.converter.ProcessConverterHelper;
import br.ufsc.gsigma.catalog.services.interfaces.CatalogService;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.ProcessTreeDescription;

import com.ibm.btools.blm.ui.navigation.model.NavigationProcessNode;

public class ProcessCatalogExportManager {

	private CatalogService catalogService;

	private NavigationProcessNode processNode;

	private ProcessTreeDescription processTreeDescription;

	private String newProcessName;

	public void exportCatalog(IProgressMonitor progressMonitor) throws ParserConfigurationException, SAXException, IOException {
		Process process = ProcessConverterHelper.getProcess(processNode, false, true, null);
		process.setName(newProcessName);
		catalogService.addOrUpdateProcess(processTreeDescription.getId(), process);
	}

	public NavigationProcessNode getProcessNode() {
		return processNode;
	}

	public void setProcessNode(NavigationProcessNode processNode) {
		this.processNode = processNode;
	}

	public ProcessTreeDescription getProcessTreeDescription() {
		return processTreeDescription;
	}

	public void setProcessTreeDescription(ProcessTreeDescription processTreeDescription) {
		this.processTreeDescription = processTreeDescription;
	}

	public String getNewProcessName() {
		return newProcessName;
	}

	public void setNewProcessName(String newProcessName) {
		this.newProcessName = newProcessName;
	}

}