package br.ufsc.gsigma.infrastructure.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class JniLibrary {

	static final String SLASH = System.getProperty("file.separator");

	final private String name;
	final private String version;
	final private ClassLoader classLoader;
	private boolean loaded;

	public JniLibrary(String name) {
		this(name, null, JniLibrary.class.getClassLoader());
	}

	public JniLibrary(String name, Class<?> clazz) {
		this(name, version(clazz), clazz.getClassLoader());
	}

	public JniLibrary(String name, String version) {
		this(name, version, null);
	}

	public JniLibrary(String name, String version, ClassLoader classLoader) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}
		this.name = name;
		this.version = version;
		this.classLoader = classLoader;
	}

	private static String version(Class<?> clazz) {
		try {
			return clazz.getPackage().getImplementationVersion();
		} catch (Throwable e) {
		}
		return null;
	}

	public static String getOperatingSystem() {
		String name = System.getProperty("os.name").toLowerCase().trim();
		if (name.startsWith("linux")) {
			return "linux";
		}
		if (name.startsWith("mac os x")) {
			return "osx";
		}
		if (name.startsWith("win")) {
			return "windows";
		}
		return name.replaceAll("\\W+", "_");

	}

	public static String getPlatform() {
		return getOperatingSystem() + getBitModel();
	}

	public static int getBitModel() {
		String prop = System.getProperty("sun.arch.data.model");
		if (prop == null) {
			prop = System.getProperty("com.ibm.vm.bitmode");
		}
		if (prop != null) {
			return Integer.parseInt(prop);
		}
		return -1; // we don't know..
	}

	/**
     * 
     */
	synchronized public void load() {
		if (loaded) {
			return;
		}
		doLoad();
		loaded = true;
	}

	private void doLoad() {
		/* Perhaps a custom version is specified */
		String version = System.getProperty("library." + name + ".version");
		if (version == null) {
			version = this.version;
		}
		ArrayList<String> errors = new ArrayList<String>();

		/* Try loading library from a custom library path */
		String customPath = System.getProperty("library." + name + ".path");
		if (customPath != null) {
			if (version != null && load(errors, file(customPath, map(name + "-" + version))))
				return;
			if (load(errors, file(customPath, map(name))))
				return;
		}

		/* Try loading library from java library path */
		if (version != null && load(errors, name + getBitModel() + "-" + version))
			return;
		if (version != null && load(errors, name + "-" + version))
			return;
		if (load(errors, name))
			return;

		/* Try extracting the library from the jar */
		if (classLoader != null) {
			if (exractAndLoad(errors, version, customPath, getArchSpecifcResourcePath()))
				return;
			if (exractAndLoad(errors, version, customPath, getPlatformSpecifcResourcePath()))
				return;
			if (exractAndLoad(errors, version, customPath, getOperatingSystemSpecifcResourcePath()))
				return;
			// For the simpler case where only 1 platform lib is getting packed into the jar
			if (exractAndLoad(errors, version, customPath, getResorucePath()))
				return;
		}

		/* Failed to find the library */
		throw new UnsatisfiedLinkError("Could not load library. Reasons: " + errors.toString());
	}

	final public String getArchSpecifcResourcePath() {
		return "META-INF/native/" + getPlatform() + "/" + System.getProperty("os.arch") + "/" + map(name);
	}

	final public String getOperatingSystemSpecifcResourcePath() {
		return getPlatformSpecifcResourcePath(getOperatingSystem());
	}

	final public String getPlatformSpecifcResourcePath() {
		return getPlatformSpecifcResourcePath(getPlatform());
	}

	final public String getPlatformSpecifcResourcePath(String platform) {
		return "META-INF/native/" + platform + "/" + map(name);
	}

	final public String getResorucePath() {
		return "META-INF/native/" + map(name);
	}

	final public String getLibraryFileName() {
		return map(name);
	}

	private boolean exractAndLoad(ArrayList<String> errors, String version, String customPath, String resourcePath) {
		URL resource = classLoader.getResource(resourcePath);
		if (resource != null) {

			String libName = name;

			String[] libNameParts = map(libName).split("\\.");
			String prefix = libNameParts[0];
			String suffix = "." + libNameParts[1];

			if (customPath != null) {
				// Try to extract it to the custom path...
				File target = extract(errors, resource, prefix, suffix, file(customPath));
				if (target != null) {
					if (load(errors, target)) {
						return true;
					}
				}
			}

			// Fall back to extracting to the tmp dir
			customPath = System.getProperty("java.io.tmpdir");

			File target = extract(errors, resource, prefix, suffix, file(customPath));
			if (target != null) {
				if (load(errors, target)) {
					return true;
				}
			}
		}
		return false;
	}

	private static void addLibraryPath(String pathToAdd) {

		try {

			final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
			usrPathsField.setAccessible(true);

			final String[] paths = (String[]) usrPathsField.get(null);

			for (String path : paths) {
				if (path.equals(pathToAdd)) {
					return;
				}
			}

			final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
			newPaths[newPaths.length - 1] = pathToAdd;
			usrPathsField.set(null, newPaths);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private File file(String... paths) {
		File rc = null;
		for (String path : paths) {
			if (rc == null) {
				rc = new File(path);
			} else {
				rc = new File(rc, path);
			}
		}
		return rc;
	}

	private String map(String libName) {
		/*
		 * libraries in the Macintosh use the extension .jnilib but the some VMs map to .dylib.
		 */
		libName = System.mapLibraryName(libName);
		String ext = ".dylib";
		if (libName.endsWith(ext)) {
			libName = libName.substring(0, libName.length() - ext.length()) + ".jnilib";
		}
		return libName;
	}

	private File extract(ArrayList<String> errors, URL source, String prefix, String suffix, File directory) {
		File target = null;
		if (directory != null) {
			directory = directory.getAbsoluteFile();
		}
		try {
			FileOutputStream os = null;
			InputStream is = null;
			try {

				addLibraryPath(directory.getAbsolutePath());

				// target = File.createTempFile(prefix, suffix, directory);
				target = new File(directory, prefix + suffix);
				is = source.openStream();
				if (is != null) {
					byte[] buffer = new byte[4096];
					os = new FileOutputStream(target);
					int read;
					while ((read = is.read(buffer)) != -1) {
						os.write(buffer, 0, read);
					}
					chmod("755", target);
				}
				target.deleteOnExit();
				return target;
			} finally {
				close(os);
				close(is);
			}
		} catch (Throwable e) {
			if (target != null) {
				target.delete();
			}
			errors.add(e.getMessage());
		}
		return null;
	}

	static private void close(Closeable file) {
		if (file != null) {
			try {
				file.close();
			} catch (Exception ignore) {
			}
		}
	}

	private void chmod(String permision, File path) {
		if (getPlatform().startsWith("windows"))
			return;
		try {
			Runtime.getRuntime().exec(new String[] { "chmod", permision, path.getCanonicalPath() }).waitFor();
		} catch (Throwable e) {
		}
	}

	private boolean load(ArrayList<String> errors, File lib) {
		try {
			System.load(lib.getPath());
			return true;
		} catch (UnsatisfiedLinkError e) {
			errors.add(e.getMessage());
		}
		return false;
	}

	private boolean load(ArrayList<String> errors, String lib) {
		try {
			System.loadLibrary(lib);
			return true;
		} catch (UnsatisfiedLinkError e) {
			errors.add(e.getMessage());
		}
		return false;
	}
}
