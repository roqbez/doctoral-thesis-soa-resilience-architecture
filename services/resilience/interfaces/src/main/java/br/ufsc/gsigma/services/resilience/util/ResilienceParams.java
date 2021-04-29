package br.ufsc.gsigma.services.resilience.util;

public interface ResilienceParams {

	public static final int DEFAULT_HTTP_READ_TIMEOUT = 3000;
	public static final int DEFAULT_HTTP_CONNECTION_TIMEOUT = 500;

	public static final int DEFAULT_SERVICE_UNAVAILABLE_CHECK_DIFF_MILLIS = 900;

	public static final int DEFAULT_PLANNING_SCHEDULER_DELAY = 10;

	public static final int SERVICE_CHECK_ATTEMPT_DELAY = 500;

	public static final int SERVICE_CHECK_MAX_ATTEMPTS = 100;

	public static final int MONITOR_NUM_THREADS = 8;

	public static final int ANALYSIS_NUM_THREADS = 8;

	public static final int PLANNING_NUM_THREADS = 8;

	public static final int EXECUTION_NUM_THREADS = 8;

	public static final int SERVICE_MONITOR_CHECK_INTERVAL = 500;

	public static final long WAIT_FOR_NEW_BINDING_CONFIGURATION_TIMEOUT = 2000L;
}
