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


package de.tarent.maven.plugins.pkg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * A bunch of method with often used functionality. There is nothing special
 * about them they just make other code more readable.
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 */
public class Utils
{
  private static final String STARTER_CLASS = "_Starter.class";
  
  /**
   * File filter that ignores files ending with "~", ".cvsignore" and CVS and SVN files.
   */
  public static final FileFilter FILTER = FileFilterUtils.makeSVNAware(FileFilterUtils.makeCVSAware(new NotFileFilter(new SuffixFileFilter(new String[] { "~", ".cvsignore" }))));

  public static void createParentDirs(File f, String item)
      throws MojoExecutionException
  {
    File p = f.getParentFile();
    if (! p.exists() && ! p.mkdirs())
      throw new MojoExecutionException("Cannot create parent dirs for the "
                                       + item + " .");
  }

  /**
   * Creates a file and the file's parent directories.
   * 
   * @param f
   * @param item
   * @throws MojoExecutionException
   */
  public static void createFile(File f, String item)
      throws MojoExecutionException
  {
    try
      {
        createParentDirs(f, item);

        f.createNewFile();
      }
    catch (IOException ioe)
      {
        throw new MojoExecutionException("IOException while creating " + item
                                         + " file.", ioe);
      }
  }

  /**
   * This wraps doing a "chmod +x" on a file.
   * 
   * @param f
   * @param item
   * @throws MojoExecutionException
   */
  public static void makeExecutable(File f, String item)
      throws MojoExecutionException
  {
    exec(new String[] { "chmod", "+x", f.getAbsolutePath() },
         "Changing the " + item + " file attributes failed.",
         "Unable to make " + item + " file executable.");

  }

  /**
   * A method which makes executing programs easier.
   * 
   * @param args
   * @param failureMsg
   * @param ioExceptionMsg
   * @throws MojoExecutionException
   */
  public static void exec(String[] args, String failureMsg,
                          String ioExceptionMsg) throws MojoExecutionException
  {
    exec(args, null, failureMsg, ioExceptionMsg);
  }

  /**
   * A method which makes executing programs easier.
   * 
   * @param args
   * @param failureMsg
   * @param ioExceptionMsg
   * @throws MojoExecutionException
   */
  public static void exec(String[] args, File workingDir, String failureMsg,
                          String ioExceptionMsg) throws MojoExecutionException
  {
    /*
     * debug code which prints out the execution command-line. Enable if
     * neccessary. for(int i=0;i<args.length;i++) { System.err.print(args[i] + "
     * "); } System.err.println();
     */

    try
      {
        Process p = Runtime.getRuntime().exec(args, null, workingDir);

        if (p.waitFor() != 0)
          {
            print(p);
            throw new MojoExecutionException(failureMsg);
          }
      }
    catch (IOException ioe)
      {
        throw new MojoExecutionException(ioExceptionMsg, ioe);
      }
    catch (InterruptedException ie)
      {
        // Cannot happen.
      }
  }

  /**
   * Stores the contents of an input stream in a file. This is used to copy a
   * resource from the classpath.
   * 
   * @param is
   * @param file
   * @param ioExceptionMsg
   * @throws MojoExecutionException
   */
  public static void storeInputStream(InputStream is, File file,
                                      String ioExceptionMsg)
      throws MojoExecutionException
  {
    if (is == null)
      throw new MojoExecutionException("InputStream must not be null.");
    
    try
      {
        FileOutputStream fos = new FileOutputStream(file);

        IOUtils.copy(is, fos);

        is.close();
        fos.close();
      }
    catch (IOException ioe)
      {
        throw new MojoExecutionException(ioExceptionMsg);
      }
  }

  /**
   * This method can be used to debug the output of processes.
   * 
   * @param p
   */
  public static void print(Process p)
  {
    BufferedReader br = new BufferedReader(
                                           new InputStreamReader(
                                                                 p.getErrorStream()));

    try
      {
        while (br.ready())
          {
            System.err.println("*** Process output ***");
            System.err.println(br.readLine());
            System.err.println("**********************");
          }
      }
    catch (IOException ioe)
      {
        // foo
      }

  }

  public static final void copyProjectArtifact(Log l, File src, File dst)
      throws MojoExecutionException
  {
    l.info("copying artifact: " + src.getAbsolutePath());
    l.info("destination: " + dst.getAbsolutePath());
    Utils.createFile(dst, "destination artifact");

    try
      {
        FileUtils.copyFile(src, dst);
      }
    catch (IOException ioe)
      {
        throw new MojoExecutionException(
                                         "IOException while copying artifact file.",
                                         ioe);
      }

  }

