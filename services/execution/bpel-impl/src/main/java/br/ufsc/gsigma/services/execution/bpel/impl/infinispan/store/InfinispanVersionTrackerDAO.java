package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.store;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanObject;

public class InfinispanVersionTrackerDAO implements InfinispanObject<String> {

	public static final String KEY = "VERSION";

	private static final long serialVersionUID = -4659536779674346519L;

	private long version;

	public InfinispanVersionTrackerDAO(long version) {
		this.version = version;
	}

	@Override
	public String getId() {
		return KEY;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

}
