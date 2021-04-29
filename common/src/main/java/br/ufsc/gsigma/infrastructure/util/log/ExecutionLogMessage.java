package br.ufsc.gsigma.infrastructure.util.log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import br.ufsc.gsigma.infrastructure.util.JsonEncodeUtil;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionAttributes;
import br.ufsc.gsigma.infrastructure.ws.context.ExecutionContext;
import br.ufsc.services.log.util.LogAttachment;
import br.ufsc.services.log.util.LogMessage;

public class ExecutionLogMessage extends LogMessage {

	private static final long serialVersionUID = 1L;

	private Set<String> transientAttributesKeys = new HashSet<String>();

	public ExecutionLogMessage() {
		super();
		fillLogAttributes();
		setAppendAttributesInToString(false);
	}

	public ExecutionLogMessage(String message, LogAttachment... attachments) {
		super(message, attachments);
		fillLogAttributes();
		setAppendAttributesInToString(false);
	}

	public ExecutionLogMessage(String appName, String messageId, String message, String[] keyValues) {
		super(appName, messageId, message, keyValues);
		fillLogAttributes();
		setAppendAttributesInToString(false);
	}

	public ExecutionLogMessage(String appName, String messageId, String message) {
		super(appName, messageId, message);
		fillLogAttributes();
		setAppendAttributesInToString(false);
	}

	public ExecutionLogMessage(String messageId, String message, String[] keyValues) {
		super(messageId, message, keyValues);
		fillLogAttributes();
		setAppendAttributesInToString(false);
	}

	public ExecutionLogMessage(String messageId, String message) {
		super(messageId, message);
		fillLogAttributes();
		setAppendAttributesInToString(false);
	}

	public ExecutionLogMessage(String message, String[] keyValues) {
		super(message, keyValues);
		fillLogAttributes();
		setAppendAttributesInToString(false);
	}

	public ExecutionLogMessage(String message) {
		super(message);
		fillLogAttributes();
		setAppendAttributesInToString(false);
	}

	@Override
	public ExecutionLogMessage addAttribute(String key, Object value) {
		return (ExecutionLogMessage) super.addAttribute(key, value);
	}

	@Override
	public ExecutionLogMessage addAttribute(String key, String value) {
		return (ExecutionLogMessage) super.addAttribute(key, value);
	}

	public ExecutionLogMessage addTransientAttribute(String key, String value) {
		if (checkNotEmpty(value)) {
			super.addAttribute(key, value);
			transientAttributesKeys.add(key);
		}
		return this;
	}

	public ExecutionLogMessage addTransientAttribute(String key, Object value) {
		if (checkNotEmpty(value)) {
			super.addAttribute(key, value);
			transientAttributesKeys.add(key);
		}
		return this;
	}

	public LogMessage clone(String message) {

		ExecutionLogMessage clone = new ExecutionLogMessage(message);

		clone.setMessageId(getMessageId());
		clone.setAppName(getAppName());

		Map<String, Object> attributes = getAttributes();

		if (attributes != null)
			clone.getAttributes().putAll(attributes);

		List<LogAttachment> attachments = getAttachments();

		if (attachments != null)
			clone.getAttachments().addAll(attachments);

		clone.transientAttributesKeys.addAll(transientAttributesKeys);

		return clone;
	}

	protected void fillLogAttributes() {

		ExecutionContext executionContext = ExecutionContext.get();

		if (executionContext != null) {

			Map<String, String> attributes = executionContext.getAttributes();

			if (attributes != null) {

				String attrsJson = attributes.get(ExecutionAttributes.ATT_LOG_ATTRS);
				if (attrsJson != null) {
					Map<String, Object> logAttributes = JsonEncodeUtil.decode(attrsJson);

					for (Entry<String, Object> e : logAttributes.entrySet()) {
						String key = e.getKey();
						Object value = e.getValue();
						if (shouldIncludeLogAttribute(key, value)) {
							getAttributes().put(key, value);
						}
					}
				}
			}

			if (executionContext.getExecutionId() != null)
				getAttributes().put(LogConstants.EXECUTION_ID, executionContext.getExecutionId());
		}
	}

	protected boolean shouldIncludeLogAttribute(String key, Object value) {
		return true;
	}

