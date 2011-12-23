package de.tarent.maven.plugins.pkg;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores parameters for the upload goal.
 * @author plafue
 *
 */
public class UploadParameters {
	/**
	 * List of urls to upload a Package to.
	 * @parameter
	 * @required
	 */
	private List<String> urls;
	/**
	 * Optional parameter to specify a target directory
	 */
	private String toDir;
	/**
	 * Optional parameter to specify a username. This will them replace the %USERNAME% placeholder in the url. 
	 */
	private String username;	
	/**
	 * Optional parameter to specify a password. This will them replace the %PASSWORD% placeholder in the url. 
	 */
	private String password;
	
	static final Pattern usernamePattern = Pattern.compile("%USERNAME%");
	static final Pattern passwordPattern = Pattern.compile("%PASSWORD%");
	
	public String parseUrlPlaceholders(String url) {
		
		Matcher m;
		
		if(username!=null){
			m = usernamePattern.matcher(url);
			while(m.find()){
				url = m.replaceAll(username);
			}
		}
		if(password!=null){
			m = passwordPattern.matcher(url);
			while(m.find()){
				url = m.replaceAll(password);
			}
		}
		return url;
	}
	
	public List<String> getUrls(){
		return urls;
	}
	
	public String getToDir() {
		return toDir;
	}
	public void setToDir(String toDir) {
		this.toDir = toDir;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
