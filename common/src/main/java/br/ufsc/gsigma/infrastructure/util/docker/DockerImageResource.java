package br.ufsc.gsigma.infrastructure.util.docker;

import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DockerImageResource {

	final Logger logger = LoggerFactory.getLogger(DockerImageResource.class);

	public String getPath();

	public Object getResource();

	public void process(TarArchiveOutputStream tar, String basePath) throws IOException;
}
