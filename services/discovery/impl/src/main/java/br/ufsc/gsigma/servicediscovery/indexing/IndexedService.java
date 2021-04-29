package br.ufsc.gsigma.servicediscovery.indexing;

import java.util.List;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TermVector;

import br.ufsc.gsigma.servicediscovery.model.DiscoveredService;
import br.ufsc.gsigma.servicediscovery.model.ServiceProvider;
import br.ufsc.gsigma.servicediscovery.model.ServiceQoSInformationItem;

@Indexed
public class IndexedService extends DiscoveredService {

	private static final long serialVersionUID = 1L;

	public IndexedService() {
	}

	public IndexedService(DiscoveredService service) {
		setBindingTemplateKey(service.getBindingTemplateKey());
		setServiceClassification(service.getServiceClassification());
		setServiceProtocolConverter(service.getServiceProtocolConverter());
		setServiceEndpointURL(service.getServiceEndpointURL());
		setServiceKey(service.getServiceKey());
		setServiceName(service.getServiceName());
		setServiceProvider(service.getServiceProvider());
		setUddiRepositoryEndpointURL(service.getUddiRepositoryEndpointURL());

		if (service.getQoSInformation() != null)
			getQoSInformation().addAll(service.getQoSInformation());

		if (service.getServiceProvider() != null)
			setServiceProvider(new IndexedServiceProvider(service.getServiceProvider()));
	}

	// Esse precisa ser o primeiro field (a ordem é importante)
	@Field(analyze = Analyze.NO, store = Store.YES, norms = Norms.NO, termVector = TermVector.YES)
	@Override
	public String getServiceClassification() {
		return super.getServiceClassification();
	}

	@Override
	@IndexedEmbedded(targetElement = IndexedServiceProvider.class)
	public ServiceProvider getServiceProvider() {
		return super.getServiceProvider();
	}

	@Override
	@Field(analyze = Analyze.NO, norms = Norms.NO)
	@FieldBridge(impl = QosAttributeFieldBridge.class)
	public List<ServiceQoSInformationItem> getQoSInformation() {
		return super.getQoSInformation();
	}

	@Override
	@Field(analyze = Analyze.NO, norms = Norms.NO, termVector = TermVector.YES)
	public String getServiceKey() {
		return super.getServiceKey();
	}

	@Override
	@Field(analyze = Analyze.NO, norms = Norms.NO)
	public String getServiceProtocolConverter() {
		return super.getServiceProtocolConverter();
	}

	@Field(analyze = Analyze.NO, norms = Norms.NO)
	@Override
	public String getUddiRepositoryEndpointURL() {
		return super.getUddiRepositoryEndpointURL();
	}

	@Field(analyze = Analyze.NO, norms = Norms.NO)
	@Override
	public String getServiceName() {
		return super.getServiceName();
	}

	@Field(analyze = Analyze.NO, norms = Norms.NO, termVector = TermVector.YES)
	@Override
	public String getServiceEndpointURL() {
		return super.getServiceEndpointURL();
	}

	@Field(analyze = Analyze.NO, norms = Norms.NO)
	@Override
	public String getServiceEndpointHostPort() {
		return super.getServiceEndpointHostPort();
	}

	@Field(analyze = Analyze.NO, norms = Norms.NO)
	@Override
	public String getBindingTemplateKey() {
		return super.getBindingTemplateKey();
	}

}
