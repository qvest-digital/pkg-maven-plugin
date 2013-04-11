package de.tarent.maven.plugins.pkg.exception;

public class XMLParserException extends Exception {

	private static final long serialVersionUID = -8872495978331881464L;

	public XMLParserException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public XMLParserException(String msg) {
		super(msg);
	}

}