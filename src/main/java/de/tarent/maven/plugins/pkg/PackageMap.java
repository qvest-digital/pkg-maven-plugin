package de.tarent.maven.plugins.pkg;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

class PackageMap
{
  
  private static final Entry BUNDLE_ENTRY = new Entry();
  
  private static final Entry IGNORE_ENTRY = new Entry();
  
  private Mapping mapping;
  
  PackageMap(URL packageMapURL, URL auxPackageMapURL, String distribution, HashSet bundleOverrides)
    throws MojoExecutionException
  {
    if (packageMapURL == null)
      packageMapURL = PackageMap.class.getResource("default-package-maps.xml");
    
    try
    {
      mapping = new Parser(packageMapURL,
                           auxPackageMapURL,
                           bundleOverrides).getMapping(distribution);
    }
    catch (ParseException pe)
    {
      throw new MojoExecutionException("Package map creation failed", pe);
    }
    
  }

  String getDefaultDependencyLine()
  {
    return (mapping.defaultDependencyLine != null ? mapping.defaultDependencyLine : "java2-runtime");
  }
  
  String getDefaultJarPath()
  {
    return (mapping.defaultJarPath != null ? mapping.defaultJarPath : "/usr/share/java");
  }

  String getDefaultJNIPath()
  {
    return (mapping.defaultJNIPath != null ? mapping.defaultJNIPath : "/usr/lib/jni");
  }
  
  String getDefaultBinPath()
  {
    return (mapping.defaultBinPath != null ? mapping.defaultBinPath : "/usr/bin");
  }

  void iterateDependencyArtifacts(Collection deps, Visitor v, boolean bundleNonExisting)
  {
   for (Iterator ite = deps.iterator(); ite.hasNext(); )
     {
       Artifact a = (Artifact) ite.next();
       
       Entry e = (Entry) mapping.getEntry(a.getArtifactId());
       if (e == BUNDLE_ENTRY)
           v.bundle(a);
       else if (e == null)
         {
           if (bundleNonExisting)
             v.bundle(a);
         }
       else if (e != IGNORE_ENTRY)
         v.visit(a, e);
     }
     
  }
  
  Entry getEntry(String artifactId, String debianSection)
  {
    Entry e = (Entry) mapping.getEntry(artifactId);
    
    // If an entry does not exist create one based on the artifact id.
    if (e == null)
      {
        //e = mapping.createDefaultEntry(artifactId)
        return null;
      }
    
    return e;
  }
  
  /**
   * Convert the artifactId into a Debian package name. Currently this only
   * applies to libraries which get a "lib" prefix and a "-java" suffix.
   * 
   * @param artifactId
   * @return
   */
  static String debianise(String artifactId, String debianSection)
  {
    return debianSection.equals("libs") ? "lib" + artifactId + "-java"
                                       : artifactId;
  }
  
  static class Entry
  {
    String artifactId;
    
    String packageName;
    
    HashSet jarFileNames;
    
    boolean isBootClasspath;

    private Entry(){
      // Special constructor for internal instances.
    }

    Entry(String artifactId, String packageName, HashSet jarFileNames, boolean isBootClasspath)
    {
      this.artifactId = artifactId;
      this.packageName = packageName;
      this.jarFileNames = jarFileNames;
      this.isBootClasspath = isBootClasspath;
    }
  }
  
  interface Visitor
  {
    public void visit(Artifact artifact, Entry entry);
    
    public void bundle(Artifact artifact);
  }
  
  private static class Mapping
  {
    String distro;
    
    String label;
    
    String parent;
    
    String packaging;
    
    String defaultBinPath;
    
    String defaultJarPath;

    String defaultJNIPath;
    
    String defaultDependencyLine;
    
    HashMap/*<String, Entry>*/ entries = new HashMap();
    
    Mapping(String distro)
    {
      label = this.distro = distro;
    }
    
    Entry getEntry(String artifactId)
    {
      return (Entry) entries.get(artifactId);
    }
    
    void putEntry (String artifactId, Entry e)
    {
      entries.put(artifactId, e);
    }
    
