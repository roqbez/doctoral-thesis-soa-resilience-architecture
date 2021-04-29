package br.ufsc.gsigma.services.execution.bpel.impl.infinispan.bpel;

import org.infinispan.counter.api.StrongCounter;

import br.ufsc.gsigma.services.execution.bpel.impl.infinispan.InfinispanDatabase;

public abstract class InfinispanIdGen {

	private static StrongCounter PROC_ID;
	private static StrongCounter PROC_INST_ID;
	private static StrongCounter SCOPE_ID;
	private static StrongCounter CSET_ID;

	public static Long newProcessId() {
		try {
			return getProcessIdCounter().incrementAndGet().get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static StrongCounter getProcessIdCounter() {
		if (PROC_ID == null) {
			synchronized (InfinispanIdGen.class) {
				if (PROC_ID == null) {
					PROC_ID = InfinispanDatabase.getCounter("COUNTER_PROC_ID");
				}
			}
		}
		return PROC_ID;
	}

	public static Long newProcessInstanceId() {
		try {
			return getProcessInstanceIdCounter().incrementAndGet().get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static StrongCounter getProcessInstanceIdCounter() {
		if (PROC_INST_ID == null) {
			synchronized (InfinispanIdGen.class) {
				if (PROC_INST_ID == null) {
					PROC_INST_ID = InfinispanDatabase.getCounter("COUNTER_PROC_INST_ID");
				}
			}
		}
		return PROC_INST_ID;
	}

	public static Long newScopeId() {
		try {
			return getScopeIdCounter().incrementAndGet().get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static StrongCounter getScopeIdCounter() {
		if (SCOPE_ID == null) {
			synchronized (InfinispanIdGen.class) {
				if (SCOPE_ID == null) {
					SCOPE_ID = InfinispanDatabase.getCounter("COUNTER_SCOPE_ID");
				}
			}
		}
		return SCOPE_ID;
	}

	public static Long newCorrelationSetId() {
		try {
			return getCorrelationSetIdCounter().incrementAndGet().get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static StrongCounter getCorrelationSetIdCounter() {
		if (CSET_ID == null) {
			synchronized (InfinispanIdGen.class) {
				if (CSET_ID == null) {
					CSET_ID = InfinispanDatabase.getCounter("COUNTER_CSET_ID");
				}
			}
		}
		return CSET_ID;
	}
}
