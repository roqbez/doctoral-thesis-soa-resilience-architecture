package br.ufsc.gsigma.infrastructure.util.docker;

import com.github.dockerjava.api.model.AuthConfig;

public abstract class DockerRegistries {

	public static final AuthConfig DEFAULT_REGISTRY_AUTH_CONFIG = new AuthConfig() //
			.withUsername("docker-client") //
			.withPassword("DockerClient2k18");

}
