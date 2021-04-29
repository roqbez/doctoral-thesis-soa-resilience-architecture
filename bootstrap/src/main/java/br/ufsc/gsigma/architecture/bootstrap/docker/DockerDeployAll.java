package br.ufsc.gsigma.architecture.bootstrap.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.infrastructure.MavenPOMLocation;
import br.ufsc.gsigma.infrastructure.util.maven.MavenUtil;

public abstract class DockerDeployAll {

	private static final Logger logger = LoggerFactory.getLogger(DockerDeployAll.class);

	public static void main(String[] args) throws Exception {

		try {

			MavenUtil.buildProject(MavenPOMLocation.POM_ARCHITECTURE);

			DockerUDDIServiceRegistry.deploy(true);
			DockerUDDIFederation.deploy(true);
			DockerCatalogService.deploy(true);
			DockerDiscoveryService.INSTANCE.deploy(true);
			DockerExecutionService.deploy(true);
			DockerBPELExporterService.deploy(true);
			DockerBindingService.deploy(true);
			DockerDeploymentService.deploy(true);
			DockerResilienceService.deploy(true);
			DockerUBLServices.INSTANCE.deploy(true);

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		} finally {
			System.exit(0);
		}
	}

}
