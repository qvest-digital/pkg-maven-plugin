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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * A bunch of method with often used functionality. There is nothing special
 * about them they just make other code more readable.
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 */
class Utils
{

  static void createParentDirs(File f, String item)
      throws MojoExecutionException
  {
    File p = f.getParentFile();
    if (!p.exists() && !p.mkdirs())
      throw new MojoExecutionException("Cannot create parent dirs for the "
                                       + item + " file.");
  }

  /** Creates a file and the file's parent directories.
   * 
   * @param f
   * @param item
   * @throws MojoExecutionException
   */
  static void createFile(File f, String item) throws MojoExecutionException
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
  static void makeExecutable(File f, String item) throws MojoExecutionException
  {
    Utils.exec(new String[] {"chmod",
                             "+x",
                             f.getAbsolutePath() },
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
  static void exec(String[] args, String failureMsg, String ioExceptionMsg)
  throws MojoExecutionException
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
  static void exec(String[] args, File workingDir, String failureMsg, String ioExceptionMsg)
  throws MojoExecutionException
  {
    /* debug code which prints out the execution command-line. Enable if neccessary.
    for(int i=0;i<args.length;i++)
      {
        System.err.print(args[i] + " ");
      }
    System.err.println();
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
        throw new MojoExecutionException(ioExceptionMsg,
                                         ioe);
      }
    catch (InterruptedException ie)
      {
        // Cannot happen.
      }
  }
  
  /**
   * Stores the contents of an input stream in a file. This
   * is used to copy a resource from the classpath.
   * 
   * @param is
   * @param file
   * @param ioExceptionMsg
   * @throws MojoExecutionException
   */
  static void storeInputStream(InputStream is, File file,
                              String ioExceptionMsg)
  throws MojoExecutionException
  {
    BufferedInputStream bis = new BufferedInputStream(is);
    
    try
    {
      FileOutputStream fos = new FileOutputStream(file);

      //    Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = bis.read(buf)) > 0) {
          fos.write(buf, 0, len);
      }
      
      bis.close();
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
  static void print(Process p)
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));

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
  
  
}
