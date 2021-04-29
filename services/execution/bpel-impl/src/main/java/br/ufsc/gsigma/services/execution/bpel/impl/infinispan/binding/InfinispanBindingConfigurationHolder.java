package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.binding;

import java.io.Serializable;

import br.ufsc.gsigma.binding.model.BindingConfiguration;
import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanObject;

public class InfinispanBindingConfigurationHolder implements InfinispanObject<Integer>, Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private BindingConfiguration bindingConfiguration;

	public InfinispanBindingConfigurationHolder(Integer id, BindingConfiguration bindingConfiguration) {
		this.id = id;
		this.bindingConfiguration = bindingConfiguration;
	}

	@Override
	public Integer getId() {
		return id;
	}

	public BindingConfiguration getBindingConfiguration() {
		return bindingConfiguration;
	}

}
