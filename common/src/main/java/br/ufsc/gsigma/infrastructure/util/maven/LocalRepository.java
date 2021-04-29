package br.ufsc.gsigma.infrastructure.util.maven;

import java.io.File;

public class LocalRepository extends Repository {
	private File path;

	public File getPath() {
		return ((LocalRepository) getInstance()).path;
	}

	public void setPath(File path) {
		this.path = path;
	}

	protected String getDefaultId() {
		return "local";
	}
}