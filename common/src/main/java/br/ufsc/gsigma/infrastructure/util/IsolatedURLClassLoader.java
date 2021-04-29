package br.ufsc.gsigma.infrastructure.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.StringTokenizer;

public class IsolatedURLClassLoader extends URLClassLoader {

	private static final URL[] urls;

	private URLClassLoader childClassLoader;

	static {
		StringTokenizer stk = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));

		urls = new URL[stk.countTokens()];

		int i = 0;

		while (stk.hasMoreTokens())
			try {
				urls[i++] = new File(stk.nextToken()).toURI().toURL();
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
	}

	public IsolatedURLClassLoader() {
		super(urls, ClassLoader.getSystemClassLoader().getParent());
	}

	public IsolatedURLClassLoader(URL[] childUrls, boolean inheritBootstrapClassPath) {
		super(inheritBootstrapClassPath ? urls : childUrls, ClassLoader.getSystemClassLoader().getParent());

		if (inheritBootstrapClassPath)
			childClassLoader = new URLClassLoader(childUrls, ClassLoader.getSystemClassLoader().getParent());
	}

	public IsolatedURLClassLoader(URL[] baseClassLoader) {
		this(baseClassLoader, null);
	}

	public IsolatedURLClassLoader(URL[] baseClassLoader, URL[] childUrls) {
		super(baseClassLoader, ClassLoader.getSystemClassLoader().getParent());

		if (childUrls != null)
			childClassLoader = new URLClassLoader(childUrls, ClassLoader.getSystemClassLoader().getParent());
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

		Class<?> clazz = null;

		if (childClassLoader != null)
			try {
				clazz = childClassLoader.loadClass(name);
			} catch (ClassNotFoundException e) {
			}

		if (clazz == null)
			clazz = super.loadClass(name, resolve);

		return clazz;
	}

	@Override
	public URL getResource(String name) {

		URL resource = null;

		if (childClassLoader != null)
			resource = childClassLoader.getResource(name);

		if (resource == null)
			resource = super.getResource(name);

		return resource;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {

		if (childClassLoader != null)
			return new CompoundEnumeration<URL>(new Enumeration[] { childClassLoader.getResources(name), super.getResources(name) });
		else
			return super.getResources(name);
	}
}
