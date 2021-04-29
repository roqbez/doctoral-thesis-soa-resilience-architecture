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

public class QoSAttributesNumberOfLevelsAttributeAdapter extends XmlAdapter<QoSAttributesNumberOfLevelsMap, Map<String, NavigableMap<Integer, List<QoSLevel>>>> {

	@Override
	public Map<String, NavigableMap<Integer, List<QoSLevel>>> unmarshal(QoSAttributesNumberOfLevelsMap value) throws Exception {
		if (value != null) {
			Map<String, NavigableMap<Integer, List<QoSLevel>>> map = new LinkedHashMap<String, NavigableMap<Integer, List<QoSLevel>>>();

			if (value.entries != null) {
				for (QoSAttributesNumberOfLevelsEntry e : value.entries)
					map.put(e.qoSKey, e.groups);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public QoSAttributesNumberOfLevelsMap marshal(Map<String, NavigableMap<Integer, List<QoSLevel>>> value) throws Exception {
		if (value != null) {
			QoSAttributesNumberOfLevelsMap map = new QoSAttributesNumberOfLevelsMap();
			map.entries = new ArrayList<QoSAttributesNumberOfLevelsEntry>(value.size());
			for (String key : value.keySet()) {
				QoSAttributesNumberOfLevelsEntry entry = new QoSAttributesNumberOfLevelsEntry();
				entry.qoSKey = key;
				entry.groups = value.get(key);
				map.entries.add(entry);
			}
			return map;
		} else {
			return null;
		}
	}

}

class QoSAttributesNumberOfLevelsMap {
	@XmlElement(name = "qoSAttribute")
	public List<QoSAttributesNumberOfLevelsEntry> entries;
}

class QoSAttributesNumberOfLevelsEntry {

	@XmlAttribute(name = "key")
	public String qoSKey;

	@XmlJavaTypeAdapter(NumberOfLevelsQoSLevelsAttributeAdapter.class)
	public NavigableMap<Integer, List<QoSLevel>> groups;
}