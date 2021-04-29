package br.ufsc.gsigma.infrastructure.util.maven;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.Maven;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Repository;
import org.apache.maven.monitor.event.DefaultEventDispatcher;
import org.apache.maven.monitor.event.EventDispatcher;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.apache.maven.project.interpolation.ModelInterpolationException;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.RuntimeInfo;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.SettingsUtils;
import org.apache.maven.settings.TrackableBase;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.embed.Embedder;
import org.codehaus.plexus.interpolation.EnvarBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.Collections;

public class MavenUtil {

	private static final Logger logger = LoggerFactory.getLogger(MavenUtil.class);

	private static final String WILDCARD = "*";

	private static final String EXTERNAL_WILDCARD = "external:*";

	private static PlexusContainer container;

	private static LocalRepository localRepository;

	private static File userSettingsFile;

	private static File globalSettingsFile;

	private static Settings settings;

	public static void buildProject(String pomFile) throws Exception {
		buildProject(new File(pomFile));
	}

	public static void buildProject(File pomFile) throws Exception {

		MavenSettingsBuilder settingsBuilder = (MavenSettingsBuilder) lookup(MavenSettingsBuilder.ROLE);
		Settings settings = settingsBuilder.buildSettings();
		ArtifactRepository localRepo = createLocalArtifactRepository();

		EventDispatcher eventDispatcher = new DefaultEventDispatcher();

		File userDir = new File(System.getProperty("user.dir"));

		Properties executionProperties = new Properties();
		executionProperties.put("maven.test.skip", "true");

		Properties userProperties = new Properties();

		ProfileManager profileManager = new DefaultProfileManager(getContainer(), executionProperties);

		MavenExecutionRequest mavenExecutionRequest = new DefaultMavenExecutionRequest(localRepo, settings, eventDispatcher, Collections.singletonList("install"), userDir.getPath(), profileManager,
				executionProperties, userProperties, true);

		mavenExecutionRequest.setPomFile(pomFile.getAbsolutePath());

		Maven maven = (Maven) lookup(Maven.ROLE);

		maven.execute(mavenExecutionRequest);

	}

	public static File[] getClassPathFromMavenPOM(String pomFile) throws Exception {
		return getClassPathFromMavenPOM(new File(pomFile));
	}

	public static File[] getClassPathFromMavenPOM(File pomFile) throws Exception {
		return getClassPathFromMavenPOM(pomFile, false);
	}

	public static Pom getPOMFromFile(String pomFile) throws Exception {
		return getPOMFromFile(new File(pomFile));
	}

	public static Pom getPOMFromFile(File pomFile) throws Exception {
		return initializePom(createLocalArtifactRepository(), pomFile);
	}

	public static File[] getClassPathFromMavenPOM(File pomFile, boolean includeRootArtifact) throws Exception {
		Pom pom = initializePom(createLocalArtifactRepository(), pomFile);
		return getClassPathFromMavenPOM(pom, includeRootArtifact);
	}

	public static File[] getClassPathFromMavenPOM(Pom pom) throws Exception {
		return getClassPathFromMavenPOM(pom, false);
	}

	@SuppressWarnings({ "unchecked" })
	public static File[] getClassPathFromMavenPOM(Pom pom, boolean includeRootArtifact) throws Exception {

		ArtifactRepository localRepo = createLocalArtifactRepository();

		ArtifactResolver resolver = (ArtifactResolver) lookup(ArtifactResolver.ROLE);
		ArtifactFactory artifactFactory = (ArtifactFactory) lookup(ArtifactFactory.ROLE);
		MavenMetadataSource metadataSource = (MavenMetadataSource) lookup(ArtifactMetadataSource.ROLE);

		List<ArtifactRepository> remoteArtifactRepositories = createRemoteArtifactRepositories(pom.getRepositories());

		Set<Artifact> artifacts = MavenMetadataSource.createArtifacts(artifactFactory, pom.getDependencies(), null, null, null);

		Artifact pomArtifact = artifactFactory.createBuildArtifact(pom.getGroupId(), pom.getArtifactId(), pom.getVersion(), pom.getPackaging());

		Map<String, Artifact> managedDependencies = pom.getMavenProject().getManagedVersionMap();

		Collection<File> files = new LinkedList<File>();

		File buildPath = new File(pom.getMavenProject().getBuild().getOutputDirectory());

		if (buildPath.exists())
			files.add(buildPath);

		ArtifactResolutionResult result = resolver.resolveTransitively(artifacts, pomArtifact, managedDependencies, localRepo, remoteArtifactRepositories, metadataSource,
				new SpecificScopesArtifactFilter("runtime,compile"), new ArrayList<ResolutionListener>());

		for (Artifact artifact : (Collection<Artifact>) result.getArtifacts())
			if (artifact.getFile() != null && artifact.getFile().exists())
				files.add(artifact.getFile());

		if (includeRootArtifact) {
			resolver.resolve(pomArtifact, remoteArtifactRepositories, localRepo);

			if (pomArtifact.getFile() != null && pomArtifact.getFile().exists())
				files.add(pomArtifact.getFile());
		}

		if (logger.isDebugEnabled()) {
			for (File f : files)
				logger.debug("getClassPathFromMavenPOM(" + pom + ") -> " + f.getAbsolutePath());
		}

		return files.toArray(new File[files.size()]);

	}

