package br.ufsc.gsigma.infrastructure.util.maven;

import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.settings.Server;

public class Authentication extends AuthenticationInfo {

	private static final long serialVersionUID = 1L;

	public Authentication() {
		super();
	}

	public Authentication(Server server) {
		setUserName(server.getUsername());
		setPassword(server.getPassword());
		setPassphrase(server.getPassphrase());
		setPrivateKey(server.getPrivateKey());
	}
}
