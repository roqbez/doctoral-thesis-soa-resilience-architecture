package br.ufsc.gsigma.services.execution.bpel.util;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class TxLog {

	public static final Log txLog = LogFactory.getLog("TxLog");

	private static final ThreadLocal<Boolean> stopLogging = new ThreadLocal<Boolean>();

	static {
		stopLogging.set(false);
	}

	public static void stopLogging() {
		stopLogging.set(true);
	}

	public static void restoreLogging() {
		stopLogging.set(false);
	}

	public static void debug(Object msg) {
		if (!BooleanUtils.isNotTrue(stopLogging.get()))
			txLog.debug(msg);
	}

	public static void info(Object msg) {
		if (!BooleanUtils.isNotTrue(stopLogging.get()))
			txLog.info(msg);
	}

}