	@Override
	protected void beforeLogging() {

		super.beforeLogging();

		try {
			getAttributes().put(LogConstants.HOSTNAME, InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e1) {
		}
		getAttributes().put(LogConstants.EVENT_TIMESTAMP, new Date());

		ExecutionContext executionContext = ExecutionContext.get(true);

		try {
			Map<String, Object> attrs = new TreeMap<String, Object>(this.getAttributes());

			for (String key : transientAttributesKeys) {
				attrs.remove(key);
			}
			if (!attrs.isEmpty())
				executionContext.addAttribute(ExecutionAttributes.ATT_LOG_ATTRS, JsonEncodeUtil.encode(attrs));
		} catch (Throwable e) {
		}
	}

	public static void removeLogMessageAttributes(ExecutionContext executionContext) {
		executionContext.removeAttribute(ExecutionAttributes.ATT_LOG_ATTRS);
	}

	public static void addLogMessageAttribute(ExecutionContext executionContext, String key, Object value) {

		Map<String, Object> attrs = getLogMessageAttributes(executionContext);
		attrs.put(key, value);

		if (!attrs.isEmpty())
			executionContext.addAttribute(ExecutionAttributes.ATT_LOG_ATTRS, JsonEncodeUtil.encode(attrs));

	}

	public static Map<String, Object> getLogMessageAttributes() {
		return getLogMessageAttributes(ExecutionContext.get());
	}

	public static Map<String, Object> getLogMessageAttributes(ExecutionContext executionContext) {

		Map<String, Object> attrs = new TreeMap<String, Object>();

		if (executionContext != null) {

			if (attrs != null) {
				String attrsJson = executionContext.getAttribute(ExecutionAttributes.ATT_LOG_ATTRS);
				if (attrsJson != null)
					attrs.putAll(JsonEncodeUtil.decode(attrsJson));
			}
		}
		return attrs;
	}

	public static void main(String[] args) {

		String str = "H4sIAAAAAAAAAL1VW2/TMBT+K5OfWzfX5iKGNHUDJjY0dkMIocl1TluL1A62M21M++8cp2mWNsALiKfE5/r5nPMdP5GP6orOlDRWMyGtIfkXcgF6ofSaSQ70EkyFWrgWazh4dXgQep5HPTIiM2UsPXkAXluhpDvtqI/umSjZXJTCPtL+4eD14YFHEzTpp7leaVUvV1Vtnd6PMcbXEWFVVQrOXILTguTEC5I08uOCQZhkmZ/x1EviNFjEUz6dZyGL0jAugEcYHLbIGkcLxvooFfJefYPiCvS94HDHOFe1tEIuZ7Wxag36jtV2pbT4ARfscQ3SkvyJzIUsnI2SC7GsdYPnFrTBD8mDETGbcCeyqBTW8FQulHNrxe/hERHURSHyqp6XwqxAB14uVQHB2InHKB23xmZcbfLetd9KK5SaHlQ+gNqaujhdGHfbFvc1rKuS2f8IxB+3qcmgODeXZwhiZW2VTyY9wLQYB54/9cIgimi9MJzOde77OE6TrYkzn+yimQw7OBl0sMMwK5kxYtFOlKvFS8C/CvyBrZEmjMPO1ahiRhjawf8XmW5sQyKS+zQK4jhAQgRhGqfJiHxXV7esrMG42TuVFpbace+YWdadSO7RMB6RK846cp7haEiOqqlPvT8Q1/lmiYfzfsQ5soA/0rcgAekAxYnWSpM8bCL0ed0FjxrVJZRiG/tcSbvCrNN9n2vNpGHc9chtHZJnje/JA4fKCd8xWZRYLvqmlo0V28ILdjMcM5SGgXMebivUZBmdelEWpFGcpNMs2q1Lf/ORPIkHMLuVhQibSxy5fopt6XoHhy0KEQXDIXHK7Q/20XOe/byD64eNieuhVhXWu7XbF7gk02j02+2NuZJBrbuStBbZAE3vln6yBbIZrR5SVu7MmJ/sduIzMByPuLnIpZrjnEusDn35/UX3zlHMlvCJaUzjwib72PbApxH1np87qpyxOZQbku9sRdjM6oKhGK1bGp7iC+gq4p4Lv5M6YmOIloYXGyF5/gmpTwQwNgcAAA==";

		System.out.println(JsonEncodeUtil.decode(str));
	}

}
