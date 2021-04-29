package br.ufsc.gsigma.architecture.bootstrap.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.gsigma.infrastructure.MavenPOMLocation;
import br.ufsc.gsigma.infrastructure.util.maven.MavenUtil;

public abstract class DockerBuildAll {

	private static final Logger logger = LoggerFactory.getLogger(DockerBuildAll.class);

	public static void main(String[] args) throws Exception {

		try {

			MavenUtil.buildProject(MavenPOMLocation.POM_ARCHITECTURE);

			DockerUDDIServiceRegistry.createImageFile();
			DockerUDDIFederation.createImageFile();
			DockerCatalogService.createImageFile();
			DockerDiscoveryService.INSTANCE.createImageFile();
			DockerExecutionService.createImageFile();
			DockerBPELExporterService.createImageFile();
			DockerBindingService.createImageFile();
			DockerDeploymentService.createImageFile();
			DockerResilienceService.createImageFile();
			DockerUBLServices.INSTANCE.createImageFile();

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		} finally {
			System.exit(0);
		}
	}

}
