package br.ufsc.gsigma.catalog.plugin.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMQoSValue;
import br.ufsc.gsigma.catalog.plugin.modules.converter.model.process.BMServiceAssociation;
import br.ufsc.gsigma.catalog.services.model.ServiceAssociation;
import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.ServiceQoSInformationItem;

public abstract class ServiceAssociationUtil {

	public static List<BMServiceAssociation> getBMServiceAssociations(List<ServiceAssociation> services) {

		List<BMServiceAssociation> l = new LinkedList<BMServiceAssociation>();

		if (services != null) {
			for (ServiceAssociation s : services)
				l.add(getBMServiceAssociation(s));
		}

		return l;
	}

	public static BMServiceAssociation getBMServiceAssociation(ServiceAssociation s) {

		BMServiceAssociation serviceAssociation = new BMServiceAssociation();

		serviceAssociation.setServiceName(s.getServiceName());
		serviceAssociation.setServiceEndpoint(s.getServiceEndpoint());
		serviceAssociation.setServiceProtocolConverter(s.getServiceProtocolConverter());

		serviceAssociation.setBindingTemplateKey(s.getBindingTemplateKey());
		serviceAssociation.setServiceKey(s.getServiceKey());

		return serviceAssociation;
	}

	public static List<ServiceAssociation> getServiceAssociations(List<BMServiceAssociation> services) {

		List<ServiceAssociation> l = new LinkedList<ServiceAssociation>();

		if (services != null) {
			for (BMServiceAssociation s : services)
				l.add(getServiceAssociation(s));
		}

		return l;
	}

	public static ServiceAssociation getServiceAssociation(BMServiceAssociation s) {

		ServiceAssociation serviceAssociation = new ServiceAssociation();

		serviceAssociation.setServiceName(s.getServiceName());
		serviceAssociation.setServiceEndpoint(s.getServiceEndpoint());

		serviceAssociation.setBindingTemplateKey(s.getBindingTemplateKey());
		serviceAssociation.setServiceKey(s.getServiceKey());
		serviceAssociation.setServiceUtility(s.getServiceUtility());

		return serviceAssociation;
	}

	public static BMServiceAssociation getBMServiceAssociation(DiscoveredService service) {

		BMServiceAssociation serviceAssociation = new BMServiceAssociation();

		serviceAssociation.setServiceName(service.getServiceName());
		serviceAssociation.setServiceEndpoint(service.getServiceEndpointURL());

		if (service.getServiceProvider() != null)
			serviceAssociation.setServiceProviderName(service.getServiceProvider().getName());

		serviceAssociation.setBindingTemplateKey(service.getBindingTemplateKey());
		serviceAssociation.setServiceKey(service.getServiceKey());

		serviceAssociation.setServiceUtility(service.getUtility());
		serviceAssociation.setServiceProtocolConverter(service.getServiceProtocolConverter());

		// QoS
		if (service.getQoSInformation() != null) {

			List<BMQoSValue> qoSValues = new ArrayList<BMQoSValue>(service.getQoSInformation().size());

			for (ServiceQoSInformationItem info : service.getQoSInformation())
				qoSValues.add(new BMQoSValue(info.getQoSItemName(), info.getQoSAttributeName(), info.getQoSValue()));

			serviceAssociation.setQoSValues(qoSValues);
		}

		return serviceAssociation;
	}

}
