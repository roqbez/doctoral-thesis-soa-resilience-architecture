package br.ufsc.gsigma.infrastructure.util.thread;

import java.util.HashMap;
import java.util.Map;

public abstract class ThreadLocalHolder {

	private static final ThreadLocal<Object> threadLocal = new ThreadLocal<Object>();

	public static void initThreadLocal() {
		getThreadLocalMap();
	}

	public static boolean isInitalized() {
		return threadLocal.get() != null;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getThreadLocalMap() {

		Map<String, Object> m = (Map<String, Object>) threadLocal.get();

		if (m == null) {
			m = new HashMap<String, Object>();
			threadLocal.set(m);
		}
		return m;
	}

	@SuppressWarnings("unchecked")
	public static void clearThreadLocal() {

		Map<String, Object> m = (Map<String, Object>) threadLocal.get();

		if (m != null)
			m.clear();

		threadLocal.remove();
	}

}
