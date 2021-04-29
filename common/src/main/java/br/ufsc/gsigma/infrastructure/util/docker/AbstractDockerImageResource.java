package br.ufsc.gsigma.infrastructure.util.docker;

public abstract class AbstractDockerImageResource implements DockerImageResource {

	private String path;

	private Object resource;

	protected AbstractDockerImageResource(String path, Object resource) {
		this.path = path;
		this.resource = resource;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Object getResource() {
		return resource;
	}

	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + " path=" + path + "]";
	}

}
