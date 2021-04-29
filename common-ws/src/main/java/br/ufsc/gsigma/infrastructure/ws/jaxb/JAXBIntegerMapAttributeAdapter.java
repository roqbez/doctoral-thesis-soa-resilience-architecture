package br.ufsc.gsigma.infrastructure.ws.jaxb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class JAXBIntegerMapAttributeAdapter extends XmlAdapter<IntegerMap, Map<String, Integer>> {
	@Override
	public Map<String, Integer> unmarshal(IntegerMap value) throws Exception {
		if (value != null) {
			Map<String, Integer> map = new LinkedHashMap<String, Integer>();

			if (value.entries != null) {
				for (IntegerEntry e : value.entries)
					map.put(e.key, e.value);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public IntegerMap marshal(Map<String, Integer> value) throws Exception {
		if (value != null) {
			IntegerMap map = new IntegerMap();
			map.entries = new ArrayList<IntegerEntry>(value.size());
			for (String key : value.keySet()) {
				IntegerEntry entry = new IntegerEntry();
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

class IntegerMap {
	@XmlElement(name = "entry")
	public List<IntegerEntry> entries;
}

class IntegerEntry {
	@XmlAttribute
	public String key;

	@XmlAttribute
	public Integer value;
}