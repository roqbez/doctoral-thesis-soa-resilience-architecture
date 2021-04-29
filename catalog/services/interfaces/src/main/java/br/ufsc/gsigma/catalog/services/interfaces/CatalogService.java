package br.ufsc.gsigma.catalog.services.interfaces;

import javax.jws.WebService;

import br.ufsc.gsigma.catalog.services.persistence.interfaces.CatalogPersistenceService;
import br.ufsc.gsigma.catalog.services.specifications.interfaces.CatalogSpecificationService;

@WebService
public interface CatalogService extends CatalogPersistenceService, CatalogSpecificationService {

	public boolean ping();

}
