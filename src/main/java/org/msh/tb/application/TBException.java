package org.msh.tb.application;

/**
 * Defines a general system exception to be rose in secondary business validation error (hackers)
 * @author Ricardo Memoria
 *
 */
public class TBException extends RuntimeException {
	private static final long serialVersionUID = 9112978270192369929L;

	public TBException() {
		super();
	}

	public TBException(String message, Throwable cause) {
		super(message, cause);
	}

	public TBException(String message) {
		super(message);
	}

	public TBException(Throwable cause) {
		super(cause);
	}

}
