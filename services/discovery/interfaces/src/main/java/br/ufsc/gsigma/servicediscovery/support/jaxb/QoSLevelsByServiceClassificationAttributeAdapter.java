package br.ufsc.gsigma.servicediscovery.support.jaxb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import br.ufsc.gsigma.servicediscovery.model.QoSLevel;

public class QoSLevelsByServiceClassificationAttributeAdapter extends
		XmlAdapter<QoSLevelsByServiceClassificationMap, Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>>> {

	@Override
	public Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> unmarshal(QoSLevelsByServiceClassificationMap value) throws Exception {
		if (value != null) {
			Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> map = new LinkedHashMap<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>>();

			if (value.entries != null) {
				for (QoSLevelsByServiceClassificationEntry e : value.entries)
					map.put(e.serviceClassification, e.qoSAttributes);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public QoSLevelsByServiceClassificationMap marshal(Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> value) throws Exception {
		if (value != null) {
			QoSLevelsByServiceClassificationMap map = new QoSLevelsByServiceClassificationMap();
			map.entries = new ArrayList<QoSLevelsByServiceClassificationEntry>(value.size());
			for (String key : value.keySet()) {
				QoSLevelsByServiceClassificationEntry entry = new QoSLevelsByServiceClassificationEntry();
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

class QoSLevelsByServiceClassificationMap {
	@XmlElement(name = "serviceClassification")
	public List<QoSLevelsByServiceClassificationEntry> entries;
}

class QoSLevelsByServiceClassificationEntry {

	@XmlAttribute(name = "name")
	public String serviceClassification;

	@XmlJavaTypeAdapter(QoSAttributesNumberOfLevelsAttributeAdapter.class)
	public Map<String, NavigableMap<Integer, List<QoSLevel>>> qoSAttributes;
}