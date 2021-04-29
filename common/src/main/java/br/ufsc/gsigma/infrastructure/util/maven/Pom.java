package br.ufsc.gsigma.infrastructure.util.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Repository;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.injection.ModelDefaultsInjector;
import org.apache.maven.project.interpolation.ModelInterpolationException;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public class Pom {

	private File file;

	private ProfileManager profileManager;

	private PlexusContainer container;

	private Settings settings;

	private MavenProject mavenProject;

	public Pom(PlexusContainer container, Settings settings, File file) {
		this.container = container;
		this.settings = settings;
		this.file = file;
	}

	public MavenProject getMavenProject() {
		return mavenProject;
	}

	public String getGroupId() {
		return mavenProject.getGroupId();
	}

	public String getArtifactId() {
		return mavenProject.getArtifactId();
	}

	public String getVersion() {
		return mavenProject.getVersion();
	}

	public String getPackaging() {
		return mavenProject.getPackaging();
	}

	public void initialiseMavenProject(MavenProjectBuilder builder, ArtifactRepository localRepository) throws ProjectBuildingException,
			ModelInterpolationException, ComponentLookupException {

		if (file != null) {
			addRepositoriesToProfileManager();
			ProjectBuilderConfiguration builderConfig = this.createProjectBuilderConfig(localRepository);
			mavenProject = builder.build(file, builderConfig);

			builder.calculateConcreteState(mavenProject, builderConfig, false);
		}

		ModelDefaultsInjector modelDefaultsInjector = (ModelDefaultsInjector) container.lookup(ModelDefaultsInjector.ROLE);

		modelDefaultsInjector.injectDefaults(mavenProject.getModel());

	}

	@SuppressWarnings("unchecked")
	public List<Dependency> getDependencies() {
		return mavenProject.getDependencies();
	}

	@SuppressWarnings("unchecked")
	public List<Repository> getRepositories() {
		return mavenProject.getRepositories();
	}

	private static RemoteRepository getDefaultRemoteRepository() {
		// TODO: could we utilize the super POM for this?
		RemoteRepository remoteRepository = new RemoteRepository();
		remoteRepository.setId("central");
		remoteRepository.setUrl("http://repo1.maven.org/maven2");
		RepositoryPolicy snapshots = new RepositoryPolicy();
		snapshots.setEnabled(false);
		remoteRepository.addSnapshots(snapshots);
		return remoteRepository;
	}

	private void addRepositoriesToProfileManager() {

		List<RemoteRepository> remoteRepositories = new ArrayList<RemoteRepository>();
		remoteRepositories.add(getDefaultRemoteRepository());

		org.apache.maven.model.Profile repositoriesProfile = new org.apache.maven.model.Profile();
		repositoriesProfile.setId("maven-ant-tasks-repo-profile");

		for (RemoteRepository antRepo : remoteRepositories) {
			Repository mavenRepo = new Repository();
			mavenRepo.setId(antRepo.getId());
			mavenRepo.setUrl(antRepo.getUrl());
			repositoriesProfile.addRepository(mavenRepo);
		}

		getProfileManager().addProfile(repositoriesProfile);
		getProfileManager().explicitlyActivate(repositoriesProfile.getId());
	}

	protected ProfileManager getProfileManager() {
		if (profileManager == null) {
			profileManager = new DefaultProfileManager(container, settings, System.getProperties());
		}
		return profileManager;
	}

	private ProjectBuilderConfiguration createProjectBuilderConfig(ArtifactRepository localArtifactRepository) {
		ProjectBuilderConfiguration builderConfig = new DefaultProjectBuilderConfiguration();
		builderConfig.setLocalRepository(localArtifactRepository);
		builderConfig.setGlobalProfileManager(getProfileManager());

		// builderConfig.setUserProperties(getAntProjectProperties());
		// builderConfig.setExecutionProperties(getAntProjectProperties());

		return builderConfig;
	}

}
