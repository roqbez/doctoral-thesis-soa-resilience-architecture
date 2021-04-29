package br.ufsc.gsigma.infrastructure.ws.cxf;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import br.ufsc.gsigma.infrastructure.util.thread.ThreadLocalHolder;

public class ThreadLocalHolderOutInterceptor extends AbstractPhaseInterceptor<Message> {

	public ThreadLocalHolderOutInterceptor() {
		super(Phase.SEND);
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		ThreadLocalHolder.clearThreadLocal();
	}
}