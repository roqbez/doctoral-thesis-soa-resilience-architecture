package br.ufsc.gsigma.infrastructure.util.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

public class SpecificScopesArtifactFilter implements ArtifactFilter {
	private boolean compileScope;

	private boolean runtimeScope;

	private boolean testScope;

	private boolean providedScope;

	private boolean systemScope;

	/**
	 * Takes a comma separated list of scopes to include.
	 * 
	 * @param scopes
	 *            A comma separated list of scopes
	 */
	public SpecificScopesArtifactFilter(String scopes) {
		String[] scopeList = scopes.split(",");

		for (int i = 0; i < scopeList.length; ++i) {
			String scope = scopeList[i].trim();

			if (scope.equals(DefaultArtifact.SCOPE_COMPILE)) {
				compileScope = true;
			} else if (scope.equals(DefaultArtifact.SCOPE_PROVIDED)) {
				providedScope = true;
			} else if (scope.equals(DefaultArtifact.SCOPE_RUNTIME)) {
				runtimeScope = true;
			} else if (scope.equals(DefaultArtifact.SCOPE_SYSTEM)) {
				systemScope = true;
			} else if (scope.equals(DefaultArtifact.SCOPE_TEST)) {
				testScope = true;
			}
		}
	}

	public boolean include(Artifact artifact) {
		if (Artifact.SCOPE_COMPILE.equals(artifact.getScope())) {
			return compileScope;
		} else if (Artifact.SCOPE_RUNTIME.equals(artifact.getScope())) {
			return runtimeScope;
		} else if (Artifact.SCOPE_TEST.equals(artifact.getScope())) {
			return testScope;
		} else if (Artifact.SCOPE_PROVIDED.equals(artifact.getScope())) {
			return providedScope;
		} else if (Artifact.SCOPE_SYSTEM.equals(artifact.getScope())) {
			return systemScope;
		}
		return true;
	}
}
