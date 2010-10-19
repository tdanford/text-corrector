package org.sc.annotator.adaptive.exceptions;

public class MatcherCloseException extends MatcherException {

	public MatcherCloseException(String message) {
		super(message);
	}

	public MatcherCloseException(Throwable cause) {
		super(cause);
	}

	public MatcherCloseException(String message, Throwable cause) {
		super(message, cause);
	}
}
