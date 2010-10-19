package org.sc.annotator.adaptive.exceptions;

public class MatcherException extends Exception {

	public MatcherException(String message) {
		super(message);
	}

	public MatcherException(Throwable cause) {
		super(cause);
	}

	public MatcherException(String message, Throwable cause) {
		super(message, cause);
	}
}
