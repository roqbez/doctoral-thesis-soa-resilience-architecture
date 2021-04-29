package br.ufsc.gsigma.infrastructure.ws.cxf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.apache.cxf.common.jaxb.JAXBContextCache;
import org.apache.cxf.common.jaxb.JAXBContextCache.CachedContextAndSchemas;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.model.ServiceInfo;

public class MultipleServicesJAXBDataBindingInitializer {

	private List<ServiceInfo> servicesInfo = new LinkedList<ServiceInfo>();

	private Map<Service, JAXBDataBinding> serviceToDataBinding = new LinkedHashMap<Service, JAXBDataBinding>();

	public JAXBDataBinding createDataBinding() {
		return new LazyJAXBDataBinding();
	}

	public void initializeAll() {
		DataBinding cacheLoader = new JAXBDataBinding();
		ServiceImpl service = new ServiceImpl(servicesInfo);
		service.put("org.apache.cxf.databinding.namespace", "true");
		cacheLoader.initialize(service);

		for (Entry<Service, JAXBDataBinding> e : new HashMap<Service, JAXBDataBinding>(serviceToDataBinding).entrySet()) {
			((LazyJAXBDataBinding) e.getValue()).initializeSuper(e.getKey());
		}
	}

	private final class LazyJAXBDataBinding extends JAXBDataBinding {
		@Override
		public synchronized void initialize(Service service) {
			service.put("org.apache.cxf.databinding.namespace", "true");
			servicesInfo.addAll(service.getServiceInfos());
			serviceToDataBinding.put(service, this);
		}

		public synchronized void initializeSuper(Service service) {
			super.initialize(service);
		}

		public CachedContextAndSchemas createJAXBContextAndSchemas(Set<Class<?>> classes, String defaultNs) throws JAXBException {
			JAXBContextCache.scanPackages(classes);
			return JAXBContextCache.getCachedContextAndSchemas(classes, defaultNs, getContextProperties(), new ArrayList<Object>(), false);
		}
	}

}