    Mapping merge(Mapping parent)
    {
      Mapping m = new Mapping(distro);
      m.label = label;
      m.packaging = parent.packaging;
      
      // These values may be null. If the merging has been done from the root to the child
      // they will be non-null for the parent however.
      m.defaultJarPath = (defaultJarPath != null) ? defaultJarPath : parent.defaultJarPath; 
      m.defaultBinPath = (defaultBinPath != null) ? defaultBinPath : parent.defaultBinPath; 
      m.defaultJNIPath = (defaultJNIPath != null) ? defaultJNIPath : parent.defaultJNIPath; 
      m.defaultDependencyLine = (defaultDependencyLine != null) ? defaultDependencyLine : parent.defaultDependencyLine;
      
      m.entries = (HashMap) parent.entries.clone();
      
      m.entries.putAll(entries);
      
      return m;
    }
  }
  
  private static class Parser
  {
    HashSet overrides;
    
    HashMap/*<String, Mapping>*/ mappings = new HashMap();
    
    Parser(URL packageMapDocument, URL auxMapDocument, HashSet bundleOverrides)
      throws ParseException
    {
      this.overrides = bundleOverrides;
      
      State s = new State(packageMapDocument);
      
      s.nextMatch("package-maps");
      parsePackageMaps(s);
      
      if (auxMapDocument != null)
        {
          s = new State(packageMapDocument);
          
          s.nextMatch("package-maps");
          parsePackageMaps(s);
        }
      
    }
    
    private void handleInclude(State currentState, String includeUrl) throws ParseException
    {
      try
      {
        // Automagically handles relative and absolute URLs.
        URL url = new URL(currentState.url, includeUrl);
        State s = new State(url);
        
        s.nextMatch("package-maps");
        parsePackageMaps(s);
        
      }
      catch (MalformedURLException mfue)
      {
        throw new ParseException("URL in <include> tag is invalid '" + includeUrl + "'");
      }
    }
    
    private void parsePackageMaps(State s) throws ParseException
    {
      s.nextMatch("version");
      
      String vc = s.nextElement();
      if (Double.parseDouble(vc) != 1.0)
        throw new ParseException("unsupported document: document version " + vc + " is not supported");
     
      s.nextElement();
      while (s.token != null)
        {
          if (s.peek("distro"))
          {
            parseDistro(s);
          }
          else if (s.peek("include"))
            {
              handleInclude(s, s.nextElement());
              s.nextElement();
            }
          else
            throw new ParseException("malformed document: unexpected token " + s.token);
        }
      
    }
    
    private void parseDistro(State s) throws ParseException
    {
      s.nextMatch("id");
      Mapping distroMapping = getMappingImpl(s.nextElement());
      s.nextMatch("label");
      distroMapping.label = s.nextElement();
      
      s.nextElement();
      // Either "inherit" or "packaging".
      if (s.peek("inherit"))
        distroMapping.parent = s.nextElement();
      else if(s.peek("packaging"))
        distroMapping.packaging = s.nextElement();
      else 
        throw new ParseException("malformed document: unexpected token " + s.token);
      
      s.nextElement();
      // Default bin (scripts) path is optional
      if (s.peek("defaultBinPath"))
        {
          distroMapping.defaultBinPath = s.nextElement();
          s.nextElement();
        }
      
      // Default jar path is optional
      if (s.peek("defaultJarPath"))
        {
          distroMapping.defaultJarPath = s.nextElement();
          s.nextElement();
        }

      // Default JNI path is optional
      if (s.peek("defaultJNIPath"))
        {
          distroMapping.defaultJNIPath = s.nextElement();
          s.nextElement();
        }
      
      // Default dependency line is optional
      if (s.peek("defaultDependencyLine"))
        {
          distroMapping.defaultDependencyLine = s.nextElement();
          s.nextElement();
        }
      
      if (s.peek("map"))
        {
          parseMap(s, distroMapping);
        }
      
    }
    
    private void parseMap(State s, Mapping distroMapping) throws ParseException
    {
      s.nextElement();
      while (s.peek("entry"))
        {
          parseEntry(s, distroMapping);
        }
    }

