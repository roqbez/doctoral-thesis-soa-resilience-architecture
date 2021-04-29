package br.ufsc.gsigma.infrastructure.ws.jaxb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class JAXBDoubleMapAttributeAdapter extends XmlAdapter<DoubleMap, Map<String, Double>> {
	@Override
	public Map<String, Double> unmarshal(DoubleMap value) throws Exception {
		if (value != null) {
			Map<String, Double> map = new LinkedHashMap<String, Double>();

			if (value.entries != null) {
				for (DoubleEntry e : value.entries)
					map.put(e.key, e.value);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public DoubleMap marshal(Map<String, Double> value) throws Exception {
		if (value != null) {
			DoubleMap map = new DoubleMap();
			map.entries = new ArrayList<DoubleEntry>(value.size());
			for (String key : value.keySet()) {
				DoubleEntry entry = new DoubleEntry();
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

class DoubleMap {
	@XmlElement(name = "entry")
	public List<DoubleEntry> entries;
}

class DoubleEntry {
	@XmlAttribute
	public String key;

	@XmlAttribute
	public Double value;
}