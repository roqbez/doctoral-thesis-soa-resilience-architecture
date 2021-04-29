package br.ufsc.gsigma.servicediscovery.support.jaxb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import br.ufsc.gsigma.servicediscovery.model.QoSAttribute;

public class QoSAttributeAttributeAdapter extends XmlAdapter<QoSAttributeList, Map<String, QoSAttribute>> {

	@Override
	public Map<String, QoSAttribute> unmarshal(QoSAttributeList value) throws Exception {
		if (value != null) {
			Map<String, QoSAttribute> map = new LinkedHashMap<String, QoSAttribute>();

			if (value.qoSAttributes != null) {
				for (QoSAttribute q : value.qoSAttributes)
					map.put(q.getKey(), q);
			}

			return map;
		} else {
			return null;
		}
	}

	@Override
	public QoSAttributeList marshal(Map<String, QoSAttribute> value) throws Exception {
		if (value != null) {
			QoSAttributeList l = new QoSAttributeList();
			l.qoSAttributes = new ArrayList<QoSAttribute>(value.size());
			for (QoSAttribute q : value.values()) {
				l.qoSAttributes.add(q);
			}
			return l;
		} else {
			return null;
		}
	}

}

class QoSAttributeList {
	@XmlElement(name = "qoSAttribute")
	public List<QoSAttribute> qoSAttributes;
}
