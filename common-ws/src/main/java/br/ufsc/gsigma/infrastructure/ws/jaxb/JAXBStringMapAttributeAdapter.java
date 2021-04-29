package br.ufsc.gsigma.infrastructure.ws.jaxb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class JAXBStringMapAttributeAdapter extends XmlAdapter<StringMap, Map<String, String>> {
	@Override
	public Map<String, String> unmarshal(StringMap value) throws Exception {
		if (value != null) {
			Map<String, String> map = new LinkedHashMap<String, String>();

			if (value.entries != null) {
				for (StringEntry e : value.entries)
					map.put(e.key, e.value);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public StringMap marshal(Map<String, String> value) throws Exception {
		if (value != null) {
			StringMap map = new StringMap();
			map.entries = new ArrayList<StringEntry>(value.size());
			for (String key : value.keySet()) {
				StringEntry entry = new StringEntry();
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

class StringMap {
	@XmlElement(name = "entry")
	public List<StringEntry> entries;
}

class StringEntry {
	@XmlAttribute
	public String key;

	@XmlAttribute
	public String value;
}