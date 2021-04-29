package br.ufsc.gsigma.architecture.bootstrap.docker;

import java.io.File;
import java.io.IOException;

public interface DockerApplication {

	public File createImage() throws Exception;

	public File createImageIfNecessary() throws Exception;

	public File createImage(File imageFileName) throws Exception;

	public void deploy() throws IOException, Exception;

	public File getImageFile();

}
