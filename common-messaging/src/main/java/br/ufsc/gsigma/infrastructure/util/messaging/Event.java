package br.ufsc.gsigma.infrastructure.util.messaging;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;

public abstract class Event implements Serializable {

	private static final long serialVersionUID = 1L;

	private final long timestamp;

	private final long ttl;

	private ExecutionContext executionContext;

	public Event() {
		this(ExecutionContext.get(), MessagingConstants.DEFAULT_MESSAGE_TTL);
	}

	public Event(long ttl) {
		this(ExecutionContext.get(), ttl);
	}

	public Event(ExecutionContext executionContext) {
		this(executionContext, MessagingConstants.DEFAULT_MESSAGE_TTL);
	}

	public Event(ExecutionContext executionContext, long ttl) {
		this.timestamp = System.currentTimeMillis();
		this.executionContext = executionContext;
		this.ttl = ttl;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getTTL() {
		return ttl;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, NO_HASHCODE_STYLE);
	}

	private static final org.apache.commons.lang.builder.ToStringStyle NO_HASHCODE_STYLE = new ToStringStyle() {
		private static final long serialVersionUID = 1L;
		{
			setUseShortClassName(true);
			setUseIdentityHashCode(false);
		}
	};

}
