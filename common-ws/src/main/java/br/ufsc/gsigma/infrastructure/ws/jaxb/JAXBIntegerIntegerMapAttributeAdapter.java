package br.ufsc.gsigma.infrastructure.ws.jaxb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class JAXBIntegerIntegerMapAttributeAdapter extends XmlAdapter<IntegerIntegerMap, Map<Integer, Integer>> {
	@Override
	public Map<Integer, Integer> unmarshal(IntegerIntegerMap value) throws Exception {
		if (value != null) {
			Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();

			if (value.entries != null) {
				for (IntegerIntegerEntry e : value.entries)
					map.put(e.key, e.value);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public IntegerIntegerMap marshal(Map<Integer, Integer> value) throws Exception {
		if (value != null) {
			IntegerIntegerMap map = new IntegerIntegerMap();
			map.entries = new ArrayList<IntegerIntegerEntry>(value.size());
			for (Integer key : value.keySet()) {
				IntegerIntegerEntry entry = new IntegerIntegerEntry();
				entry.key = key;
				entry.value = value.get(key);
				map.entries.add(entry);
			}
			return map;
		} else {
			return null;
		}

	}
}

class IntegerIntegerMap {
	@XmlElement(name = "entry")
	public List<IntegerIntegerEntry> entries;
}

class IntegerIntegerEntry {
	@XmlAttribute
	public Integer key;

	@XmlAttribute
	public Integer value;
}