package br.ufsc.gsigma.catalog.services.specifications.interfaces;

import javax.jws.WebService;

import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessTaxonomy;
import br.ufsc.gsigma.catalog.services.specifications.output.ProcessStandardSpecification;

@WebService
public interface CatalogSpecificationService {

	public boolean ping();

	public String getServiceWSDLFromTaxonomyClassification(String taxonomyClassification);

	public ProcessStandardSpecification getProcessStandardSpecification(String processStandardId);

	public byte[] getDocumentsXSDZipFromProcessStandardSpecification(String processStandardId);

	public ProcessTaxonomy getProcessTaxonomy(String processStandardId);

	public Process getProcessFromProcessStandardSpecification(String taxonomyClassification);

}
