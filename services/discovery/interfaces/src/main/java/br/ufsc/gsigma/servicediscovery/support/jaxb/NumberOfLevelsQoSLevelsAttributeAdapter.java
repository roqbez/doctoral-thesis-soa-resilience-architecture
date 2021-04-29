package br.ufsc.gsigma.servicediscovery.support.jaxb;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import br.ufsc.gsigma.servicediscovery.model.QoSLevel;

public class NumberOfLevelsQoSLevelsAttributeAdapter extends XmlAdapter<NumberOfLevelsQoSLevelsMap, NavigableMap<Integer, List<QoSLevel>>> {

	@Override
	public NavigableMap<Integer, List<QoSLevel>> unmarshal(NumberOfLevelsQoSLevelsMap value) throws Exception {
		if (value != null) {
			NavigableMap<Integer, List<QoSLevel>> map = new TreeMap<Integer, List<QoSLevel>>();

			if (value.entries != null) {
				for (NumberOfLevelsQoSLevelsEntry e : value.entries)
					map.put(e.numberOfLevels, e.levels);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public NumberOfLevelsQoSLevelsMap marshal(NavigableMap<Integer, List<QoSLevel>> value) throws Exception {
		if (value != null) {
			NumberOfLevelsQoSLevelsMap map = new NumberOfLevelsQoSLevelsMap();
			map.entries = new ArrayList<NumberOfLevelsQoSLevelsEntry>(value.size());
			for (Integer key : value.keySet()) {
				NumberOfLevelsQoSLevelsEntry entry = new NumberOfLevelsQoSLevelsEntry();
				entry.numberOfLevels = key;
				entry.levels = value.get(key);
				map.entries.add(entry);
			}
			return map;
		} else {
			return null;
		}
	}

}

class NumberOfLevelsQoSLevelsMap {
	@XmlElement(name = "qoSLevelGroup")
	public List<NumberOfLevelsQoSLevelsEntry> entries;
}

class NumberOfLevelsQoSLevelsEntry {

	@XmlAttribute
	public Integer numberOfLevels;

	@XmlElement(name = "level")
	public List<QoSLevel> levels;
}