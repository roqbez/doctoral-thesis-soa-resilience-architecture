package br.ufsc.gsigma.infrastructure.util.docker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class FileDockerImageResouce extends AbstractDockerImageResource {

	public FileDockerImageResouce(String path, File file) {
		super(path, file);
	}

	@Override
	public void process(TarArchiveOutputStream tar, String basePath) throws IOException {

		String p = getPath();
		File f = (File) getResource();

		if (f.isDirectory()) {
			for (File f2 : FileUtils.listFiles(f, TrueFileFilter.TRUE, TrueFileFilter.TRUE)) {
				try (InputStream in = new FileInputStream(f2)) {

					String name = br.ufsc.gsigma.infrastructure.util.FileUtils.adjustAbsolutePath(f2.getAbsolutePath().substring(f.getAbsolutePath().length() + 1));
					String tarPath = basePath + "/" + p + "/" + name;

					if (logger.isDebugEnabled()) {
						logger.debug("Putting file in tar archive --> " + tarPath);
					}

					tar.putArchiveEntry(new TarArchiveEntry(f2, tarPath));
					IOUtils.copy(in, tar);
					tar.closeArchiveEntry();
				}
			}
		} else {
			try (InputStream in = new FileInputStream(f)) {

				String tarPath = basePath + "/" + p;

				if (logger.isDebugEnabled()) {
					logger.debug("Putting file in tar archive --> " + tarPath);
				}

				tar.putArchiveEntry(new TarArchiveEntry(f, tarPath));

				IOUtils.copy(in, tar);
				tar.closeArchiveEntry();
			}
		}

	}

}
