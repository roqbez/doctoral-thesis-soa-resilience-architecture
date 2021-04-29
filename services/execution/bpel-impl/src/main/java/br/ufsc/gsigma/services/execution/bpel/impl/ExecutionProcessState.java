package br.ufsc.gsigma.services.execution.bpel.impl;

import org.apache.ode.bpel.common.ProcessState;

public class ExecutionProcessState {

	public static final short STATE_NEW = ProcessState.STATE_NEW;

	public static final short STATE_READY = ProcessState.STATE_READY;

	public static final short STATE_ACTIVE = ProcessState.STATE_ACTIVE;

	public static final short STATE_COMPLETED_OK = ProcessState.STATE_COMPLETED_OK;

	public static final short STATE_COMPLETED_WITH_FAULT = ProcessState.STATE_COMPLETED_WITH_FAULT;

	public static final short STATE_SUSPENDED = ProcessState.STATE_SUSPENDED;

	public static final short STATE_TERMINATED = ProcessState.STATE_TERMINATED;

	public static final short STATE_COMPLETED_WITH_QOS_VIOLATION = 70;

	public static String stateToString(short state) {
		switch (state) {

		case STATE_NEW:
			return "New";

		case STATE_READY:
			return "Ready";

		case STATE_ACTIVE:
			return "Active";

		case STATE_COMPLETED_OK:
			return "Completed Ok";

		case STATE_COMPLETED_WITH_FAULT:
			return "Completed Fault";

		case STATE_SUSPENDED:
			return "Suspended";

		case STATE_TERMINATED:
			return "Terminated";

		case STATE_COMPLETED_WITH_QOS_VIOLATION:
			return "Completed with QoS Violation";

		default:
			throw new IllegalStateException("unknown state: " + state);
		}
	}

}
