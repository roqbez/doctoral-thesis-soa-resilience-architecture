package br.ufsc.gsigma.services.execution.bpel.impl.infinispan;

import java.io.Serializable;

public interface InfinispanObject<K extends Serializable> extends Serializable {

	public K getId();

}
