package br.ufsc.gsigma.infrastructure.ws.cxf;

import javax.xml.stream.XMLInputFactory;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import com.ctc.wstx.api.WstxInputProperties;

public class ConfigurableWstxInputInInterceptor extends AbstractPhaseInterceptor<Message> {

	private static final XMLInputFactory XML_INPUT_FACTORY;

	static {
		XML_INPUT_FACTORY = org.apache.cxf.staxutils.StaxUtils.createXMLInputFactory(true);
		XML_INPUT_FACTORY.setProperty(WstxInputProperties.P_INPUT_BUFFER_LENGTH, 4000 * 16);
	}

	public ConfigurableWstxInputInInterceptor() {
		super(Phase.POST_STREAM);
		addBefore(StaxInInterceptor.class.getName());

	}

	@Override
	public void handleMessage(Message message) throws Fault {
		message.put(XMLInputFactory.class.getName(), XML_INPUT_FACTORY);
	}

}
