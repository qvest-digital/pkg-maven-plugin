package de.tarent.maven.plugins.pkg.helper;

import java.util.Collections;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

public abstract class ArtifactInclusionStrategy {

	protected ArtifactInclusionStrategy() {
		// Intentionally do nothing.
	}

	public static ArtifactInclusionStrategy getStrategyInstance(String id) {
		if (id.equals("dependencies")) {
			return new IncludeDependenciesStrategy();
		} else if (id.equals("none")) {
			return new NoneStrategy();
		} else if (id.equals("project")) {
			return new IncludeProjectArtifactStrategy();
		} else {
			return new DefaultStrategy();
		}
	}

	public Result processArtifacts(Helper h) throws MojoExecutionException {
		Result result = new Result();
		result.resolvedDependencies = Collections.emptySet();
		result.byteAmount = 0L;

		processArtifactsImpl(h, result);

		return result;
	}

	protected abstract void processArtifactsImpl(Helper h, Result result)
			throws MojoExecutionException;

	public static final class Result {
		Set<Artifact> resolvedDependencies;
		long byteAmount;

		public Set<Artifact> getResolvedDependencies() {
			return resolvedDependencies;
		}

		public void setResolvedDependencies(Set<Artifact> resolvedDependencies) {
			this.resolvedDependencies = resolvedDependencies;
		}

		public long getByteAmount() {
			return byteAmount;
		}

		public void setByteAmount(long byteAmount) {
			this.byteAmount = byteAmount;
		}
	}

	static class DefaultStrategy extends ArtifactInclusionStrategy {

		@Override
		protected void processArtifactsImpl(Helper h, Result result)
				throws MojoExecutionException {
			result.byteAmount += h.copyProjectArtifact();
			result.resolvedDependencies = h.resolveProjectDependencies();
		}
	}

	static class NoneStrategy extends ArtifactInclusionStrategy {

		@Override
		protected void processArtifactsImpl(Helper h, Result result)
				throws MojoExecutionException {
			// Intentionally do nothing.
		}
	}

	static class IncludeProjectArtifactStrategy extends
			ArtifactInclusionStrategy {

		@Override
		protected void processArtifactsImpl(Helper h, Result result)
				throws MojoExecutionException {
			result.byteAmount += h.copyProjectArtifact();
		}
	}

	static class IncludeDependenciesStrategy extends ArtifactInclusionStrategy {

		@Override
		protected void processArtifactsImpl(Helper h, Result result)
				throws MojoExecutionException {
			result.resolvedDependencies = h.resolveProjectDependencies();
		}
	}
}
