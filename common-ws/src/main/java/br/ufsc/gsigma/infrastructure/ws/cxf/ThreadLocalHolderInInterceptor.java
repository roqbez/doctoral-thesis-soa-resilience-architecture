package br.ufsc.gsigma.infrastructure.ws.cxf;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import br.ufsc.gsigma.infrastructure.util.thread.ThreadLocalHolder;

public class ThreadLocalHolderInInterceptor extends AbstractPhaseInterceptor<Message> {

	public ThreadLocalHolderInInterceptor() {
		super(Phase.RECEIVE);
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		ThreadLocalHolder.initThreadLocal();
	}
}