	private static List<ArtifactRepository> createRemoteArtifactRepositories(List<Repository> pomRepositories) {
		List<RemoteRepository> remoteRepositories = new ArrayList<RemoteRepository>();

		// Add repositories configured in POM
		if (pomRepositories != null) {
			for (Repository pomRepository : pomRepositories) {
				remoteRepositories.add(createRemoteRepository(pomRepository));
			}
		}

		// Only add default repository if no repositories were configured
		// otherwise
		if (remoteRepositories.isEmpty()) {
			remoteRepositories.add(getDefaultRemoteRepository());
		}

		List<ArtifactRepository> list = new ArrayList<ArtifactRepository>();
		Set<String> ids = new HashSet<String>();
		for (RemoteRepository remoteRepository : remoteRepositories) {
			if (!ids.add(remoteRepository.getId())) {
				// repository id already added to the list: ignore it, since it
				// has been overridden
				continue;
			}
			updateRepositoryWithSettings(remoteRepository);

			list.add(createRemoteArtifactRepository(remoteRepository));
		}
		return list;
	}

	private static RemoteRepository createRemoteRepository(org.apache.maven.model.Repository pomRepository) {

		RemoteRepository r = createAntRemoteRepositoryBase(pomRepository);

		if (pomRepository.getSnapshots() != null) {
			r.addSnapshots(convertRepositoryPolicy(pomRepository.getSnapshots()));
		}
		if (pomRepository.getReleases() != null) {
			r.addReleases(convertRepositoryPolicy(pomRepository.getReleases()));
		}

		return r;
	}

	private static void updateRepositoryWithSettings(RemoteRepository repository) {

		Mirror mirror = getMirror(getSettings().getMirrors(), repository);
		if (mirror != null) {
			repository.setUrl(mirror.getUrl());
			repository.setId(mirror.getId());
		}

		if (repository.getAuthentication() == null) {
			Server server = getSettings().getServer(repository.getId());
			if (server != null) {
				repository.addAuthentication(new Authentication(server));
			}
		}

		if (repository.getProxy() == null) {
			org.apache.maven.settings.Proxy proxy = getSettings().getActiveProxy();
			if (proxy != null) {
				repository.addProxy(new Proxy(proxy));
			}
		}
	}

	private static ArtifactRepository createRemoteArtifactRepository(RemoteRepository repository) {
		ArtifactRepositoryLayout repositoryLayout = (ArtifactRepositoryLayout) lookup(ArtifactRepositoryLayout.ROLE, repository.getLayout());

		ArtifactRepositoryFactory repositoryFactory = null;

		ArtifactRepository artifactRepository;

		try {
			repositoryFactory = getArtifactRepositoryFactory(repository);

			ArtifactRepositoryPolicy snapshots = buildArtifactRepositoryPolicy(repository.getSnapshots());
			ArtifactRepositoryPolicy releases = buildArtifactRepositoryPolicy(repository.getReleases());

			artifactRepository = repositoryFactory.createArtifactRepository(repository.getId(), repository.getUrl(), repositoryLayout, snapshots, releases);
		} finally {
			releaseArtifactRepositoryFactory(repositoryFactory);
		}

		return artifactRepository;
	}

	private static ArtifactRepositoryPolicy buildArtifactRepositoryPolicy(RepositoryPolicy policy) {
		boolean enabled = true;
		String updatePolicy = null;
		String checksumPolicy = null;

		if (policy != null) {
			enabled = policy.isEnabled();
			if (policy.getUpdatePolicy() != null) {
				updatePolicy = policy.getUpdatePolicy();
			}
			if (policy.getChecksumPolicy() != null) {
				checksumPolicy = policy.getChecksumPolicy();
			}
		}

		return new ArtifactRepositoryPolicy(enabled, updatePolicy, checksumPolicy);
	}

