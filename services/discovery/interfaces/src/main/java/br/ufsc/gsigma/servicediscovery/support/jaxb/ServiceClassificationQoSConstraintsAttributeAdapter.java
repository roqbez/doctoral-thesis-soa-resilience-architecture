package br.ufsc.gsigma.servicediscovery.support.jaxb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import br.ufsc.gsigma.servicediscovery.model.QoSConstraint;

public class ServiceClassificationQoSConstraintsAttributeAdapter extends XmlAdapter<ServiceClassificationQoSConstraintsMap, Map<String, List<QoSConstraint>>> {

	@Override
	public Map<String, List<QoSConstraint>> unmarshal(ServiceClassificationQoSConstraintsMap value) throws Exception {
		if (value != null) {
			Map<String, List<QoSConstraint>> map = new LinkedHashMap<String, List<QoSConstraint>>();

			if (value.entries != null) {
				for (ServiceClassificationQoSConstraintsEntry e : value.entries)
					map.put(e.serviceClassification, e.qoSConstraints);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public ServiceClassificationQoSConstraintsMap marshal(Map<String, List<QoSConstraint>> value) throws Exception {
		if (value != null) {
			ServiceClassificationQoSConstraintsMap map = new ServiceClassificationQoSConstraintsMap();
			map.entries = new ArrayList<ServiceClassificationQoSConstraintsEntry>(value.size());
			for (String key : value.keySet()) {
				ServiceClassificationQoSConstraintsEntry entry = new ServiceClassificationQoSConstraintsEntry();
				entry.serviceClassification = key;
				entry.qoSConstraints = value.get(key);
				map.entries.add(entry);
			}
			return map;
		} else {
			return null;
		}
	}

}

class ServiceClassificationQoSConstraintsMap {
	@XmlElement(name = "serviceClassification")
	public List<ServiceClassificationQoSConstraintsEntry> entries;
}

class ServiceClassificationQoSConstraintsEntry {

	@XmlAttribute(name = "name")
	public String serviceClassification;

	@XmlElement(name = "qoSConstraint")
	public List<QoSConstraint> qoSConstraints;
}