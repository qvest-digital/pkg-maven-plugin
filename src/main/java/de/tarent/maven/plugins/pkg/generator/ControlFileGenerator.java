/*
 * Maven Packaging Plugin,
 * Maven plugin to package a Project (deb and izpack)
 * Copyright (C) 2000-2007 tarent GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License,version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * tarent GmbH., hereby disclaims all copyright
 * interest in the program 'Maven Packaging Plugin'
 * Signature of Elmar Geese, 14 June 2007
 * Elmar Geese, CEO tarent GmbH.
 */

package de.tarent.maven.plugins.pkg.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/** Simple generator for Debian- or IPK-style control files.
 * 
 * @author Robert Schuster (r.schuster@tarent.de)
 *
 */
public class ControlFileGenerator
{
  
  private String packageName;
  
  private String version;
  
  private String section;
  
  private String dependencies;
  
  private long installedSize;
  
  private String maintainer;
  
  private String shortDescription;
  
  private String description;
  
  private String architecture;
  
  private String oe;
  
  private String homepage;
  
  private String source;
  
  public ControlFileGenerator()
  {
    
  }
  
  public String getOE()
  {
    return oe;
  }
  
  public void setOE(String newOE)
  {
    oe = newOE;
  }

  public String getArchitecture()
  {
    return architecture;
  }

  public void setArchitecture(String architecture)
  {
    this.architecture = architecture;
  }

  public String getDependencies()
  {
    return dependencies;
  }

  public void setDependencies(String dependencies)
  {
    this.dependencies = dependencies;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public long getInstalledSize()
  {
    return installedSize;
  }

  public void setInstalledSize(long installedSize)
  {
    this.installedSize = installedSize;
  }

  public String getMaintainer()
  {
    return maintainer;
  }

  public void setMaintainer(String maintainer)
  {
    this.maintainer = maintainer;
  }

  public String getPackageName()
  {
    return packageName;
  }

  public void setPackageName(String packageName)
  {
    this.packageName = packageName;
  }

  public String getSection()
  {
    return section;
  }

  public void setSection(String section)
  {
    this.section = section;
  }

  public String getShortDescription()
  {
    return shortDescription;
  }

  public void setShortDescription(String shortDescription)
  {
    this.shortDescription = shortDescription;
  }

  public String getVersion()
  {
    return version;
  }

  public void setVersion(String version)
  {
    this.version = version;
  }
  public String getHomepage()
  {
    return homepage;
  }

  public void setHomepage(String homepage)
  {
    this.homepage = homepage;
  }

  public String getSource()
  {
    return source;
  }

  public void setSource(String source)
  {
    this.source = source;
  }

  public void generate(File f) throws IOException
  {
    PrintWriter w = new PrintWriter(new FileOutputStream(f));
    
    writeEntry(w, "Package", packageName);
    writeEntry(w, "Version", version);
    writeEntry(w, "Section", section);
    writeEntry(w, "Depends", dependencies);
    writeEntry(w, "Priority", "optional");
    writeEntry(w, "Architecture", architecture);
    writeEntry(w, "OE", oe);
    writeEntry(w, "Homepage", homepage);
    writeEntry(w, "Installed-Size", installedSize);
    writeEntry(w, "Maintainer", maintainer);
    writeEntry(w, "Description", shortDescription);
    writeEntry(w, "Source", source);
    
    if (description != null)
      w.println(" " + description);
    
    w.close();
  }
  
  private void writeEntry(PrintWriter w, String name, String value)
  {
    if (value != null)
      w.println(name + ": " +  value);
  }

  private void writeEntry(PrintWriter w, String name, long value)
  {
    if (value != 0)
      w.println(name + ": " +  value);
  }
  
}
