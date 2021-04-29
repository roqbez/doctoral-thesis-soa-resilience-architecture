package br.ufsc.gsigma.infrastructure.util.docker;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.dockerjava.api.DockerClient;

import br.ufsc.gsigma.infrastructure.util.Util;
import edu.emory.mathcs.backport.java.util.Arrays;

public abstract class DockerServers {

	public static final File CERT_PATH = Util.getDirectoryFromClassLoaderResources("br/ufsc/gsigma/infrastructure/util/docker/cert/server");

	public static final String SERVER_150_162_6_131 = "SERVER_150_162_6_131";

	public static final String SERVER_150_162_6_133 = "SERVER_150_162_6_133";

	public static final String SERVER_150_162_6_63 = "SERVER_150_162_6_63";

	public static final String SERVER_150_162_6_65 = "SERVER_150_162_6_65";

	public static final String SERVER_150_162_6_194 = "SERVER_150_162_6_194";

	public static final String SERVER_150_162_6_15 = "SERVER_150_162_6_15";

	public static final String SERVER_150_162_6_22 = "SERVER_150_162_6_22";

	public static final String SERVER_150_162_6_210 = "SERVER_150_162_6_210";

	public static final String SERVER_150_162_6_147 = "SERVER_150_162_6_147";

	public static final String SERVER_150_162_6_201 = "SERVER_150_162_6_201";

	public static final String SERVER1 = SERVER_150_162_6_131;
	public static final String SERVER2 = SERVER_150_162_6_133;
	public static final String SERVER3 = SERVER_150_162_6_63;
	public static final String SERVER4 = SERVER_150_162_6_194;
	public static final String SERVER5 = SERVER_150_162_6_15;
	public static final String SERVER6 = SERVER_150_162_6_22;
	public static final String SERVER7 = SERVER_150_162_6_65;
	public static final String SERVER8 = SERVER_150_162_6_201;

	private static final String TCP_150_162_6_194_2376 = "tcp://150.162.6.194:2376";

	private static final String TCP_150_162_6_63_2376 = "tcp://150.162.6.63:2376";

	private static final String TCP_150_162_6_65_2376 = "tcp://150.162.6.65:2376";

	private static final String TCP_150_162_6_133_2376 = "tcp://150.162.6.133:2376";

	private static final String TCP_150_162_6_131_2376 = "tcp://150.162.6.131:2376";

	private static final String TCP_150_162_6_15_2376 = "tcp://150.162.6.15:2376";

	private static final String TCP_150_162_6_22_2376 = "tcp://150.162.6.22:2376";

	private static final String TCP_150_162_6_210_2376 = "tcp://150.162.6.210:2376";

	private static final String TCP_150_162_6_147_2376 = "tcp://150.162.6.147:2376";

	private static final String TCP_150_162_6_201_2376 = "tcp://150.162.6.201:2376";

	private static final Map<String, DockerClient> SERVERS_FOR_ADHOC_DEPLOYMENT = new LinkedHashMap<String, DockerClient>();

	private static final Map<String, DockerClient> ALL_SERVERS = new LinkedHashMap<String, DockerClient>();

	private static final Map<String, GesellixDockerClient> GESELLIX_DOCKER_CLIENTS = new LinkedHashMap<String, GesellixDockerClient>();

	private static final Map<String, String> ADDR_TO_NAME = new LinkedHashMap<String, String>();

	public static final DockerClient DOCKER_CLIENT_150_162_6_131 = DockerUtil.getDockerClient(TCP_150_162_6_131_2376, CERT_PATH);
	public static final DockerClient DOCKER_CLIENT_150_162_6_133 = DockerUtil.getDockerClient(TCP_150_162_6_133_2376, CERT_PATH);
	public static final DockerClient DOCKER_CLIENT_150_162_6_63 = DockerUtil.getDockerClient(TCP_150_162_6_63_2376, CERT_PATH);
	public static final DockerClient DOCKER_CLIENT_150_162_6_65 = DockerUtil.getDockerClient(TCP_150_162_6_65_2376, CERT_PATH);
	public static final DockerClient DOCKER_CLIENT_150_162_6_194 = DockerUtil.getDockerClient(TCP_150_162_6_194_2376, CERT_PATH);
	public static final DockerClient DOCKER_CLIENT_150_162_6_15 = DockerUtil.getDockerClient(TCP_150_162_6_15_2376, CERT_PATH);
	public static final DockerClient DOCKER_CLIENT_150_162_6_22 = DockerUtil.getDockerClient(TCP_150_162_6_22_2376, CERT_PATH);
	public static final DockerClient DOCKER_CLIENT_150_162_6_210 = DockerUtil.getDockerClient(TCP_150_162_6_210_2376, CERT_PATH);
	public static final DockerClient DOCKER_CLIENT_150_162_6_147 = DockerUtil.getDockerClient(TCP_150_162_6_147_2376, CERT_PATH);
	public static final DockerClient DOCKER_CLIENT_150_162_6_201 = DockerUtil.getDockerClient(TCP_150_162_6_201_2376, CERT_PATH);

