package br.ufsc.gsigma.architecture.bootstrap.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.infrastructure.MavenPOMLocation;
import br.ufsc.gsigma.infrastructure.util.maven.MavenUtil;

public abstract class DockerDeployIfNecessary {

	private static final Logger logger = LoggerFactory.getLogger(DockerDeployIfNecessary.class);

	public static void main(String[] args) throws Exception {

		try {

			MavenUtil.buildProject(MavenPOMLocation.POM_ARCHITECTURE);

			DockerUDDIServiceRegistry.deploy(false);
			DockerUDDIFederation.deploy(false);
			DockerCatalogService.deploy(false);
			DockerDiscoveryService.INSTANCE.deploy(false);
			DockerExecutionService.deploy(false);
			DockerBPELExporterService.deploy(false);
			DockerBindingService.deploy(false);
			DockerDeploymentService.deploy(false);
			DockerUBLServices.INSTANCE.deploy(false);

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		} finally {
			System.exit(0);
		}
	}

}
