package br.ufsc.gsigma.binding.camel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataSource;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.cxf.attachment.AttachmentUtil;
import org.apache.cxf.helpers.LoadingByteArrayOutputStream;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxSource;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.staxutils.W3CDOMStreamReader;

public class SoapMessageModeInInterceptor extends AbstractPhaseInterceptor<Message> {

	private final Class<?> type = SOAPMessage.class;

	private Class<?> soapMsgClass;

	public SoapMessageModeInInterceptor() {
		super(Phase.POST_LOGICAL);
		try {
			soapMsgClass = Class.forName("javax.xml.soap.SOAPMessage");
		} catch (Throwable t) {
			soapMsgClass = null;
		}
	}

	public void handleMessage(Message message) throws Fault {

		Object o = message.getContent(soapMsgClass);
		if (o != null) {
			doFromSoapMessage(message, o);
		} else if (DataSource.class.isAssignableFrom(type)) {
			doDataSource(message);
		}

	}

	private void doDataSource(final Message message) {
		MessageContentsList list = (MessageContentsList) message.getContent(List.class);
		// reconstitute all the parts into a Mime data source
		if (message.getAttachments() != null && !message.getAttachments().isEmpty() && list != null && !list.isEmpty() && list.get(0) instanceof DataSource) {
			list.set(0, new MultiPartDataSource(message, (DataSource) list.get(0)));
		}
	}

	private void doFromSoapMessage(Message message, Object sm) {
		SOAPMessage m = (SOAPMessage) sm;
		MessageContentsList list = (MessageContentsList) message.getContent(List.class);
		if (list == null) {
			list = new MessageContentsList();
			message.setContent(List.class, list);
		}
		Object o = m;

		if (StreamSource.class.isAssignableFrom(type)) {
			try {
				try (CachedOutputStream out = new CachedOutputStream()) {
					XMLStreamWriter xsw = StaxUtils.createXMLStreamWriter(out);
					StaxUtils.copy(new DOMSource(m.getSOAPPart()), xsw);
					xsw.close();
					o = new StreamSource(out.getInputStream());
				}
			} catch (Exception e) {
				throw new Fault(e);
			}
		} else if (SAXSource.class.isAssignableFrom(type)) {
			o = new StaxSource(new W3CDOMStreamReader(m.getSOAPPart()));
		} else if (Source.class.isAssignableFrom(type)) {
			o = new DOMSource(m.getSOAPPart());
		}
		list.set(0, o);
	}

	private static class MultiPartDataSource implements DataSource {
		final Iterator<Attachment> atts;
		final String contentType;
		final String boundary;
		final String start;

		final LoadingByteArrayOutputStream bout = new LoadingByteArrayOutputStream();
		Writer writer;

		DataSource rootPart;
		InputStream current;
		boolean writingHeaders;
		Attachment att;

		MultiPartDataSource(Message message, DataSource root) {
			atts = message.getAttachments().iterator();
			String s = (String) message.get(Message.CONTENT_TYPE);
			boundary = findBoundary(s);
			start = findStart(s);
			if (!s.contains(boundary)) {
				s += "; boundary=\"" + boundary + "\"";
			}
			contentType = s;
			rootPart = root;
			try {
				writer = new OutputStreamWriter(bout, "ASCII");
				writer.append("Content-Type: ").append(contentType).append("\r\n\r\n");
				writer.flush();
				current = bout.createInputStream();
			} catch (Exception e) {
				// nothing
			}
		}

		public String getContentType() {
			return contentType;
		}

		public InputStream getInputStream() throws IOException {
			return new InputStream() {
				public int read() throws IOException {
					int i = current.read();
					if (i == -1) {
						nextCurrent();
						i = current.read();
					}
					return i;
				}
			};
		}

		private void nextCurrent() throws IOException {
			if (rootPart != null) {
				if (writingHeaders) {
					writingHeaders = false;
					current = rootPart.getInputStream();
					rootPart = null;
				} else {
					writingHeaders = true;
					bout.reset();
					writer.append("--").append(boundary).append("\r\n");
					writer.append("Content-Type: ").append(rootPart.getContentType()).append("\r\n");
					writer.append("Content-ID: <").append(start).append(">\r\n\r\n");
					writer.flush();
					current = bout.createInputStream();
				}
			} else {
				if (writingHeaders) {
					writingHeaders = false;
					current = att.getDataHandler().getInputStream();
				} else if (atts.hasNext()) {
					att = atts.next();
					writingHeaders = true;
					bout.reset();
					writer.append("\r\n");
					writer.append("--").append(boundary).append("\r\n");
					Iterator<String> heads = att.getHeaderNames();
					while (heads.hasNext()) {
						String s = heads.next();
						writer.append(s).append(": ").append(att.getHeader(s)).append("\r\n");
					}
					writer.append("\r\n");
					writer.flush();
					current = bout.createInputStream();
				}
			}
		}

		public String getName() {
			return null;
		}

		public OutputStream getOutputStream() throws IOException {
			return null;
		}

		private String findStart(String ct) {
			int idx = ct.indexOf("start=");
			if (idx == -1) {
				return "root";
			}
			ct = ct.substring(idx + 6);
			if (ct.charAt(0) == '"') {
				ct = ct.substring(1);
				idx = ct.indexOf('"');
				return ct.substring(0, idx);
			}
			idx = ct.indexOf(';');
			if (idx == -1) {
				return ct;
			}
			return ct.substring(0, idx);
		}

		private String findBoundary(String ct) {
			int idx = ct.indexOf("boundary=");
			if (idx == -1) {
				return AttachmentUtil.getUniqueBoundaryValue();
			}
			ct = ct.substring(idx + 9);
			if (ct.charAt(0) == '"') {
				ct = ct.substring(1);
				idx = ct.indexOf('"');
				return ct.substring(0, idx);
			}
			idx = ct.indexOf(';');
			if (idx == -1) {
				return ct;
			}
			return ct.substring(0, idx);
		}
	}
}