	public static final GesellixDockerClient GESELLIX_DOCKER_CLIENT_150_162_6_131 = new GesellixDockerClient(TCP_150_162_6_131_2376, CERT_PATH);
	public static final GesellixDockerClient GESELLIX_DOCKER_CLIENT_150_162_6_133 = new GesellixDockerClient(TCP_150_162_6_133_2376, CERT_PATH);
	public static final GesellixDockerClient GESELLIX_DOCKER_CLIENT_150_162_6_63 = new GesellixDockerClient(TCP_150_162_6_63_2376, CERT_PATH);
	public static final GesellixDockerClient GESELLIX_DOCKER_CLIENT_150_162_6_65 = new GesellixDockerClient(TCP_150_162_6_65_2376, CERT_PATH);
	public static final GesellixDockerClient GESELLIX_DOCKER_CLIENT_150_162_6_194 = new GesellixDockerClient(TCP_150_162_6_194_2376, CERT_PATH);
	public static final GesellixDockerClient GESELLIX_DOCKER_CLIENT_150_162_6_15 = new GesellixDockerClient(TCP_150_162_6_15_2376, CERT_PATH);
	public static final GesellixDockerClient GESELLIX_DOCKER_CLIENT_150_162_6_22 = new GesellixDockerClient(TCP_150_162_6_22_2376, CERT_PATH);
	public static final GesellixDockerClient GESELLIX_DOCKER_CLIENT_150_162_6_210 = new GesellixDockerClient(TCP_150_162_6_210_2376, CERT_PATH);
	public static final GesellixDockerClient GESELLIX_DOCKER_CLIENT_150_162_6_147 = new GesellixDockerClient(TCP_150_162_6_147_2376, CERT_PATH);
	public static final GesellixDockerClient GESELLIX_DOCKER_CLIENT_150_162_6_201 = new GesellixDockerClient(TCP_150_162_6_201_2376, CERT_PATH);


	public static final String TCP_SWARM_MANAGER = TCP_150_162_6_63_2376;
	public static final String SWARM_MANAGER = SERVER_150_162_6_63;

	public static final DockerClient DOCKER_CLIENT_SWARM_MANAGER = DOCKER_CLIENT_150_162_6_63;
	public static final GesellixDockerClient GESELLIX_DOCKER_CLIENT_SWARM_MANAGER = GESELLIX_DOCKER_CLIENT_150_162_6_63;

