package br.ufsc.gsigma.infrastructure.ws.jaxb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class JAXBBooleanMapAttributeAdapter extends XmlAdapter<BooleanMap, Map<String, Boolean>> {
	@Override
	public Map<String, Boolean> unmarshal(BooleanMap value) throws Exception {
		if (value != null) {
			Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();

			if (value.entries != null) {
				for (BooleanEntry e : value.entries)
					map.put(e.key, e.value);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public BooleanMap marshal(Map<String, Boolean> value) throws Exception {
		if (value != null) {
			BooleanMap map = new BooleanMap();
			map.entries = new ArrayList<BooleanEntry>(value.size());
			for (String key : value.keySet()) {
				BooleanEntry entry = new BooleanEntry();
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

class BooleanMap {
	@XmlElement(name = "entry")
	public List<BooleanEntry> entries;
}

class BooleanEntry {
	@XmlAttribute
	public String key;

	@XmlAttribute
	public Boolean value;
}