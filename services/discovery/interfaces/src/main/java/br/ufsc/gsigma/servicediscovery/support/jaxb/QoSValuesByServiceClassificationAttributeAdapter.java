package br.ufsc.gsigma.servicediscovery.support.jaxb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.infrastructure.ws.jaxb.JAXBDoubleMapAttributeAdapter;

public class QoSValuesByServiceClassificationAttributeAdapter extends XmlAdapter<QoSValuesByServiceClassificationMap, Map<String, Map<String, Double>>> {

	@Override
	public Map<String, Map<String, Double>> unmarshal(QoSValuesByServiceClassificationMap value) throws Exception {
		if (value != null) {
			Map<String, Map<String, Double>> map = new LinkedHashMap<String, Map<String, Double>>();

			if (value.entries != null) {
				for (QoSValuesByServiceClassificationEntry e : value.entries)
					map.put(e.serviceClassification, e.qoSAttributes);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public QoSValuesByServiceClassificationMap marshal(Map<String, Map<String, Double>> value) throws Exception {
		if (value != null) {
			QoSValuesByServiceClassificationMap map = new QoSValuesByServiceClassificationMap();
			map.entries = new ArrayList<QoSValuesByServiceClassificationEntry>(value.size());
			for (String key : value.keySet()) {
				QoSValuesByServiceClassificationEntry entry = new QoSValuesByServiceClassificationEntry();
				entry.serviceClassification = key;
				entry.qoSAttributes = value.get(key);
				map.entries.add(entry);
			}
			return map;
		} else {
			return null;
		}
	}

}

class QoSValuesByServiceClassificationMap {
	@XmlElement(name = "serviceClassification")
	public List<QoSValuesByServiceClassificationEntry> entries;
}

class QoSValuesByServiceClassificationEntry {

	@XmlAttribute(name = "name")
	public String serviceClassification;

	@XmlJavaTypeAdapter(JAXBDoubleMapAttributeAdapter.class)
	public Map<String, Double> qoSAttributes;
}