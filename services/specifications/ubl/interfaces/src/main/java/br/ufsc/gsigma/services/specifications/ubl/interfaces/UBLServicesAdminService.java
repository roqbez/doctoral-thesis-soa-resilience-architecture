package br.ufsc.gsigma.services.specifications.ubl.interfaces;

import java.util.List;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBIntegerMapAttributeAdapter;

@WebService
public interface UBLServicesAdminService {

	public static final String STATUS_SERVICE_AVAILABLE = "available";

	public static final String STATUS_SERVICE_UNAVAILABLE = "unavailable";

	public String changeServiceAvailability(@WebParam(name = "servicePath") String servicePath);

	public String setServiceAvailable(@WebParam(name = "servicePath") String servicePath);

	public String setServiceUnavailable(@WebParam(name = "servicePath") String servicePath);

	public String setRandomServiceAvailability(@WebParam(name = "servicePath") String servicePath);

	public List<String> setRandomServiceAvailabilityCombination(@WebParam(name = "servicePathNumberOfServices") @XmlJavaTypeAdapter(JAXBIntegerMapAttributeAdapter.class) Map<String, Integer> servicePath, @WebParam(name = "host") List<String> hosts);

	public void setServicesAvailability(@WebParam(name = "availableServicesPath") List<String> availableServicesPaths, @WebParam(name = "unavailableServicesPath") List<String> unavailableServicesPaths);

	public boolean isServiceAvailable(@WebParam(name = "servicePath") String servicePath);

	public List<String> getServiceAvailability(@WebParam(name = "servicePath") String servicePath, @WebParam(name = "host") List<String> hosts);

	public List<String> setHostsServicesAvailability(@WebParam(name = "servicePath") List<String> servicesPath, @WebParam(name = "host") List<String> hosts, @WebParam(name = "available") boolean available);

}
