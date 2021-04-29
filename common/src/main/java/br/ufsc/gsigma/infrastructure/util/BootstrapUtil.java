package br.ufsc.gsigma.infrastructure.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BootstrapUtil {

	private static final Logger logger = LoggerFactory.getLogger(BootstrapUtil.class);

	public static ClassLoader getIsolatedURLClassLoader(File classPathBase, boolean inheritBootstrapClassPath) throws MalformedURLException {
		return new IsolatedURLClassLoader(new URL[] { classPathBase.toURI().toURL() }, inheritBootstrapClassPath);
	}

	public static ClassLoader getIsolatedURLClassLoader(File[] classPathBaseFiles) throws MalformedURLException {
		return getIsolatedURLClassLoader(classPathBaseFiles, null);
	}

	public static ClassLoader getIsolatedURLClassLoader(File[] classPathBaseFiles, File childClassPath) throws MalformedURLException {

		URL[] classPathBase = new URL[classPathBaseFiles.length];

		for (int i = 0; i < classPathBaseFiles.length; i++)
			classPathBase[i] = classPathBaseFiles[i].toURI().toURL();

		return new IsolatedURLClassLoader(classPathBase, childClassPath != null ? new URL[] { childClassPath.toURI().toURL() } : null);
	}

	public static void bootstrapIsolated(final String className, File[] classPathBase) throws MalformedURLException, InterruptedException {
		ClassLoader classLoader = getIsolatedURLClassLoader(classPathBase);
		bootstrapIsolated(className, "main", new Class[] { String[].class }, new Object[] { new String[0] }, classLoader, true);
	}

	public static void bootstrapIsolated(final String className, File[] classPathBase, String... args) throws MalformedURLException, InterruptedException {
		ClassLoader classLoader = getIsolatedURLClassLoader(classPathBase);
		bootstrapIsolated(className, "main", new Class[] { String[].class }, new Object[] { args }, classLoader, true);
	}

	public static void bootstrapIsolated(final String className, String methodName, File classPathBase) throws MalformedURLException, InterruptedException {
		bootstrapIsolated(className, methodName, new Class[0], new Object[0], classPathBase, true, false);
	}

	public static void bootstrapIsolated(final String className, final String methodName, File[] classPathBase, File childClassPath, boolean parallel)
			throws MalformedURLException, InterruptedException {
		ClassLoader classLoader = getIsolatedURLClassLoader(classPathBase, childClassPath);
		bootstrapIsolated(className, methodName, new Class[0], new Object[0], classLoader, parallel);

	}

	public static void bootstrapIsolated(final String className, final String methodName, final Class<?>[] methodParametersTypes, final Object[] methodParameters, File[] classPathBase,
			File childClassPath, boolean parallel) throws MalformedURLException, InterruptedException {
		ClassLoader classLoader = getIsolatedURLClassLoader(classPathBase, childClassPath);
		bootstrapIsolated(className, methodName, methodParametersTypes, methodParameters, classLoader, parallel);
	}

	public static void bootstrapIsolated(final String className, final String methodName, final Class<?>[] methodParametersTypes, final Object[] methodParameters, File classPathBase, boolean parallel)
			throws MalformedURLException, InterruptedException {
		bootstrapIsolated(className, methodName, methodParametersTypes, methodParameters, classPathBase, true, false);
	}

	public static void bootstrapIsolated(final String className, final String methodName, final Class<?>[] methodParametersTypes, final Object[] methodParameters, File classPathBase, boolean parallel,
			boolean inheritBootstrapClassPath) throws MalformedURLException, InterruptedException {

		ClassLoader classLoader = getIsolatedURLClassLoader(classPathBase, inheritBootstrapClassPath);
		bootstrapIsolated(className, methodName, methodParametersTypes, methodParameters, classLoader, parallel);
	}

	public static void bootstrapIsolated(final String className, final String methodName, final Class<?>[] methodParametersTypes, final Object[] methodParameters, final ClassLoader classLoader,
			final boolean parallel) throws MalformedURLException, InterruptedException {
		bootstrapInternal(className, methodName, methodParametersTypes, methodParameters, classLoader, parallel);
	}

	public static void bootstrap(final String className, final String methodName, final Class<?>[] methodParametersTypes, final Object[] methodParameters)
			throws MalformedURLException, InterruptedException {
		bootstrapInternal(className, methodName, methodParametersTypes, methodParameters, BootstrapUtil.class.getClassLoader(), false);
	}

	private static void bootstrapInternal(final String className, final String methodName, final Class<?>[] methodParametersTypes, final Object[] methodParameters, final ClassLoader classLoader,
			final boolean parallel) throws MalformedURLException, InterruptedException {

		bootstrapInternal(new Runnable() {
			@Override
			public void run() {
				try {
					long s = System.currentTimeMillis();
					logger.info("Bootstraping " + className + "." + methodName + (parallel ? " in parallel " : ""));
					Class<?> mainClass = classLoader.loadClass(className);
					Method main = mainClass.getMethod(methodName, methodParametersTypes);
					main.invoke(null, methodParameters);
					long d = System.currentTimeMillis() - s;
					logger.info("Bootstraping of " + className + "." + methodName + " complete (" + d + " ms)");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			}
		}, classLoader, parallel);
	}

	private static void bootstrapInternal(Runnable runnable, ClassLoader classLoader, boolean parallel) throws MalformedURLException, InterruptedException {

		if (parallel) {

			Thread t = new Thread(runnable);
			t.setContextClassLoader(classLoader);

			t.start();

		} else {

			ClassLoader previous = Thread.currentThread().getContextClassLoader();

			try {
				Thread.currentThread().setContextClassLoader(classLoader);
				runnable.run();
			} finally {
				Thread.currentThread().setContextClassLoader(previous);
			}
		}

	}
}
