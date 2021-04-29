package br.ufsc.gsigma.infrastructure.util.xml;

import java.io.InputStream;
import java.io.OutputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public abstract class SimpleXMLSerializerUtil {

	public static <T> T read(Class<T> clazz, InputStream in) throws Exception {
		Serializer serializer = createPersister();
		return serializer.read(clazz, in);
	}

	public static void write(Object object, OutputStream out) throws Exception {
		Serializer serializer = createPersister();
		serializer.write(object, out);
	}

	private static Persister createPersister() {
		return new Persister(new CyclicNoClassAttributeSimpleXMLPersisterStrategy());
	}

}
