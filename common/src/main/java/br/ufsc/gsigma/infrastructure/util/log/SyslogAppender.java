package br.ufsc.gsigma.infrastructure.util.log;

import br.ufsc.services.core.util.json.ISO8601DateFormat;
import br.ufsc.services.core.util.json.JsonUtil;
import br.ufsc.services.log.util.LogAttributesConstants;
import br.ufsc.services.log.util.LogMessage;
import br.ufsc.services.log.util.SyslogUtil;

public class SyslogAppender extends br.ufsc.services.log.util.Log4jSyslogAppender {

	static {
		// Dates with millis and timezone
		JsonUtil.getObjectMapper().setDateFormat(new ISO8601DateFormat(true, true));

		// Fix hostname (Docker Stacks)
		try {
			String localHostname = SyslogUtil.getLocalHostname();
			SyslogUtil.setLocalHostname(localHostname.replaceAll("_", "-"));
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	@Override
	protected Object convertMessage(Object message) {

		if (message instanceof String) {
			return new ExecutionLogMessage((String) message);

		} else if (message instanceof LogMessage && !(message instanceof ExecutionLogMessage)) {

			LogMessage logMessage = (LogMessage) message;

			ExecutionLogMessage executionLogMessage = new ExecutionLogMessage();
			executionLogMessage.setAppName(logMessage.getAppName());
			executionLogMessage.setMessageId(logMessage.getMessageId());
			executionLogMessage.setMessage(logMessage.getMessage());
			executionLogMessage.getAttributes().putAll(logMessage.getAttributes());
			executionLogMessage.getAttachments().addAll(logMessage.getAttachments());

			return executionLogMessage;
		}

		return super.convertMessage(message);
	}

	@Override
	protected void handleThrowable(LogMessage message, Throwable throwable) {

		if (message instanceof ExecutionLogMessage)
			((ExecutionLogMessage) message).addTransientAttribute(LogAttributesConstants.EXCEPTION_MESSAGE, throwable.getMessage());
	}
}
