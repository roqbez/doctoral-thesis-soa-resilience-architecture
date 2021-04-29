package br.ufsc.gsigma.infrastructure.util.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Value;
import org.simpleframework.xml.stream.NodeMap;

public class CyclicNoClassAttributeSimpleXMLPersisterStrategy extends CycleStrategy {

	public CyclicNoClassAttributeSimpleXMLPersisterStrategy() {
		super("refId", "reference");
	}

	@SuppressWarnings("all")
	@Override
	public Value read(Type type, NodeMap node, Map map) throws Exception {
		if (type.getType().equals(List.class))
			node.put("class", ArrayList.class.getName());
		return super.read(type, node, map);
	}

	@SuppressWarnings("all")
	@Override
	public boolean write(Type type, Object value, NodeMap node, Map map) {
		boolean result = super.write(type, value, node, map);
		node.remove("class");
		return result;
	}

}
