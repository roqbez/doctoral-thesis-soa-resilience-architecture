package br.ufsc.gsigma.infrastructure.util.docker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import de.gesellix.docker.client.DockerResponseHandler;
import de.gesellix.docker.client.authentication.ManageAuthenticationClient;
import de.gesellix.docker.client.config.ManageConfigClient;
import de.gesellix.docker.client.container.ManageContainerClient;
import de.gesellix.docker.client.distribution.ManageDistributionService;
import de.gesellix.docker.client.image.ManageImageClient;
import de.gesellix.docker.client.network.ManageNetworkClient;
import de.gesellix.docker.client.node.ManageNodeClient;
import de.gesellix.docker.client.node.NodeUtil;
import de.gesellix.docker.client.repository.RepositoryTagParser;
import de.gesellix.docker.client.secret.ManageSecretClient;
import de.gesellix.docker.client.service.ManageServiceClient;
import de.gesellix.docker.client.stack.DeployConfigReader;
import de.gesellix.docker.client.stack.DeployStackConfig;
import de.gesellix.docker.client.stack.DeployStackOptions;
import de.gesellix.docker.client.stack.ManageStackClient;
import de.gesellix.docker.client.swarm.ManageSwarmClient;
import de.gesellix.docker.client.system.ManageSystemClient;
import de.gesellix.docker.client.tasks.ManageTaskClient;
import de.gesellix.docker.client.volume.ManageVolumeClient;
import de.gesellix.docker.engine.DockerClientConfig;
import de.gesellix.docker.engine.DockerEnv;
import de.gesellix.docker.engine.EngineResponse;
import de.gesellix.docker.engine.OkDockerClient;
import de.gesellix.util.QueryUtil;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

public class GesellixDockerClient extends de.gesellix.docker.client.DockerClientImpl {

	public GesellixDockerClient(String host) {
		super(getDockerEnv(host));
	}

	public GesellixDockerClient(String host, File certPath) {
		super(getDockerEnv(host, certPath.getAbsolutePath()));
	}

	public GesellixDockerClient(DockerEnv dockerEnv) {
		super(dockerEnv);
	}

	private static DockerEnv getDockerEnv(String host) {
		return getDockerEnv(host, DockerServers.CERT_PATH.getAbsolutePath());
	}

	private static DockerEnv getDockerEnv(String host, String certPath) {
		DockerEnv dockerEnv = new DockerEnv();
		dockerEnv.setDockerHost(host.replaceAll("tcp://", "http://"));
		dockerEnv.setCertPath(certPath);
		return dockerEnv;
	}

	public void deployStack(String namespace, String yamlFile) throws URISyntaxException, IOException {

		DeployStackConfig deployConfig = getDeployStackConfig(namespace, yamlFile);

		deployStack(namespace, deployConfig);
	}

	public void deployStack(String namespace, DeployStackConfig deployConfig) {

		DeployStackOptions options = new DeployStackOptions();
		options.setSendRegistryAuth(true);

		stackDeploy(namespace, deployConfig, options);
	}

	public DeployStackConfig getDeployStackConfig(String namespace, URL yamlURL) throws URISyntaxException, IOException {

		File f = File.createTempFile("docker-swarm-stack-", "-" + namespace);

		try {

			try (InputStream in = yamlURL.openStream()) {
				FileUtils.copyToFile(in, f);
			}

			return getDeployStackConfig(namespace, f.getAbsolutePath());

		} finally {
			f.delete();
		}

	}

	public DeployStackConfig getDeployStackConfig(String namespace, String yamlFile) throws URISyntaxException, IOException {

		URL res = GesellixDockerClient.class.getResource(yamlFile);

		if (res == null) {
			File f = new File(yamlFile);
			if (f.exists()) {
				res = f.toURI().toURL();
			}
		}

		String workingDir = Paths.get(res.toURI()).getParent().toString();

		DeployStackConfig deployConfig = null;

		try (InputStream in = res.openStream()) {
			deployConfig = (DeployStackConfig) new DeployConfigReader(this).loadCompose(namespace, in, workingDir, new HashMap<String, String>());
		}

		return deployConfig;
	}

