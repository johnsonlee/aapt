package com.sdklite.aapt;

/**
 * AAPT exception
 * 
 * @author johnsonlee
 *
 */
@SuppressWarnings("serial")
public class AaptException extends RuntimeException {

    /**
     * Default constructor
     */
    public AaptException() {
        super();
    }

    /**
     * Instantialize with message and cause
     * 
     * @param message
     *            The message
     * @param cause
     *            The cause
     */
    public AaptException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantialize with message
     * 
     * @param message
     *            The message
     */
    public AaptException(final String message) {
        super(message);
    }

    /**
     * Instantialize with caurse
     * 
     * @param cause
     *            The cause
     */
    public AaptException(final Throwable cause) {
        super(cause);
    }

}
