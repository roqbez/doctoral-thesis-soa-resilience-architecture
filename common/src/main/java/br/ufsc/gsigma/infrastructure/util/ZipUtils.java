package br.ufsc.gsigma.infrastructure.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

public abstract class ZipUtils {

	public static void zipFromClassLoaderResources(ClassLoader classLoader, String baseDirectory, ZipOutputStream zos) throws IOException {

		for (Entry<String, Resource> e : Util.getClassLoaderResources(classLoader, baseDirectory).entrySet()) {

			String basePath = Util.getResourceSubPath(baseDirectory, e.getKey());

			try (InputStream in = e.getValue().getInputStream()) {
				int read = 0;
				byte[] buffer = new byte[8192];

				ZipEntry entry = new ZipEntry(basePath);
				zos.putNextEntry(entry);

				while (-1 != (read = in.read(buffer))) {
					zos.write(buffer, 0, read);
				}
			}
		}
	}

	public static byte[] zip(File directory) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (ZipOutputStream out = new ZipOutputStream(baos)) {
			zip(directory, directory, out);
		}

		return baos.toByteArray();
	}

	public static void zip(File directory, File base, ZipOutputStream zos) throws IOException {
		File[] files = directory.listFiles();
		byte[] buffer = new byte[8192];
		int read = 0;
		for (int i = 0, n = files.length; i < n; i++) {
			if (files[i].isDirectory()) {
				zip(files[i], base, zos);
			} else {
				FileInputStream in = new FileInputStream(files[i]);
				String path = files[i].getPath().substring(base.getPath().length() + 1);
				path = FileUtils.adjustAbsolutePath(path);
				ZipEntry entry = new ZipEntry(path);
				zos.putNextEntry(entry);
				while (-1 != (read = in.read(buffer))) {
					zos.write(buffer, 0, read);
				}
				in.close();
			}
		}
	}

	public static void unzip(byte[] zipFileArray, File extractTo) throws IOException {
		try (ByteArrayInputStream in = new ByteArrayInputStream(zipFileArray)) {
			unzip(in, extractTo);
		}
	}

	public static void unzip(InputStream in, final File extractTo) throws IOException, FileNotFoundException {

		ZipEntryListener listener = new ZipEntryListener() {

			@Override
			public OutputStream prepareEntryWrite(ZipInputStream zis, ZipEntry entry) throws IOException {

				File file = new File(extractTo, entry.getName());
				if (entry.isDirectory()) {
					if (!file.exists())
						file.mkdirs();
					return null;
				} else {
					File dir = file.getParentFile();
					if (!dir.exists())
						dir.mkdirs();
					return new FileOutputStream(file);
				}
			}

			@Override
			public void finishEntryWrite(OutputStream out) throws IOException {
				out.close();
			}

		};

		unzip(in, listener);
	}

	public static void unzip(InputStream in, ZipEntryListener listener) throws IOException, FileNotFoundException {

		try (ZipInputStream zis = in instanceof ZipInputStream ? (ZipInputStream) in : new ZipInputStream(in)) {

			ZipEntry entry = null;

			while ((entry = zis.getNextEntry()) != null) {

				OutputStream os = listener.prepareEntryWrite(zis, entry);

				if (os != null) {
					try {
						IOUtils.copy(zis, os);
					} finally {
						listener.finishEntryWrite(os);
					}
				}
			}
		}
	}

	public static interface ZipEntryListener {

		public OutputStream prepareEntryWrite(ZipInputStream zis, ZipEntry entry) throws IOException;

		public void finishEntryWrite(OutputStream out) throws IOException;

	}

}
