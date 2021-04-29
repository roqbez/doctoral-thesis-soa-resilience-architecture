package br.ufsc.gsigma.infrastructure.util.maven;

public abstract class Repository {
	private String id;

	private String layout = "default";

	protected abstract String getDefaultId();

	public String getId() {
		if (getInstance().id == null) {
			getInstance().setId(getDefaultId());
		}
		return getInstance().id;
	}

	public void setId(String id) {
		this.id = id;
	}

	protected Repository getInstance() {
		return this;
	}

	public String getLayout() {
		return getInstance().layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}
}
