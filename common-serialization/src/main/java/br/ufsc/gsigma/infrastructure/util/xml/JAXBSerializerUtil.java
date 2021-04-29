package br.ufsc.gsigma.infrastructure.util.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class JAXBSerializerUtil {

	private static Map<Class<?>, JAXBContext> jaxbCache = new HashMap<Class<?>, JAXBContext>();

	public static <T> T read(Class<T> clazz, File file) throws Exception {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return read(clazz, in);
		} finally {
			if (in != null)
				in.close();
		}
	}

	public static <T> T read(Class<T> clazz, InputStream in) throws Exception {
		return readInternal(clazz, in);
	}

	public static <T> T read(Class<T> clazz, Node node) throws Exception {
		return readInternal(clazz, node);
	}

	@SuppressWarnings("unchecked")
	private static <T> T readInternal(Class<T> clazz, Object input) throws Exception {

		JAXBContext context = getJAXBContext(clazz);

		Unmarshaller m = context.createUnmarshaller();
		if (input instanceof InputStream)
			return (T) m.unmarshal((InputStream) input);
		else if (input instanceof Node)
			return (T) m.unmarshal((Node) input);
		else
			return null;
	}

	private static <T> JAXBContext getJAXBContext(Class<T> clazz) throws JAXBException {

		JAXBContext ctx = jaxbCache.get(clazz);

		if (ctx == null) {
			synchronized (jaxbCache) {
				ctx = jaxbCache.get(clazz);

				if (ctx == null) {
					ctx = JAXBContext.newInstance(clazz);
					jaxbCache.put(clazz, ctx);
				}
			}
		}

		return ctx;
	}

	public static void write(Object object, OutputStream out) throws Exception {
		write(object, out, true);
	}

	public static void write(Object object, OutputStream out, boolean formated) throws Exception {

		JAXBContext context = getJAXBContext(object.getClass());

		Marshaller m = context.createMarshaller();

		if (formated) {
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		}

		m.marshal(object, out);
	}

	public static void write(Object object, Node node) throws Exception {

		JAXBContext context = getJAXBContext(object.getClass());

		Marshaller m = context.createMarshaller();

		m.marshal(object, node);
	}

	public static Element toElement(Object object) throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(object, baos, false);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
		Document doc = db.parse(bis);
		
		return doc.getDocumentElement();
	}

}
