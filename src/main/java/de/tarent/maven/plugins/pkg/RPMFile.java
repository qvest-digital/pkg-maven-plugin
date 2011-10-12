package de.tarent.maven.plugins.pkg;

import java.io.File;

/**
 * This extension to the AuxFile class allows the user
 * to set target permissions for the file in question.
 * 
 * Maybe would be a good idea to implement this in the
 * AuxFile type directly to provide this functionality
 * to all Packagers.
 * 
 * @author plafue
 *
 */
public class RPMFile extends File{
	
	private static final long serialVersionUID = -6482658785118570712L;


	public RPMFile(String pathname) {
		super(pathname);
	}


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
	
	/**
	 * Returns the permissions for the file in unix format
	 * as an int (there is possibly a much better way to do this).
	 *  
	 * @return
	 */
	public int getOctalPermission() {
		short total = 0;
				
		if(userRead)
			total+=400;
		if(userWrite)
			total+=200;
		if(userExecute)
			total+=100;
		if(groupRead)
			total+=40;
		if(groupWrite)
			total+=20;
		if(groupExecute)
			total+=10;
		if(othersRead)
			total+=4;
		if(othersWrite)
			total+=2;
		if(othersExecute)
			total+=1;		
		return total;
	}
	public String getOwner() {
		return owner;
	}


	public void setOwner(String owner) {
		this.owner = owner;
	}


	public String getGroup() {
		return group;
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
