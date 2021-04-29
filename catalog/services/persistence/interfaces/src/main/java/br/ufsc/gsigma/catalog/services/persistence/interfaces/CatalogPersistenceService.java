package br.ufsc.gsigma.catalog.services.persistence.interfaces;

import java.util.List;

import javax.jws.WebService;

import br.ufsc.gsigma.catalog.services.model.Document;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.ProcessCategory;
import br.ufsc.gsigma.catalog.services.model.ProcessStandard;
import br.ufsc.gsigma.catalog.services.model.ProcessTreeDescription;
import br.ufsc.gsigma.catalog.services.persistence.output.ProcessInfo;

@WebService
public interface CatalogPersistenceService {

	public List<ProcessCategory> getProcessCategories();

	public List<ProcessTreeDescription> getListProcessTreeDescription(Integer categoryId);

	public Process getProcessById(Integer id);

	public List<ProcessInfo> getListProcessInfo(Integer processTreeDescriptionId);

	public boolean checkIfProcessNameIsAvailable(Integer processTreeDescriptionId, String processName);

	public Integer addOrUpdateProcess(Integer processCategoryParentId, Process process);

	public List<ProcessStandard> getListProcessStandard();

	public boolean ping();

	public Document getCatalogDocumentById(String id);

	public void removeAllProcesses();

	public List<ProcessInfo> findListProcessInfoByName(String processName);

	public Process getProcessByName(String name);

}
