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
 * Data type for the elements of the 'auxFiles' property of the
 * {@link TargetConfiguration} class.
 * 
 * <p>
 * An instance of this class denotes a file or directory that has to be copied
 * into the package. By default the file or directory is copied into the
 * directory specified by the <code>to</code> property. If the
 * <code>rename</code> property is set to <code>true</code> the source file will
 * be copied into the parent directory specified by <code>to</code> an renamed
 * according to the file name part of that property.
 * </p>
 * 
 * <p>
 * If the executable bit is set the file will be flagged as exectuable after
 * copying. This property is only used for file copies not (yet) for
 * directories.
 * </p>
 * 
 * <p>
 * The <code>to</code> property is optional. If it is not set it will mean
 * copying the file into the package's base director. For IzPack this means the
 * application's own directory.
 * </p>
 * 
 * <p>
 * This class is automatically picked up by Maven. Property accessors exist for
 * the sake of Java Bean compatibility. There is nothing special involved into
 * them and the field can be accessed directly.
 * </p>
 * 
 * @author Robert Schuster (robert.schuster@tarent.de)
 * 
 */
public class AuxFile {

	String from;

	String to = "";

	boolean rename;

	boolean executable;
	private String owner;
	private String group;
	private boolean userRead;
	private boolean userWrite;
	private boolean userExecute;
	private boolean groupRead;
	private boolean groupWrite;
	private boolean groupExecute;
	private boolean othersRead;
	private boolean othersWrite;
	private boolean othersExecute;

	public AuxFile(){
		
	}
	
	public AuxFile(String to){
		this.to = to;
	}
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public boolean isExecutable() {
		return executable;
	}

	public void setExecutable(boolean executable) {
		this.executable = executable;
	}

	public boolean isRename() {
		return rename;
	}

	public void setRename(boolean rename) {
		this.rename = rename;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String toString() {
		return from;
	}
	
	/**
	 * Returns the permissions for the file in unix format
	 * as an int (there is possibly a much better way to do this).
	 *  
	 * @return
	 */
	public int getOctalPermission() {
		short total = 0;
				
		if(userRead){
			total+=400;
		}
		if(userWrite){
			total+=200;
		}
		if(userExecute){
			total+=100;
		}
		if(groupRead){
			total+=40;
		}
		if(groupWrite){
			total+=20;
		}
		if(groupExecute){
			total+=10;
		}
		if(othersRead){
			total+=4;
		}
		if(othersWrite){
			total+=2;
		}
		if(othersExecute){
			total+=1;
		}
		return total;
	}
	public String getOwner() {
		
		return owner==null? "root":owner;
	}


	public void setOwner(String owner) {
		this.owner = owner;
	}


	public String getGroup() {
		return group==null? "root":group;
	}


	public void setGroup(String group) {
		this.group = group;
	}


	public boolean isUserRead() {
		return userRead;
	}


	public void setUserRead(boolean userRead) {
		this.userRead = userRead;
	}


	public boolean isUserWrite() {
		return userWrite;
	}


	public void setUserWrite(boolean userWrite) {
		this.userWrite = userWrite;
	}


	public boolean isUserExecute() {
		return userExecute;
	}


	public void setUserExecute(boolean userExecute) {
		this.userExecute = userExecute;
	}


	public boolean isGroupRead() {
		return groupRead;
	}


	public void setGroupRead(boolean groupRead) {
		this.groupRead = groupRead;
	}


	public boolean isGroupWrite() {
		return groupWrite;
	}


	public void setGroupWrite(boolean groupWrite) {
		this.groupWrite = groupWrite;
	}


	public boolean isGroupExecute() {
		return groupExecute;
	}


	public void setGroupExecute(boolean groupExecute) {
		this.groupExecute = groupExecute;
	}


	public boolean isOthersRead() {
		return othersRead;
	}


	public void setOthersRead(boolean othersRead) {
		this.othersRead = othersRead;
	}


	public boolean isOthersWrite() {
		return othersWrite;
	}


	public void setOthersWrite(boolean othersWrite) {
		this.othersWrite = othersWrite;
	}


	public boolean isOthersExecute() {
		return othersExecute;
	}


	public void setOthersExecute(boolean othersExecute) {
		this.othersExecute = othersExecute;
	}

}
