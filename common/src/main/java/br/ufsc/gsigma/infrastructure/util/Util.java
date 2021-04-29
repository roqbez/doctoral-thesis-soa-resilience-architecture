package br.ufsc.gsigma.infrastructure.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.google.common.io.Files;

public abstract class Util {

	public static File getDirectoryFromClassLoaderResources(String baseDirectory) {
		return getDirectoryFromClassLoaderResources(Util.class.getClassLoader(), baseDirectory);
	}

	public static File getDirectoryFromClassLoaderResources(ClassLoader classLoader, String baseDirectory) {

		try {

			File baseFile = new File(Files.createTempDir(), baseDirectory);

			baseFile.deleteOnExit();

			for (Entry<String, Resource> e : Util.getClassLoaderResources(classLoader, baseDirectory).entrySet()) {

				String basePath = Util.getResourceSubPath(baseDirectory, e.getKey());

				try (InputStream in = e.getValue().getInputStream()) {

					File outFile = new File(baseFile, basePath);
					File p = outFile.getParentFile();

					if (p != null)
						p.mkdirs();

					if (!outFile.isDirectory()) {
						int read = 0;
						byte[] buffer = new byte[8192];

						try (OutputStream out = new FileOutputStream(outFile)) {
							while (-1 != (read = in.read(buffer))) {
								out.write(buffer, 0, read);
							}
						}
					}
				}
			}
			return baseFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static Map<String, Resource> getClassLoaderResources(ClassLoader classLoader, String baseDirectory) throws IOException {

		Map<String, Resource> result = new LinkedHashMap<String, Resource>();

		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(classLoader);

		for (Resource r : resourcePatternResolver.getResources(baseDirectory + "/**")) {

			if (r.isReadable()) {

				String path = getResourcePath(r);

				result.put(path, r);

			}
		}
		return result;
	}

	public static String getResourcePath(Resource r) throws IOException {
		URI uri = r.getURI();

		String path;

		if ("jar".equals(uri.getScheme())) {
			String p = uri.getRawSchemeSpecificPart();
			path = p.substring(p.indexOf('!') + 1);
		} else {
			path = uri.getPath();
		}
		return path;
	}

	public static String getResourceSubPath(String baseDirectory, String path) {
		return path.substring(path.indexOf(baseDirectory) + baseDirectory.length());
	}

	public static String readStringFromClassLoaderResource(String url, String encoding) throws IOException {
		try (InputStream in = Util.class.getResourceAsStream(url)) {
			return readStreamAsString(in, encoding);
		}
	}

	public static void writeStringToFile(String file, String content) throws IOException {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			out.write(content);
		}
	}

	public static String readStreamAsString(InputStream is) throws java.io.IOException {
		return readStreamAsString(is, "UTF-8");
	}

	public static String readStreamAsString(InputStream is, String encoding) throws java.io.IOException {
		try (InputStream in = is) {
			return IOUtils.toString(in, encoding);
		}
	}

	public static String suffixHost(String host, int i) {

		int idx = host.indexOf('.');

		if (idx == -1)
			return host + i;
		else
			return host.substring(0, idx) + i + host.substring(idx);
	}

	public static void sleep(long millis) {
		if (millis > 0)
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
			}
	}

}