	private static ArtifactRepositoryFactory getArtifactRepositoryFactory(RemoteRepository repository) {
		WagonManager manager = (WagonManager) lookup(WagonManager.ROLE);

		Authentication authentication = repository.getAuthentication();
		if (authentication != null) {
			manager.addAuthenticationInfo(repository.getId(), authentication.getUserName(), authentication.getPassword(), authentication.getPrivateKey(), authentication.getPassphrase());
		}

		Proxy proxy = repository.getProxy();
		if (proxy != null) {
			manager.addProxy(proxy.getType(), proxy.getHost(), proxy.getPort(), proxy.getUserName(), proxy.getPassword(), proxy.getNonProxyHosts());
		}

		return (ArtifactRepositoryFactory) lookup(ArtifactRepositoryFactory.ROLE);
	}

	private static void releaseArtifactRepositoryFactory(ArtifactRepositoryFactory repositoryFactory) {
		try {
			getContainer().release(repositoryFactory);
		} catch (ComponentLifecycleException e) {
			// TODO: Warn the user, or not?
		}
	}

	private static Mirror getMirror(List<Mirror> mirrors, RemoteRepository repository) {
		String repositoryId = repository.getId();

		if (repositoryId != null) {
			for (Mirror mirror : mirrors) {
				if (repositoryId.equals(mirror.getMirrorOf())) {
					return mirror;
				}
			}

			for (Mirror mirror : mirrors) {
				if (matchPattern(repository, mirror.getMirrorOf())) {
					return mirror;
				}
			}
		}

		return null;
	}

	private static boolean matchPattern(RemoteRepository originalRepository, String pattern) {
		boolean result = false;
		String originalId = originalRepository.getId();

		// simple checks first to short circuit processing below.
		if (WILDCARD.equals(pattern) || pattern.equals(originalId)) {
			result = true;
		} else {
			// process the list
			String[] repos = pattern.split(",");

			for (int i = 0; i < repos.length; i++) {
				String repo = repos[i];

				// see if this is a negative match
				if (repo.length() > 1 && repo.startsWith("!")) {
					if (originalId.equals(repo.substring(1))) {
						// explicitly exclude. Set result and stop processing.
						result = false;
						break;
					}
				}
				// check for exact match
				else if (originalId.equals(repo)) {
					result = true;
					break;
				}
				// check for external:*
				else if (EXTERNAL_WILDCARD.equals(repo) && isExternalRepo(originalRepository)) {
					result = true;
					// don't stop processing in case a future segment explicitly
					// excludes this repo
				} else if (WILDCARD.equals(repo)) {
					result = true;
					// don't stop processing in case a future segment explicitly
					// excludes this repo
				}
			}
		}
		return result;
	}

	private static boolean isExternalRepo(RemoteRepository originalRepository) {
		try {
			URL url = new URL(originalRepository.getUrl());
			return !(url.getHost().equals("localhost") || url.getHost().equals("127.0.0.1") || url.getProtocol().equals("file"));
		} catch (MalformedURLException e) {
			// bad url just skip it here. It should have been validated already,
			// but the wagon lookup will deal with it
			return false;
		}
	}

