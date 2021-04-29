package br.ufsc.gsigma.infrastructure.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.filefilter.TrueFileFilter;

public class FileUtils {

	public static String adjustAbsolutePath(File file) {
		return adjustAbsolutePath(file.getAbsolutePath());
	}

	public static String adjustAbsolutePath(String path) {
		return path.replaceAll("\\" + File.separator, "/");
	}

	public static void buildFolder(File[] baseDirs, File destDir, final String[] contentSubstitutionStr) throws IOException {
		buildFolder(baseDirs, destDir, contentSubstitutionStr, true);
	}

	public static void buildFolder(File baseDir, File destDir) throws IOException {
		buildFolder(new File[] { baseDir }, destDir, null, true);
	}

	public static void buildFolder(File[] baseDirs, File destDir, final String[] contentSubstitutionStr, boolean removeDestDir) throws IOException {

		if (removeDestDir)
			recursiveDelete(destDir);

		Map<String, String> contentSubstitution = null;

		if (contentSubstitutionStr != null) {
			contentSubstitution = new HashMap<String, String>(contentSubstitutionStr.length);

			for (String s : contentSubstitutionStr) {
				int i = s.indexOf('=');
				contentSubstitution.put(s.substring(0, i), s.substring(i + 1));
			}

		}

		buildFolder(baseDirs, destDir, contentSubstitution);

	}

	public static void buildFolder(File[] baseDirs, File destDir, final Map<String, String> contentSubstitution) throws IOException {

		ITokenResolver resolver = null;

		if (contentSubstitution != null)
			resolver = new ITokenResolver() {

				@Override
				public String resolveToken(String tokenName) {
					return contentSubstitution.get(tokenName);
				}
			};

		for (File f : baseDirs)
			doCopyDirectory(f, destDir, resolver);
	}

	public static File createTempDir() throws IOException {
		final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
		File newTempDir;
		final int maxAttempts = 9;
		int attemptCount = 0;
		do {
			attemptCount++;
			if (attemptCount > maxAttempts) {
				throw new IOException("The highly improbable has occurred! Failed to " + "create a unique temporary directory after " + maxAttempts + " attempts.");
			}
			String dirName = UUID.randomUUID().toString();
			newTempDir = new File(sysTempDir, dirName);
		} while (newTempDir.exists());

		if (newTempDir.mkdirs()) {
			return newTempDir;
		} else {
			throw new IOException("Failed to create temp dir named " + newTempDir.getAbsolutePath());
		}
	}

	public static Collection<File> listFilesAndDirs(File directory) {
		Collection<File> result = org.apache.commons.io.FileUtils.listFilesAndDirs(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		result.remove(directory);
		return result;
	}

	public static boolean recursiveDelete(File fileOrDir) {
		if (fileOrDir.isDirectory()) {
			for (File innerFile : fileOrDir.listFiles()) {
				if (!recursiveDelete(innerFile)) {
					return false;
				}
			}
		}
		return fileOrDir.delete();
	}

	public static void deleteDirectory(File directory) throws IOException {
		org.apache.commons.io.FileUtils.deleteDirectory(directory);
	}

	private static void doCopyDirectory(File srcDir, File destDir, ITokenResolver resolver) throws IOException {
		if (destDir.exists()) {
			if (destDir.isDirectory() == false) {
				throw new IOException("Destination '" + destDir + "' exists but is not a directory");
			}
		} else {
			if (destDir.mkdirs() == false) {
				throw new IOException("Destination '" + destDir + "' directory cannot be created");
			}
		}
		if (destDir.canWrite() == false) {
			throw new IOException("Destination '" + destDir + "' cannot be written to");
		}
		// recurse
		File[] files = srcDir.listFiles();
		if (files == null) { // null if security restricted
			throw new IOException("Failed to list contents of " + srcDir);
		}
		for (int i = 0; i < files.length; i++) {
			File copiedFile = new File(destDir, files[i].getName());
			if (files[i].isDirectory()) {
				doCopyDirectory(files[i], copiedFile, resolver);
			} else {
				doCopyFile(files[i], copiedFile, resolver);
			}
		}
	}

	private static void doCopyFile(File srcFile, File destFile, ITokenResolver resolver) throws IOException {
		if (destFile.exists() && destFile.isDirectory()) {
			throw new IOException("Destination '" + destFile + "' exists but is a directory");
		}

		FileInputStream input = new FileInputStream(srcFile);
		FileOutputStream output = new FileOutputStream(destFile);

		if (resolver == null || isBinaryFile(srcFile))
			copy(input, output);
		else
			copyText(input, output, resolver);
	}

	private static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ioe) {
		}
	}

	private static void copy(InputStream input, OutputStream output) throws IOException {

		try {
			try {
				byte[] buffer = new byte[4096];
				int n = 0;
				while (-1 != (n = input.read(buffer))) {
					output.write(buffer, 0, n);
				}
			} finally {
				closeQuietly(output);
			}
		} finally {
			closeQuietly(input);
		}

	}

	private static void copyText(InputStream input, OutputStream output, ITokenResolver resolver) throws IOException {

		Reader reader = new TokenReplacingReader(new InputStreamReader(input), resolver);
		Writer writer = new OutputStreamWriter(output);

		try {
			try {
				char[] buffer = new char[4096];
				int n = 0;
				while (-1 != (n = reader.read(buffer))) {
					writer.write(buffer, 0, n);
				}
			} finally {
				closeQuietly(writer);
				closeQuietly(output);
			}
		} finally {
			closeQuietly(reader);
			closeQuietly(input);
		}

	}

	public static boolean isBinaryFile(File f) throws FileNotFoundException, IOException {
		FileInputStream in = new FileInputStream(f);
		int size = in.available();
		if (size > 1024)
			size = 1024;
		byte[] data = new byte[size];
		in.read(data);
		in.close();

		int ascii = 0;
		int other = 0;

		for (int i = 0; i < data.length; i++) {
			byte b = data[i];
			if (b < 0x09)
				return true;

			if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D)
				ascii++;
			else if (b >= 0x20 && b <= 0x7E)
				ascii++;
			else
				other++;
		}

		if (other == 0)
			return false;

		return 100 * other / (ascii + other) > 95;
	}
}
