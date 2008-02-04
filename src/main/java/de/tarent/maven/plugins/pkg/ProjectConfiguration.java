package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

public class ProjectConfiguration
{
  public File baseDir;
  
  public String artifactId;
  
  public String version;
  
  public File tempRoot;
  
  public File outputDirectory;
  
  public String finalName;
  
  public ArchiverManager archiverManager;
  
  public MavenProject project;
  
  public Artifact artifact;
  
  public List remoteRepos;
  
  public ArtifactRepository local;
  
  public ArtifactMetadataSource metadataSource;
  
  public ArtifactResolver resolver;
  
  public ArtifactFactory factory;

}