	private static RemoteRepository createAntRemoteRepositoryBase(org.apache.maven.model.RepositoryBase pomRepository) {

		RemoteRepository r = new RemoteRepository();
		r.setId(pomRepository.getId());
		r.setUrl(pomRepository.getUrl());
		r.setLayout(pomRepository.getLayout());

		return r;
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

	private static RepositoryPolicy convertRepositoryPolicy(org.apache.maven.model.RepositoryPolicy pomRepoPolicy) {
		RepositoryPolicy policy = new RepositoryPolicy();
		policy.setEnabled(pomRepoPolicy.isEnabled());
		policy.setUpdatePolicy(pomRepoPolicy.getUpdatePolicy());
		return policy;
	}

	private static ArtifactRepository createLocalArtifactRepository() {
		ArtifactRepositoryLayout repositoryLayout = (ArtifactRepositoryLayout) lookup(ArtifactRepositoryLayout.ROLE, getLocalRepository().getLayout());
		return new DefaultArtifactRepository("local", "file://" + getLocalRepository().getPath(), repositoryLayout);
	}

	private static LocalRepository getLocalRepository() {

		if (localRepository == null) {

			Settings settings = getSettings();

			localRepository = new LocalRepository();
			localRepository.setId("local");
			localRepository.setPath(new File(settings.getLocalRepository()));
		}
		return localRepository;
	}

	private static synchronized Settings getSettings() {
		if (settings == null) {
			initSettings();
		}
		return settings;
	}

	private static void initSettings() {
		if (userSettingsFile == null) {
			File tempSettingsFile = newFile(System.getProperty("user.home"), ".m2", "settings.xml");
			if (tempSettingsFile.exists())
				userSettingsFile = tempSettingsFile;
		}

		if (globalSettingsFile == null) {
			String m2Home = System.getenv("M2_HOME");

			if (m2Home != null) {
				File tempSettingsFile = newFile(m2Home, "conf", "settings.xml");
				if (tempSettingsFile.exists()) {
					globalSettingsFile = tempSettingsFile;
				}
			}
		}

		Settings userSettings = loadSettings(userSettingsFile);
		Settings globalSettings = loadSettings(globalSettingsFile);

		SettingsUtils.merge(userSettings, globalSettings, TrackableBase.GLOBAL_LEVEL);
		settings = userSettings;

		if (StringUtils.isEmpty(settings.getLocalRepository())) {
			String location = newFile(System.getProperty("user.home"), ".m2", "repository").getAbsolutePath();
			settings.setLocalRepository(location);
		}

		WagonManager wagonManager = (WagonManager) lookup(WagonManager.ROLE);
		// wagonManager.setDownloadMonitor(new AntDownloadMonitor());
		if (settings.isOffline()) {
			logger.info("You are working in offline mode.");
			wagonManager.setOnline(false);
		} else {
			wagonManager.setOnline(true);
		}
	}

	private static Settings loadSettings(File settingsFile) {
		Settings settings = null;
		try {
			if (settingsFile != null) {
				logger.info("Loading Maven settings file: " + settingsFile.getPath());
				settings = readSettings(settingsFile);
			}
		} catch (IOException e) {
			logger.error("Error reading settings file '" + settingsFile + "' - ignoring. Error was: " + e.getMessage());
		} catch (XmlPullParserException e) {
			logger.error("Error parsing settings file '" + settingsFile + "' - ignoring. Error was: " + e.getMessage());
		}

		if (settings == null) {
			settings = new Settings();
			RuntimeInfo rtInfo = new RuntimeInfo(settings);
			settings.setRuntimeInfo(rtInfo);
		}

		return settings;
	}

	private static Settings readSettings(File settingsFile) throws IOException, XmlPullParserException {
		Settings settings = null;
		Reader reader = null;
		try {
			reader = ReaderFactory.newXmlReader(settingsFile);
			StringWriter sWriter = new StringWriter();

			IOUtil.copy(reader, sWriter);

			String rawInput = sWriter.toString();

			try {
				RegexBasedInterpolator interpolator = new RegexBasedInterpolator();
				interpolator.addValueSource(new EnvarBasedValueSource());

				rawInput = interpolator.interpolate(rawInput, "settings");
			} catch (Exception e) {
				logger.warn("Failed to initialize environment variable resolver. Skipping environment substitution in " + "settings.");
			}

			StringReader sReader = new StringReader(rawInput);

			SettingsXpp3Reader modelReader = new SettingsXpp3Reader();

			settings = modelReader.read(sReader);

			RuntimeInfo rtInfo = new RuntimeInfo(settings);

			rtInfo.setFile(settingsFile);

			settings.setRuntimeInfo(rtInfo);
		} finally {
			IOUtil.close(reader);
		}
		return settings;
	}

	private static Object lookup(String role) {
		try {
			return getContainer().lookup(role);
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to find component: " + role, e);
		}
	}

	private static Object lookup(String role, String roleHint) {
		try {
			return getContainer().lookup(role, roleHint);
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to find component: " + role + "[" + roleHint + "]", e);
		}
	}

	private static synchronized PlexusContainer getContainer() {

		if (container == null) {
			try {
				ClassWorld classWorld = new ClassWorld();

				classWorld.newRealm("plexus.core", MavenUtil.class.getClassLoader());

				Embedder embedder = new Embedder();

				embedder.start(classWorld);

				container = embedder.getContainer();
			} catch (PlexusContainerException e) {
				throw new RuntimeException("Unable to start embedder", e);
			} catch (DuplicateRealmException e) {
				throw new RuntimeException("Unable to create embedder ClassRealm", e);
			}
		}

		return container;
	}

	private static Pom initializePom(ArtifactRepository localArtifactRepository, File pomFile) throws ProjectBuildingException, ModelInterpolationException, ComponentLookupException {

		Pom pom = new Pom(getContainer(), getSettings(), pomFile);

		MavenProjectBuilder projectBuilder = (MavenProjectBuilder) lookup(MavenProjectBuilder.ROLE);
		pom.initialiseMavenProject(projectBuilder, localArtifactRepository);

		return pom;
	}

	private static File newFile(String parent, String subdir, String filename) {
		return new File(new File(parent, subdir), filename);
	}

}
