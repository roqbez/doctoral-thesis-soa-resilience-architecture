package br.ufsc.gsigma.catalog.plugin;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.xml.sax.SAXException;

public class Activator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "br.ufsc.gsigma.catalog.plugin";
	private static Activator plugin;
	private XMLParserPool parserPool;

	public Activator() {
		plugin = this;
	}

	public void start(BundleContext paramBundleContext) throws Exception {
		super.start(paramBundleContext);
	}

	public void stop(BundleContext paramBundleContext) throws Exception {
		plugin = null;
		super.stop(paramBundleContext);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static void log(IStatus paramIStatus) {
		getDefault().getLog().log(paramIStatus);
	}

	public static void log(String paramString, Exception paramException) {
		log(new Status(4, "br.ufsc.gsigma.wsbpm.ublimporter", 0, paramString, paramException));
	}

	public final synchronized SAXParser obtainSAXParser() throws ParserConfigurationException, SAXException {
		if (this.parserPool == null) {
			this.parserPool = new XMLParserPool();
		}

		return this.parserPool.obtainSAXParser();
	}

	public final synchronized void releaseSAXParser(SAXParser paramSAXParser) {
		if (this.parserPool != null) {
			paramSAXParser.reset();
			this.parserPool.releaseSAXParser(paramSAXParser);
		}
	}

	public final synchronized DocumentBuilder obtainDOMParser() throws ParserConfigurationException {
		if (this.parserPool == null) {
			this.parserPool = new XMLParserPool();
		}

		return this.parserPool.obtainDOMParser();
	}

	public final synchronized void releaseDOMParser(DocumentBuilder paramDocumentBuilder) {
		if (this.parserPool != null) {
			paramDocumentBuilder.reset();
			this.parserPool.releaseDOMParser(paramDocumentBuilder);
		}
	}

	class XMLParserPool {
		private DocumentBuilderFactory fDOMFactory;
		private ArrayList<DocumentBuilder> fDOMPool;
		private SAXParserFactory fSAXFactory;
		private ArrayList<SAXParser> fSAXPool;

		private void initSAX() {
			if (this.fSAXFactory == null) {
				this.fSAXFactory = SAXParserFactory.newInstance();
				this.fSAXPool = new ArrayList<SAXParser>();
			}
		}

		public SAXParser obtainSAXParser() throws ParserConfigurationException, SAXException {
			initSAX();

			int i = this.fSAXPool.size();
			SAXParser localSAXParser;
			if (i > 0)
				localSAXParser = (SAXParser) this.fSAXPool.remove(i - 1);
			else {
				localSAXParser = this.fSAXFactory.newSAXParser();
			}
			return localSAXParser;
		}

		public void releaseSAXParser(SAXParser paramSAXParser) {
			this.fSAXPool.add(paramSAXParser);
		}

		private void initDOM() {
			if (this.fDOMFactory == null) {
				this.fDOMFactory = DocumentBuilderFactory.newInstance();
				this.fDOMPool = new ArrayList<DocumentBuilder>();
			}
		}

		public DocumentBuilder obtainDOMParser() throws ParserConfigurationException {
			initDOM();

			int i = this.fDOMPool.size();
			DocumentBuilder localDocumentBuilder;
			if (i > 0)
				localDocumentBuilder = (DocumentBuilder) this.fDOMPool.remove(i - 1);
			else {
				localDocumentBuilder = this.fDOMFactory.newDocumentBuilder();
			}
			return localDocumentBuilder;
		}

		public void releaseDOMParser(DocumentBuilder paramDocumentBuilder) {
			this.fDOMPool.add(paramDocumentBuilder);
		}
	}
}