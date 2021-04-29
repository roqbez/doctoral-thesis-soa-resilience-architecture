package br.ufsc.gsigma.infrastructure.jgroups;

import org.jgroups.conf.ClassConfigurator;
import org.jgroups.protocols.dns.DNS_PING;

public class DOCKER_DNS_PING extends DNS_PING {

	static {
		ClassConfigurator.addProtocol((short) 1024, DOCKER_DNS_PING.class);
	}

	protected void validateProperties() {
		if (dns_query == null) {
			throw new IllegalArgumentException("dns_query can not be null or empty");
		}
	}

}