  /**
   * Convert the artifactId into a Debian package name which contains gcj
   * precompiled binaries.
   * 
   * @param artifactId
   * @return
   */
  public static String gcjise(String artifactId, String section,
                              boolean debianise)
  {
    return debianise && section.equals("libs") ? "lib" + artifactId + "-gcj"
                                              : artifactId + "-gcj";
  }

  /**
   * Concatenates two dependency lines. If both parts (prefix and suffix) are
   * non-empty the lines will be joined with a comma in between.
   * <p>
   * If one of the parts is empty the other will be returned.
   * </p>
   * 
   * @param prefix
   * @param suffix
   * @return
   */
  public static String joinDependencyLines(String prefix, String suffix)
  {
    return (prefix.length() == 0) ? suffix : (suffix.length() == 0) ? prefix
                                                                   : prefix
                                                                     + ", "
                                                                     + suffix;
  }

  /**
   * Copies the <code>AuxFile</code> instances contained within the set.
   * It takes the <code>srcAuxFilesDir</code> and <code>auxFileDstDir</code>
   * arguments into account to specify the parent source and destination
   * directory of the files.
   * 
   * By default files are copied into directories. If the <code>rename</code>
   * property of the <code>AuxFile</code> instance is set however the file is
   * copied and renamed to the last part of the path.
   * 
   * The return value is the amount of copied bytes.
   * 
   * @param l
   * @param srcAuxFilesDir
   * @param dstDir
   * @param auxFiles
   * @return
   * @throws MojoExecutionException
   */
  static long copyFiles(Log l,
                        File srcDir,
                        File dstDir,
                        List auxFiles,
                        String type)
      throws MojoExecutionException
  {
    long size = 0;
    
    Iterator ite = auxFiles.iterator();
    while (ite.hasNext())
      {
        AuxFile af = (AuxFile) ite.next();
        File from = new File(srcDir, af.from);
        File to = new File(dstDir, af.to);

        l.info("copying " + type + ": " + from.toString());
        l.info("destination: " + to.toString());

        if (! from.exists())
          throw new MojoExecutionException("File to copy does not exist: "
                                               + from.toString());
        
        createParentDirs(to, type);

        try
          {
            if (from.isDirectory())
              {
                to = new File(to, from.getName());
                FileUtils.copyDirectory(from, to, FILTER);
              }
            else if (af.isRename())
              {
                FileUtils.copyFile(from, to);
                size += from.length();
                
                if (af.isExecutable())
                  makeExecutable(to, to.getName());
              }
            else
              {
                FileUtils.copyFileToDirectory(from, to);
                size += from.length();

                if (af.isExecutable())
                  makeExecutable(to, to.getName());
              }
          }
        catch (IOException ioe)
          {
            throw new MojoExecutionException("IOException while copying " + type,
                                             ioe);
          }
      }
    
    return size;
  }

  /**
   * Convert the artifactId into a Debian package name. Currently this only
   * applies to libraries which get a "lib" prefix and a "-java" suffix.
   * 
   * @param artifactId
   * @return
   */
  public static String createPackageName(String artifactId, String section, boolean debianise)
  {
    return debianise && section.equals("libs")
                          ? "lib" + artifactId + "-java"
                          : artifactId;
  }

  /**
   * Copies the starter classfile to the starter path, prepares the classpath properties file
   * and stores it at that location, too.
   * 
   * @param dstStarterRoot
   * @param dependencies
   * @param libraryPrefix
   * @throws MojoExecutionException
   */
  public static void setupStarter(Log l, String mainClass, File dstStarterRoot, String classpath)
  throws MojoExecutionException
  {
    File destStarterClassFile = new File(dstStarterRoot, STARTER_CLASS);

    Utils.createFile(destStarterClassFile, "starter class");
    Utils.storeInputStream(Utils.class.getResourceAsStream("/" + STARTER_CLASS),
                           destStarterClassFile, "Unable to store starter class file in destination.");
    
    File destClasspathFile = new File(dstStarterRoot, "_classpath"); 
    Utils.createFile(destClasspathFile, "starter classpath");

    PrintWriter writer = null;
    try
      {
        writer = new PrintWriter(destClasspathFile);
        
        writer.println("# This file controls the application's classpath and is autogenerated.");
        writer.println("# Slashes (/) in the filenames are replaced with the platform-specific");
        writer.println("# separator char at runtime.");
        writer.println("# The next line is the fully-classified name of the main class:");
        writer.println(mainClass);
        writer.println("# The following lines are the classpath entries:");
  
        writer.println(classpath);
        
        l.info("created library entries");
      }
    catch (IOException e)
      {
        throw new MojoExecutionException("storing the classpath entries failed", e);
      }
    finally
      {
        if (writer != null)
            writer.close();
      }
  }
  
}