    private void parseEntry(State s, Mapping distroMapping) throws ParseException
    {
      String artifactId;
      String dependencyLine;
      
      s.nextMatch("artifactId");
      dependencyLine = artifactId = s.nextElement();
      
      s.nextElement();
      if (s.peek("ignore"))
        {
          distroMapping.putEntry(artifactId, IGNORE_ENTRY);
          s.nextElement();
          return;
        }
      else if (s.peek("bundle"))
        {
          distroMapping.putEntry(artifactId, BUNDLE_ENTRY);
          s.nextElement();
          return;
        }
      else if (s.peek("dependencyLine"))
        {
          dependencyLine = s.nextElement();
          s.nextElement();
        }
      
      boolean isBootClaspath = false;
      if (s.peek("boot"))
        {
          isBootClaspath = true;
          s.nextElement();
        }
      
      HashSet jarFileNames = new HashSet();
      if (s.peek("jars"))
        {
          parseJars(s, jarFileNames);
        }
      
      distroMapping.putEntry(artifactId, new Entry(artifactId, dependencyLine, jarFileNames, isBootClaspath));
    }

    private void parseJars(State s, HashSet jarFileNames) throws ParseException
    {
      s.nextElement();
      while (s.peek("jar"))
        {
          jarFileNames.add(s.nextElement());
          s.nextElement();
        }
    }

    Mapping getMapping(String distro)
    {
      Mapping m = (Mapping) mappings.get(distro);
      
      if (m == null)
        mappings.put(distro, m = new Mapping(distro));
      else if (m.parent != null)
        {
          return m.merge(getMapping(m.parent));
        }
      
      return m;
    }
    
    Mapping getMappingImpl(String distro)
    {
      Mapping m = (Mapping) mappings.get(distro);
      
      if (m == null)
        mappings.put(distro, m = new Mapping(distro));
      
      return m;
    }
    
    static class State
    {
      String token;
      
      XmlPullParser parser;
      
      URL url;
      
      State(URL url) throws ParseException
      {
        parser = new MXParser();
        
        this.url = url;
        
        try
        {
          parser.setInput(url.openStream(), null);
        }
        catch (XmlPullParserException xmlppe)
        {
          throw new ParseException("XML document malformed");
        }
        catch (IOException ioe)
        {
          throw new ParseException("I/O error when accessing XML document: " + url);
        }

      }
      
      String nextElement() throws ParseException
      {
        do
          {
            try 
            {
              switch (parser.next())
              {
                case XmlPullParser.START_TAG:
//                  System.err.println("start: " + parser.getName());
                  return token = parser.getName();
                case XmlPullParser.END_TAG:
//                  System.err.println("end");
                  continue;
                case XmlPullParser.END_DOCUMENT:
                  return token = null;
                case XmlPullParser.TEXT:
                  // We don't care about whitespace characters.
                  if (parser.isWhitespace())
                    continue;
                  
//                  System.err.println("text: " + parser.getText());
                  
                  return token = parser.getText();
              }
            }
            catch (XmlPullParserException xmlppe)
            {
              throw new ParseException("XML document malformed");
            }
            catch (IOException ioe)
            {
              throw new ParseException("I/O error when accessing XML document: " + url);
            }
          }
        while (true);
      }

      private void nextMatch(String expected) throws ParseException
      {
        nextElement();
        if (!expected.equals(token))
          throw new ParseException("malformed document: expected " + expected + " got '" + token + "'");
      }
      
      private void match(String expected) throws ParseException
      {
        if (!expected.equals(token))
          throw new ParseException("malformed document: expected " + expected + " got '" + token + "'");
      }

      private boolean peek(String expected)
      {
        return expected.equals(token);
      }
      
    }
  }
  
  static class ParseException extends Exception
  {
    private static final long serialVersionUID = - 8872495978331881464L;

    ParseException(String msg, Throwable cause)
    {
      super(msg, cause);
    }

    ParseException(String msg)
    {
      super(msg);
    }
    
  }
  
}