	@Override
	public Object apply(DockerClientConfig dockerClientConfig, Proxy proxy) {

		OkDockerClient httpClient = new OkDockerClient(dockerClientConfig, proxy) {
			@Override
			public OkHttpClient newClient(Builder clientBuilder) {
				clientBuilder.hostnameVerifier(new HostnameVerifier() {
					@Override
					public boolean verify(String host, SSLSession session) {
						try {
							Certificate[] certs = session.getPeerCertificates();
							if (certs != null && certs[0] instanceof X509Certificate) {
								return true;
							}
							return false;
						} catch (SSLException e) {
							return false;
						}
					}
				});

				return super.newClient(clientBuilder);
			}
		};

		DockerResponseHandler responseHandler = new DockerResponseHandler() {
			@Override
			public Object getErrors(Object response) {
				boolean success = ((EngineResponse) response).getStatus().isSuccess();
				Object r = !success ? super.getErrors(response) : Collections.EMPTY_LIST;
				return r;
			}
		};

		NodeUtil nodeUtil = new NodeUtil(getManageSystem());

		DockerEnv env = dockerClientConfig.getEnv();
		RepositoryTagParser repositoryTagParser = new RepositoryTagParser();
		QueryUtil queryUtil = new QueryUtil();

		ManageSystemClient manageSystem = new ManageSystemClient(httpClient, responseHandler);
		ManageAuthenticationClient manageAuthentication = new ManageAuthenticationClient(env, httpClient, responseHandler, manageSystem);
		ManageImageClient manageImage = new ManageImageClient(httpClient, responseHandler);
		ManageDistributionService manageDistribution = new ManageDistributionService(httpClient, responseHandler);
		ManageContainerClient manageContainer = new ManageContainerClient(httpClient, responseHandler, manageImage);
		ManageVolumeClient manageVolume = new ManageVolumeClient(httpClient, responseHandler);
		ManageNetworkClient manageNetwork = new ManageNetworkClient(httpClient, responseHandler);
		ManageSwarmClient manageSwarm = new ManageSwarmClient(httpClient, responseHandler);
		ManageSecretClient manageSecret = new ManageSecretClient(httpClient, responseHandler);
		ManageConfigClient manageConfig = new ManageConfigClient(httpClient, responseHandler);

		ManageTaskClient manageTask = new ManageTaskClient(httpClient, responseHandler);

		ManageServiceClient manageService = new ManageServiceClient(httpClient, responseHandler, manageTask, nodeUtil);
		ManageNodeClient manageNode = new ManageNodeClient(httpClient, responseHandler, manageTask, nodeUtil);
		ManageStackClient manageStackClient = new ManageStackClient(httpClient, responseHandler, manageService, manageTask, manageNode, manageNetwork, manageSecret, manageConfig, manageSystem, manageAuthentication);

		setProxy(proxy);
		setHttpClient(httpClient);
		setResponseHandler(responseHandler);
		setRepositoryTagParser(repositoryTagParser);
		setQueryUtil(queryUtil);
		setManageSystem(manageSystem);
		setManageAuthentication(manageAuthentication);
		setManageImage(manageImage);
		setManageDistribution(manageDistribution);
		setManageContainer(manageContainer);
		setManageVolume(manageVolume);
		setManageNetwork(manageNetwork);
		setManageSwarm(manageSwarm);
		setManageSecret(manageSecret);
		setManageConfig(manageConfig);
		setManageTask(manageTask);
		setManageService(manageService);
		setManageNode(manageNode);
		setManageStack(manageStackClient);

		return null;
	}

	public List<Map<String, Object>> httpGetToListMap(String path, Map<String, String> queryMap) {
		Object r = httpGet(path, queryMap).getContent();
		return toListMap(r);
	}

	public EngineResponse httpGet(String path, Map<String, String> queryMap) {

		Map<String, Object> requestConfig = new LinkedHashMap<String, Object>();

		requestConfig.put("path", path);
		requestConfig.put("query", queryMap != null ? queryMap : Collections.EMPTY_MAP);

		EngineResponse response = getHttpClient().get(requestConfig);

		getResponseHandler().ensureSuccessfulResponse(response, new IllegalStateException("httpGet " + requestConfig + " failed"));

		return response;
	}

	public EngineResponse httpPost(String path, Map<String, Object> headers, Object body) {

		Map<String, Object> requestConfig = new LinkedHashMap<String, Object>();

		requestConfig.put("path", path);
		requestConfig.put("headers", headers != null ? headers : Collections.EMPTY_MAP);
		requestConfig.put("body", body);
		requestConfig.put("requestContentType", "application/json");

		EngineResponse response = getHttpClient().post(requestConfig);

		getResponseHandler().ensureSuccessfulResponse(response, new IllegalStateException("httpPost " + requestConfig + " failed"));

		return response;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map<String, Object>> toListMap(Object r) {
		if (r instanceof List && !CollectionUtils.isEmpty((Collection) r)) {

			if ((((List) r).get(0)) instanceof Map) {
				return (List<Map<String, Object>>) r;
			}

			r = (((List) r).get(0));

			if (r instanceof List && !CollectionUtils.isEmpty((Collection) r)) {
				if ((((List) r).get(0)) instanceof Map) {
					return (List<Map<String, Object>>) r;
				}
			}
		}
		return null;
	}

}
