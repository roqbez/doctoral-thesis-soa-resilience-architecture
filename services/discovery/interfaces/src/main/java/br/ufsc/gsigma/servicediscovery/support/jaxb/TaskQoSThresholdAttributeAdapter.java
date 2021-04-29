package br.ufsc.gsigma.servicediscovery.support.jaxb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import br.ufsc.gsigma.servicediscovery.model.QoSThreshold;

public class TaskQoSThresholdAttributeAdapter extends XmlAdapter<TaskQoSThresholdsMap, Map<String, List<QoSThreshold>>> {

	@Override
	public Map<String, List<QoSThreshold>> unmarshal(TaskQoSThresholdsMap value) throws Exception {
		if (value != null) {
			Map<String, List<QoSThreshold>> map = new LinkedHashMap<String, List<QoSThreshold>>();

			if (value.entries != null) {
				for (TaskQoSThresholdsEntry e : value.entries)
					map.put(e.taskName, e.qoSThresholds);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public TaskQoSThresholdsMap marshal(Map<String, List<QoSThreshold>> value) throws Exception {
		if (value != null) {
			TaskQoSThresholdsMap map = new TaskQoSThresholdsMap();
			map.entries = new ArrayList<TaskQoSThresholdsEntry>(value.size());
			for (String key : value.keySet()) {
				TaskQoSThresholdsEntry entry = new TaskQoSThresholdsEntry();
				entry.taskName = key;
				entry.qoSThresholds = value.get(key);
				map.entries.add(entry);
			}
			return map;
		} else {
			return null;
		}
	}

}

class TaskQoSThresholdsMap {
	@XmlElement(name = "serviceClassification")
	public List<TaskQoSThresholdsEntry> entries;
}

class TaskQoSThresholdsEntry {

	@XmlAttribute(name = "taskName")
	public String taskName;

	@XmlElement(name = "qoSThreshold")
	public List<QoSThreshold> qoSThresholds;
}