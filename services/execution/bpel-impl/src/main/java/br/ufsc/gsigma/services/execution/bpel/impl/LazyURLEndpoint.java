package br.ufsc.gsigma.services.execution.bpel.impl;

import java.util.Map;

import org.apache.ode.bpel.epr.URLEndpoint;
import org.w3c.dom.Document;

public class LazyURLEndpoint extends URLEndpoint {

	@Override
	public Document toXML() {

		String url = loadUrl();

		if (url != null)
			this.setUrl(url);

		return super.toXML();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map toMap() {

		String url = loadUrl();

		if (url != null)
			this.setUrl(url);

		return super.toMap();
	}

	@Override
	public String getUrl() {

		String url = loadUrl();

		if (url != null)
			this.setUrl(url);

		return super.getUrl();
	}

	protected String loadUrl() {
		return null;
	}

	@Override
	public String toString() {
		return getUrl();
	}

}