	static {

		SERVERS_FOR_ADHOC_DEPLOYMENT.put(SERVER_150_162_6_63, DOCKER_CLIENT_150_162_6_63);
		SERVERS_FOR_ADHOC_DEPLOYMENT.put(SERVER_150_162_6_133, DOCKER_CLIENT_150_162_6_133);
		SERVERS_FOR_ADHOC_DEPLOYMENT.put(SERVER_150_162_6_194, DOCKER_CLIENT_150_162_6_194);
		SERVERS_FOR_ADHOC_DEPLOYMENT.put(SERVER_150_162_6_210, DOCKER_CLIENT_150_162_6_210);
		SERVERS_FOR_ADHOC_DEPLOYMENT.put(SERVER_150_162_6_147, DOCKER_CLIENT_150_162_6_147);

		ALL_SERVERS.put(SERVER_150_162_6_131, DOCKER_CLIENT_150_162_6_131);
		ALL_SERVERS.put(SERVER_150_162_6_133, DOCKER_CLIENT_150_162_6_133);
		ALL_SERVERS.put(SERVER_150_162_6_63, DOCKER_CLIENT_150_162_6_63);
		ALL_SERVERS.put(SERVER_150_162_6_65, DOCKER_CLIENT_150_162_6_65);
		ALL_SERVERS.put(SERVER_150_162_6_194, DOCKER_CLIENT_150_162_6_194);
		ALL_SERVERS.put(SERVER_150_162_6_15, DOCKER_CLIENT_150_162_6_15);
		ALL_SERVERS.put(SERVER_150_162_6_22, DOCKER_CLIENT_150_162_6_22);
		ALL_SERVERS.put(SERVER_150_162_6_210, DOCKER_CLIENT_150_162_6_210);
		ALL_SERVERS.put(SERVER_150_162_6_147, DOCKER_CLIENT_150_162_6_147);
		ALL_SERVERS.put(SERVER_150_162_6_201, DOCKER_CLIENT_150_162_6_201);

		GESELLIX_DOCKER_CLIENTS.put(SERVER_150_162_6_131, GESELLIX_DOCKER_CLIENT_150_162_6_131);
		GESELLIX_DOCKER_CLIENTS.put(SERVER_150_162_6_133, GESELLIX_DOCKER_CLIENT_150_162_6_133);
		GESELLIX_DOCKER_CLIENTS.put(SERVER_150_162_6_63, GESELLIX_DOCKER_CLIENT_150_162_6_63);
		GESELLIX_DOCKER_CLIENTS.put(SERVER_150_162_6_65, GESELLIX_DOCKER_CLIENT_150_162_6_65);
		GESELLIX_DOCKER_CLIENTS.put(SERVER_150_162_6_194, GESELLIX_DOCKER_CLIENT_150_162_6_194);
		GESELLIX_DOCKER_CLIENTS.put(SERVER_150_162_6_15, GESELLIX_DOCKER_CLIENT_150_162_6_15);
		GESELLIX_DOCKER_CLIENTS.put(SERVER_150_162_6_22, GESELLIX_DOCKER_CLIENT_150_162_6_22);
		GESELLIX_DOCKER_CLIENTS.put(SERVER_150_162_6_210, GESELLIX_DOCKER_CLIENT_150_162_6_210);
		GESELLIX_DOCKER_CLIENTS.put(SERVER_150_162_6_147, GESELLIX_DOCKER_CLIENT_150_162_6_147);
		GESELLIX_DOCKER_CLIENTS.put(SERVER_150_162_6_201, GESELLIX_DOCKER_CLIENT_150_162_6_201);

		ADDR_TO_NAME.put(getAddr(TCP_150_162_6_131_2376), SERVER_150_162_6_131);
		ADDR_TO_NAME.put(getAddr(TCP_150_162_6_133_2376), SERVER_150_162_6_133);
		ADDR_TO_NAME.put(getAddr(TCP_150_162_6_63_2376), SERVER_150_162_6_63);
		ADDR_TO_NAME.put(getAddr(TCP_150_162_6_65_2376), SERVER_150_162_6_65);
		ADDR_TO_NAME.put(getAddr(TCP_150_162_6_194_2376), SERVER_150_162_6_194);
		ADDR_TO_NAME.put(getAddr(TCP_150_162_6_15_2376), SERVER_150_162_6_15);
		ADDR_TO_NAME.put(getAddr(TCP_150_162_6_22_2376), SERVER_150_162_6_22);
		ADDR_TO_NAME.put(getAddr(TCP_150_162_6_210_2376), SERVER_150_162_6_210);
		ADDR_TO_NAME.put(getAddr(TCP_150_162_6_147_2376), SERVER_150_162_6_147);
		ADDR_TO_NAME.put(getAddr(TCP_150_162_6_201_2376), SERVER_150_162_6_201);
	}

	private static String getAddr(String url) {
		try {
			return new URI(url).getHost();
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public static Collection<String> getServerForAdhocDeployment() {
		return new ArrayList<String>(SERVERS_FOR_ADHOC_DEPLOYMENT.keySet());
	}

	@SuppressWarnings("unchecked")
	public static Collection<String> getAllServerKeys() {
		return Arrays.asList(new String[] { SERVER1, SERVER2, SERVER3, SERVER4, SERVER5, SERVER6, SERVER7 });
	}

	public static String[] getServerAddresses() {
		return new String[] { TCP_150_162_6_194_2376, TCP_150_162_6_63_2376, TCP_150_162_6_65_2376, TCP_150_162_6_133_2376, TCP_150_162_6_131_2376, TCP_150_162_6_15_2376, TCP_150_162_6_22_2376 };
	}

	public static DockerClient getDockerClient(String serverName) {
		return ALL_SERVERS.get(serverName);
	}

	public static GesellixDockerClient getGesellixDockerClient(String serverName) {
		return GESELLIX_DOCKER_CLIENTS.get(serverName);
	}

	public static String getDockerServerNameFromAddress(String address) {
		String r = ADDR_TO_NAME.get(address);

		if (r == null) {
			r = ADDR_TO_NAME.get(getAddr(address));
		}

		return r;
	}

}
