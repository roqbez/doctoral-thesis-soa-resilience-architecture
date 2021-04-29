package br.ufsc.gsigma.servicediscovery.interfaces;

import java.util.List;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.catalog.services.model.Process;
import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBIntegerMapAttributeAdapter;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryRequest;
import br.ufsc.gsigma.servicediscovery.model.DiscoveryResult;
import br.ufsc.gsigma.servicediscovery.model.ProcessQoSInfo;
import br.ufsc.gsigma.servicediscovery.model.ProcessQoSThresholds;
import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;
import br.ufsc.gsigma.servicediscovery.model.QoSInformation;
import br.ufsc.gsigma.servicediscovery.model.QoSItem;
import br.ufsc.gsigma.servicediscovery.model.QoSLevelsRequest;
import br.ufsc.gsigma.servicediscovery.model.QoSLevelsResponse;
import br.ufsc.gsigma.servicediscovery.model.ServiceProvider;
import br.ufsc.gsigma.servicediscovery.model.ServicesQoSInformation;
import br.ufsc.gsigma.servicediscovery.support.ObjectFactory;
import br.ufsc.gsigma.servicediscovery.support.jaxb.QoSAttributeAttributeAdapter;

@WebService(targetNamespace = "http://gsigma.ufsc.br/serviceDiscovery", name = "DiscoveryService", serviceName = "DiscoveryService")
@XmlSeeAlso({ ObjectFactory.class })
public interface DiscoveryService {

	@WebResult(name = "discoveryResult")
	public DiscoveryResult discoverServices(@WebParam(name = "serviceClassification") String serviceClassification, @WebParam(name = "qoSInformation") QoSInformation qoSInformation) throws Exception;

	public DiscoveryResult discover(@WebParam(name = "request") DiscoveryRequest request) throws Exception;

	@WebResult(name = "serviceEndpoint")
	public String discoverUniqueServiceEndpoint(@WebParam(name = "serviceClassification") String serviceClassification, @WebParam(name = "qoSInformation") QoSInformation qoSInformation) throws Exception;

	@WebResult(name = "qoSItem")
	public List<QoSItem> getQoSItems();

	@WebResult(name = "qoSItem")
	public List<QoSItem> getEnabledQoSItems();

	@WebResult(name = "qoSLevelsResponse")
	public QoSLevelsResponse getQoSLevels(@WebParam(name = "request") QoSLevelsRequest request) throws Exception;

	@XmlJavaTypeAdapter(QoSAttributeAttributeAdapter.class)
	public Map<String, QoSAttribute> getQoSAttributes();

	@XmlJavaTypeAdapter(QoSAttributeAttributeAdapter.class)
	public Map<String, QoSAttribute> getEnabledQoSAttributes();

	@WebResult(name = "servicesQoSInformation")
	public ServicesQoSInformation getServicesQoSInformation(@WebParam(name = "serviceKey") List<String> servicesKeys) throws Exception;

	@WebResult(name = "processQoSInfo")
	public ProcessQoSInfo getLocalQoSConstraints(@WebParam(name = "process") Process process, @WebParam(name = "qoSInformation") QoSInformation qoSInformation);

	@WebResult(name = "processQoSThresholds")
	public ProcessQoSThresholds getProcessQoSThresholds(@WebParam(name = "process") Process process);

	@XmlJavaTypeAdapter(JAXBIntegerMapAttributeAdapter.class)
	@WebResult(name = "numberOfServicesPerServiceClassification")
	public Map<String, Integer> getNumberOfServicesPerServiceClassification();

	@WebResult(name = "serviceProvider")
	public List<ServiceProvider> getServiceProviders();

}
