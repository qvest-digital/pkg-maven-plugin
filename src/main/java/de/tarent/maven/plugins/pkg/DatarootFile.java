/*
 * Maven Packaging Plugin,
 * Maven plugin to package a Project (deb, ipk, izpack)
 * Copyright (C) 2000-2008 tarent GmbH
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
 * Signature of Elmar Geese, 11 March 2008
 * Elmar Geese, CEO tarent GmbH.
 */

package de.tarent.maven.plugins.pkg;

/**
 * This class is used by Maven2 to fill the datarootfiles entries.
 * 
 * <p>
 * It exists just to please Maven2 automatic type mapping. Technically we are
 * only interested that this type is compatible to <code>AuxFile</code> to let
 * common utility methods work on it nicely.
 * </p>
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 * 
 */
public class DatarootFile extends AuxFile {
}
