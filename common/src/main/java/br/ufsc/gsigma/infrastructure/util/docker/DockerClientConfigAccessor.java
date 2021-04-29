package br.ufsc.gsigma.infrastructure.util.docker;

import com.github.dockerjava.core.DockerClientConfig;

public interface DockerClientConfigAccessor {

	public DockerClientConfig getDockerClientConfig();

}
