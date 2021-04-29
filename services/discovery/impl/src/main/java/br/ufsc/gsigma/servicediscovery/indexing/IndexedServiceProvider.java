package br.ufsc.gsigma.servicediscovery.indexing;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Norms;

import br.ufsc.gsigma.servicediscovery.model.ServiceProvider;

public class IndexedServiceProvider extends ServiceProvider {

	private static final long serialVersionUID = 1L;

	public IndexedServiceProvider() {
	}

	public IndexedServiceProvider(ServiceProvider serviceProvider) {
		setName(serviceProvider.getName());
		setIdentification(serviceProvider.getIdentification());
	}

	@Override
	@Field(analyze = Analyze.NO, norms = Norms.NO)
	public String getName() {
		return super.getName();
	}

	@Override
	@Field(analyze = Analyze.NO, norms = Norms.NO)
	public String getIdentification() {
		return super.getIdentification();
	}

}
