package br.ufsc.gsigma.catalog.services.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.catalog.services.interfaces.CatalogService;
import br.ufsc.gsigma.catalog.services.model.Document;
import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.model.ProcessCategory;
import br.ufsc.gsigma.catalog.services.model.ProcessStandard;
import br.ufsc.gsigma.catalog.services.model.ProcessTreeDescription;
import br.ufsc.gsigma.catalog.services.persistence.interfaces.CatalogPersistenceService;
import br.ufsc.gsigma.catalog.services.persistence.output.ProcessInfo;
import br.ufsc.gsigma.catalog.services.specifications.interfaces.CatalogSpecificationService;
import br.ufsc.gsigma.infrastructure.ws.WebServiceLocator;

public class CatalogServiceImpl implements CatalogService {

	private final static Logger logger = LoggerFactory.getLogger(CatalogServiceImpl.class);

	private CatalogPersistenceService catalogPersistenceService = null;

	private CatalogSpecificationService catalogSpecificationService = null;

	public CatalogServiceImpl() {
	}

	public CatalogServiceImpl(CatalogPersistenceService catalogPersistenceService, CatalogSpecificationService catalogSpecificationService) {
		this.catalogPersistenceService = catalogPersistenceService;
		this.catalogSpecificationService = catalogSpecificationService;
	}

	private CatalogPersistenceService getPersistenceService() {
		if (catalogPersistenceService == null)
			catalogPersistenceService = WebServiceLocator.locateService(WebServiceLocator.CATALOG_PERSISTENCE_SERVICE_UDDI_SERVICE_KEY, CatalogPersistenceService.class);

		return catalogPersistenceService;
	}

	private CatalogSpecificationService getSpecificationService() {
		if (catalogSpecificationService == null)
			catalogSpecificationService = WebServiceLocator.locateService(WebServiceLocator.CATALOG_SPECIFICATION_SERVICE_UDDI_SERVICE_KEY, CatalogSpecificationService.class);

		return catalogSpecificationService;
	}

	@Override
	public boolean ping() {
		return true;
	}

	@Override
	public Integer addOrUpdateProcess(Integer arg0, Process arg1) {
		return getPersistenceService().addOrUpdateProcess(arg0, arg1);
	}

	@Override
	public boolean checkIfProcessNameIsAvailable(Integer arg0, String arg1) {
		return getPersistenceService().checkIfProcessNameIsAvailable(arg0, arg1);
	}

	@Override
	public Process getProcessByName(String name) {
		return getPersistenceService().getProcessByName(name);
	}

	@Override
	public List<ProcessInfo> findListProcessInfoByName(String name) {
		return getPersistenceService().findListProcessInfoByName(name);
	}

	@Override
	public Document getCatalogDocumentById(String arg0) {
		return getPersistenceService().getCatalogDocumentById(arg0);
	}

	@Override
	public List<br.ufsc.gsigma.catalog.services.persistence.output.ProcessInfo> getListProcessInfo(Integer arg0) {
		return getPersistenceService().getListProcessInfo(arg0);
	}

	@Override
	public List<ProcessStandard> getListProcessStandard() {
		try {
			return getPersistenceService().getListProcessStandard();
		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}

	}

	@Override
	public List<ProcessTreeDescription> getListProcessTreeDescription(Integer arg0) {
		return getPersistenceService().getListProcessTreeDescription(arg0);
	}

	@Override
	public Process getProcessById(Integer arg0) {
		return getPersistenceService().getProcessById(arg0);
	}

	@Override
	public List<ProcessCategory> getProcessCategories() {
		return getPersistenceService().getProcessCategories();
	}

	@Override
	public byte[] getDocumentsXSDZipFromProcessStandardSpecification(String paramString) {
		return getSpecificationService().getDocumentsXSDZipFromProcessStandardSpecification(paramString);
	}

	@Override
	public Process getProcessFromProcessStandardSpecification(String paramString) {
		return getSpecificationService().getProcessFromProcessStandardSpecification(paramString);
	}

	@Override
	public br.ufsc.gsigma.catalog.services.specifications.output.ProcessTaxonomy getProcessTaxonomy(String paramString) {
		return getSpecificationService().getProcessTaxonomy(paramString);
	}

	@Override
	public br.ufsc.gsigma.catalog.services.specifications.output.ProcessStandardSpecification getProcessStandardSpecification(String paramString) {
		return getSpecificationService().getProcessStandardSpecification(paramString);
	}

	@Override
	public String getServiceWSDLFromTaxonomyClassification(String paramString) {
		return getSpecificationService().getServiceWSDLFromTaxonomyClassification(paramString);
	}

	@Override
	public void removeAllProcesses() {
		getPersistenceService().removeAllProcesses();
	}

}
