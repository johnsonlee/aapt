package com.sdklite.aapt;

/**
 * AAPT exception
 * 
 * @author johnsonlee
 *
 */
@SuppressWarnings("serial")
public class AaptException extends RuntimeException {

    public AaptException() {
        super();
    }

    public AaptException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AaptException(final String message) {
        super(message);
    }

    public AaptException(final Throwable cause) {
        super(cause);
    }

}
