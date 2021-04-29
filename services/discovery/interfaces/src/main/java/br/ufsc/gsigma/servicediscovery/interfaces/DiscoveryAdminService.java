package br.ufsc.gsigma.servicediscovery.interfaces;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;

import br.ufsc.gsigma.servicediscovery.support.ObjectFactory;

@WebService(targetNamespace = "http://gsigma.ufsc.br/serviceDiscovery", name = "DiscoveryAdminService", serviceName = "DiscoveryAdminService")
@XmlSeeAlso({ ObjectFactory.class })
public interface DiscoveryAdminService {

	@WebResult(name = "rebuilding")
	public boolean rebuildIndex() throws Exception;

	@WebResult(name = "rebuilding")
	public boolean rebuildServiceClassificationInfo() throws Exception;

	@WebResult(name = "rebuilding")
	public boolean rebuildQoSLevels() throws Exception;

	@WebResult(name = "populating")
	public boolean populateExampleServices(@WebParam(name = "numberOfPublishers") int numberOfPublishers, @WebParam(name = "numberOfServers") int numberOfServers, @WebParam(name = "serviceClassification") List<String> servicesClassifications)
			throws Exception;

}
