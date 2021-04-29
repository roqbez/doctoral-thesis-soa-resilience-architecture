package br.ufsc.gsigma.architecture.bootstrap.docker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.DockerClient;

import br.ufsc.gsigma.infrastructure.MavenPOMLocation;
import br.ufsc.gsigma.infrastructure.util.FileUtils;
import br.ufsc.gsigma.infrastructure.util.docker.DockerImageResource;
import br.ufsc.gsigma.infrastructure.util.docker.DockerUtil;
import br.ufsc.gsigma.infrastructure.util.docker.FileDockerImageResouce;
import br.ufsc.gsigma.infrastructure.util.docker.ZipStreamDockerImageResouce;
import br.ufsc.gsigma.infrastructure.util.maven.MavenUtil;
import br.ufsc.gsigma.infrastructure.util.maven.Pom;
import br.ufsc.gsigma.infrastructure.ws.ServicesAddresses;

public class DockerExecutionService {

	private static final Logger logger = LoggerFactory.getLogger(DockerExecutionService.class);

	private static final String HOSTNAME = ServicesAddresses.EXECUTION_SERVICE_HOSTNAME;

	private static final int PORT = ServicesAddresses.EXECUTION_SERVICE_PORT;

	private static final int DEBUG_PORT = ServicesAddresses.EXECUTION_SERVICE_DEBUG_PORT;

	private static final String MAIN_CLASS = "br.ufsc.gsigma.services.execution.bpel.bootstrap.RunExecutionService";

	private static final Pom POM;

	private static final String IMAGE_NAME = "execution-service";

	private static final String CONTAINER_NAME = "execution-service";

	static {
		try {
			POM = MavenUtil.getPOMFromFile(MavenPOMLocation.POM_SERVICES_EXECUTION_BPEL);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static File createImageFile() throws Exception {
		return createImage(true, new AtomicBoolean());
	}

	private static File createImage(boolean overwrite, AtomicBoolean alreadyExists) throws Exception {

		File f = getImageFileName(POM);

		if (!f.exists()) {
			alreadyExists.set(false);
		} else {
			alreadyExists.set(true);
		}

		f = !f.exists() || overwrite ? createImage(f) : f;

		return f;
	}

	public static void main(String[] args) throws Exception {

		try {
			deploy(false);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

	public static void deploy(boolean forceDeploy) throws Exception, IOException {

		logger.info("Deploying container " + CONTAINER_NAME + " with image " + IMAGE_NAME);

		AtomicBoolean alreadyExists = new AtomicBoolean(false);

		File f = createImage(forceDeploy, alreadyExists);

		boolean redeploy = forceDeploy || !alreadyExists.get();

		String tagName = IMAGE_NAME + ":" + POM.getVersion();

		String imageId = DockerUtil.getImageIdByTagName(tagName);

		if (imageId == null || redeploy) {
			imageId = DockerUtil.deployImage(f, tagName);
			redeploy = true;
		}

		DockerClient docker = DockerUtil.getDockerClient();

		String containerId = DockerUtil.getContainerIdByName(docker, CONTAINER_NAME);

		if (containerId == null || redeploy) {
			containerId = DockerUtil.createContainer(docker, imageId, CONTAINER_NAME, //
					ServicesAddresses.DEFAULT_DOCKER_HTTP_PORT + ":" + PORT, //
					ServicesAddresses.DEFAULT_DOCKER_DEBUG_PORT + ":" + DEBUG_PORT);
		}

		if (DockerUtil.isContainerRunningById(docker, containerId))
			DockerUtil.stopContainer(docker, containerId);

		DockerUtil.startContainer(docker, containerId);
	}

	private static File createImage(File imageFileName) throws Exception {

		logger.info("Creating image " + IMAGE_NAME);

		File webappFile = null;

		try {

			File pomFile = POM.getMavenProject().getFile();

			List<DockerImageResource> resources = new LinkedList<DockerImageResource>();

			String webappDir = "webapp";
			String dbDir = "db";

			webappFile = new File(System.getProperty("java.io.tmpdir"), "tmp-docker-" + webappDir + "-" + System.currentTimeMillis());

			FileUtils.buildFolder(new File(pomFile.getParentFile(), "src/main/webapp"), webappFile);

			resources.add(new FileDockerImageResouce(webappDir, webappFile));

			resources.add(new ZipStreamDockerImageResouce(dbDir, new ZipInputStream(new FileInputStream(new File("db/ode.zip"))), dbDir));

			String[] jreArgs = new String[] { "-Xmx512m", "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=" + ServicesAddresses.DEFAULT_DOCKER_DEBUG_PORT };
			String[] appArgs = new String[] { //
					ServicesAddresses.DEFAULT_DOCKER_HTTP_HOST, String.valueOf(ServicesAddresses.DEFAULT_DOCKER_HTTP_PORT), HOSTNAME, String.valueOf(PORT), //
					webappDir //
			};

			InputStream imageStream = DockerUtil.createJavaApplicationImageFromPOM(pomFile.getAbsolutePath(), //
					jreArgs, MAIN_CLASS, appArgs, resources);

			try (InputStream in = imageStream) {
				try (FileOutputStream out = new FileOutputStream(imageFileName)) {
					IOUtils.copy(in, out);
				}
			}

			return imageFileName;

		} finally

		{
			if (webappFile != null)
				FileUtils.deleteDirectory(webappFile);
		}
	}

	private static File getImageFileName(Pom pom) {
		return new File("docker_images", "docker-" + IMAGE_NAME + "-" + pom.getVersion() + ".tar.gz");
	}

}
