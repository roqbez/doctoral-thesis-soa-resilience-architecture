package br.ufsc.gsigma.services.resilience.support;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.gsigma.services.resilience.impl.ResiliencePlanning;

public class PlanningAction implements Callable<Object>, Runnable, Serializable {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PlanningAction.class);

	private static final long serialVersionUID = 1L;

	private long startTime;

	private long delay;

	private SOAApplicationReconfiguration reconfiguration;

	private ExecutionContext executionContext;

	private boolean running;

	private String nodeId;

	public PlanningAction(String nodeId, SOAApplicationReconfiguration reconfiguration, long startTime) {
		this.nodeId = nodeId;
		this.reconfiguration = reconfiguration;
		this.startTime = startTime;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	@Override
	public void run() {
		try {
			call();
		} catch (Exception e) {
			throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
		}
	}

	@Override
	public Object call() throws Exception {
		try {
			if (executionContext != null) {
				executionContext.set();
			}

			ResiliencePlanning.getInstance().submitPlanningAction(this);

			return null;

		} finally {
			if (executionContext != null) {
				executionContext.remove();
			}
		}
	}

	public long getStartTime() {
		return startTime;
	}

	public SOAApplicationReconfiguration getReconfiguration() {
		return reconfiguration;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public void setExecutionContext(ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

}