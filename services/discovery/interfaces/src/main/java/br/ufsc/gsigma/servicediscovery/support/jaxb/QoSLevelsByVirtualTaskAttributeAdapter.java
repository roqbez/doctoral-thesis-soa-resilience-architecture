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

public class QoSLevelsByVirtualTaskAttributeAdapter extends XmlAdapter<QoSLevelsByVirtualTaskMap, Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>>> {

	@Override
	public Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> unmarshal(QoSLevelsByVirtualTaskMap value) throws Exception {
		if (value != null) {
			Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> map = new LinkedHashMap<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>>();

			if (value.entries != null) {
				for (QoSLevelsByVirtualTaskEntry e : value.entries)
					map.put(e.name, e.qoSAttributes);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public QoSLevelsByVirtualTaskMap marshal(Map<String, Map<String, NavigableMap<Integer, List<QoSLevel>>>> value) throws Exception {
		if (value != null) {
			QoSLevelsByVirtualTaskMap map = new QoSLevelsByVirtualTaskMap();
			map.entries = new ArrayList<QoSLevelsByVirtualTaskEntry>(value.size());
			for (String key : value.keySet()) {
				QoSLevelsByVirtualTaskEntry entry = new QoSLevelsByVirtualTaskEntry();
				entry.name = key;
				entry.qoSAttributes = value.get(key);
				map.entries.add(entry);
			}
			return map;
		} else {
			return null;
		}
	}

}

class QoSLevelsByVirtualTaskMap {
	@XmlElement(name = "virtualTask")
	public List<QoSLevelsByVirtualTaskEntry> entries;
}

class QoSLevelsByVirtualTaskEntry {

	@XmlAttribute(name = "name")
	public String name;

	@XmlJavaTypeAdapter(QoSAttributesNumberOfLevelsAttributeAdapter.class)
	public Map<String, NavigableMap<Integer, List<QoSLevel>>> qoSAttributes;
}