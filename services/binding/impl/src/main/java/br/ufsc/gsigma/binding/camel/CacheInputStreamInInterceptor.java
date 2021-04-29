package br.ufsc.gsigma.binding.camel;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.io.DelegatingInputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class CacheInputStreamInInterceptor extends AbstractPhaseInterceptor<Message> {

	private static final Logger LOG = LogUtils.getL7dLogger(CacheInputStreamInInterceptor.class);

	public CacheInputStreamInInterceptor() {
		super(Phase.POST_STREAM);
		addBefore(StaxInInterceptor.class.getName());
	}

	@Override
	public void handleMessage(Message message) throws Fault {

		DelegatingInputStream is = message.getContent(DelegatingInputStream.class);

		if (is != null) {
			is.cacheInput();
			message.getInterceptorChain().add(CacheInputStreamInEndingInterceptor.INSTANCE);
		}
	}

	private static class CacheInputStreamInEndingInterceptor extends AbstractPhaseInterceptor<Message> {

		private static final CacheInputStreamInEndingInterceptor INSTANCE = new CacheInputStreamInEndingInterceptor();

		public CacheInputStreamInEndingInterceptor() {
			super(Phase.POST_STREAM);
			addAfter(StaxInInterceptor.class.getName());
		}

		@Override
		public void handleMessage(Message message) throws Fault {

			DelegatingInputStream is = message.getContent(DelegatingInputStream.class);

			if (is != null) {
				try {
					is.reset();
				} catch (IOException e) {
					throw new Fault(e.getMessage(), LOG);
				}
			}
		}
	}
}
