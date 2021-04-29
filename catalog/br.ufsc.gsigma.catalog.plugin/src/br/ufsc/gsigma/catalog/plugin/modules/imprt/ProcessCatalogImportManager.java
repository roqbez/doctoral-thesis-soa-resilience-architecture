package br.ufsc.gsigma.catalog.plugin.modules.imprt;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import br.ufsc.gsigma.catalog.plugin.modules.converter.ProcessCatalogConverter;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.ProjectDefinition;
import br.ufsc.gsigma.catalog.services.interfaces.CatalogService;

import com.ibm.btools.blm.ie.imprt.IImportQuery;
import com.ibm.btools.blm.ie.imprt.ImportResult;
import com.ibm.btools.blm.ie.imprt.engine.ImportEngine;
import com.ibm.btools.blm.ui.navigation.model.NavigationProjectNode;

public class ProcessCatalogImportManager {

	public enum IMPORT_MODE {
		FROM_PROCESS_SPECIFICATION, FROM_USER_STORED_PROCESS
	};

	private CatalogService catalogService;

	private IMPORT_MODE importMode;

	private br.ufsc.gsigma.catalog.services.model.Process importingFromProcessSpecificationSelectedProcess;
	private Integer importingFromUserStoredProcessSelectedProcessId;

	public void importCatalog(IProgressMonitor progressMonitor, NavigationProjectNode p, String projectName) throws Exception {

		Calendar now = Calendar.getInstance();

		String tmpPath = System.getProperty("java.io.tmpdir") + File.separator + now.getTimeInMillis();

		String fileName = tmpPath + File.separator + "tmp" + ".xml";

		ProcessCatalogConverter converter = new ProcessCatalogConverter();

		br.ufsc.gsigma.catalog.services.model.Process process = null;

		if (importMode == IMPORT_MODE.FROM_USER_STORED_PROCESS)
			process = catalogService.getProcessById(importingFromUserStoredProcessSelectedProcessId);
		else if (importMode == IMPORT_MODE.FROM_PROCESS_SPECIFICATION)
			process = importingFromProcessSpecificationSelectedProcess;

		ProjectDefinition projectDefinition = converter.convertToEditorFormat(process);

		File xmlFile = converter.outputEditorXML(projectDefinition, fileName);

		List<File> files = new ArrayList<File>();
		files.add(xmlFile);

		runXMLImport(projectName, files);

		xmlFile.delete();

	}

	private ImportResult runXMLImport(String projectName, List<File> files) {
		IImportQuery query = new IImportQuery() {
			public int queryImportOption(Object arg0) {
				return 2;
			}
		};
		Map<String, Object> options = new HashMap<String, Object>();
		boolean removeProject = false;

		ImportEngine engine = new ImportEngine("com.ibm.btools.te.xml.XmlImportOperation", projectName, query, files, options,
				removeProject);
		try {
			engine.run(new NullProgressMonitor());
			return engine.getImportResult();
		} catch (Exception e) {
		}

		return null;
	}

	public IMPORT_MODE getImportMode() {
		return importMode;
	}

	public void setImportMode(IMPORT_MODE importMode) {
		this.importMode = importMode;
	}

	public CatalogService getCatalogService() {
		return catalogService;
	}

	public void setCatalogService(CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	public br.ufsc.gsigma.catalog.services.model.Process getImportingFromProcessSpecificationSelectedProcess() {
		return importingFromProcessSpecificationSelectedProcess;
	}

	public void setImportingFromProcessSpecificationSelectedProcess(
			br.ufsc.gsigma.catalog.services.model.Process importingFromProcessSpecificationSelectedProcess) {
		this.importingFromProcessSpecificationSelectedProcess = importingFromProcessSpecificationSelectedProcess;
	}

	public Integer getImportingFromUserStoredProcessSelectedProcessId() {
		return importingFromUserStoredProcessSelectedProcessId;
	}

	public void setImportingFromUserStoredProcessSelectedProcessId(Integer importingFromUserStoredProcessSelectedProcessId) {
		this.importingFromUserStoredProcessSelectedProcessId = importingFromUserStoredProcessSelectedProcessId;
	}

}