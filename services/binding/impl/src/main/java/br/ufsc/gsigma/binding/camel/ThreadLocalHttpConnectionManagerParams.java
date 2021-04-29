package br.ufsc.gsigma.binding.camel;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public class ThreadLocalHttpConnectionManagerParams extends HttpConnectionManagerParams {

	private static final long serialVersionUID = 1L;

	private ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<Map<String, Object>>();

	public void setThreadLocalParameter(String name, Object value) {
		Map<String, Object> threadLocalParameters = threadLocal.get();
		if (threadLocalParameters == null) {
			synchronized (this) {
				threadLocalParameters = threadLocal.get();
				if (threadLocalParameters == null) {
					threadLocalParameters = new HashMap<String, Object>();
					threadLocal.set(threadLocalParameters);
				}
			}
		}
		threadLocalParameters.put(name, value);
	}

	public void resetThreadLocal() {
		Map<String, Object> threadLocalParameters = threadLocal.get();
		if (threadLocalParameters != null)
			threadLocalParameters.clear();
		threadLocal.remove();
	}

	@Override
	public synchronized Object getParameter(String name) {

		Map<String, Object> m = threadLocal.get();

		if (m != null) {
			Object obj = m.get(name);
			if (obj != null)
				return obj;
		}

		return super.getParameter(name);
	}

}
