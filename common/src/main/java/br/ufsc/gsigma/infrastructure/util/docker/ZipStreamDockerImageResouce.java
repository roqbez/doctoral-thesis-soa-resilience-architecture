package br.ufsc.gsigma.infrastructure.util.docker;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.lang.StringUtils;

import br.ufsc.gsigma.infrastructure.util.ZipUtils;
import br.ufsc.gsigma.infrastructure.util.ZipUtils.ZipEntryListener;

public class ZipStreamDockerImageResouce extends AbstractDockerImageResource {

	private String zipBasePath;

	public ZipStreamDockerImageResouce(String path, ZipInputStream in, String zipBasePath) {
		super(path, in);
		this.zipBasePath = zipBasePath != null && !zipBasePath.endsWith("/") ? zipBasePath + "/" : zipBasePath;
	}

	@Override
	public void process(final TarArchiveOutputStream tar, final String basePath) throws IOException {

		ZipUtils.unzip((ZipInputStream) getResource(), new ZipEntryListener() {
			@Override
			public OutputStream prepareEntryWrite(ZipInputStream zis, ZipEntry entry) throws IOException {

				String name = entry.getName();
				name = name.startsWith(zipBasePath) ? name.substring(zipBasePath.length()) : entry.getName();

				if (!StringUtils.isBlank(name)) {
					TarArchiveEntry archiveEntry = new TarArchiveEntry(basePath + "/" + zipBasePath + name);
					archiveEntry.setSize(entry.getSize());
					tar.putArchiveEntry(archiveEntry);
					return tar;
				} else {
					return null;
				}
			}

			@Override
			public void finishEntryWrite(OutputStream out) throws IOException {
				tar.closeArchiveEntry();
			}
		});
	}
}
