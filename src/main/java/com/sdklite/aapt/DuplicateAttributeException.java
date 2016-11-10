package com.sdklite.aapt;

/**
 * This exception is thrown when adding an duplicate attributes is requested
 * 
 * @author johnsonlee
 *
 */
@SuppressWarnings("serial")
public class DuplicateAttributeException extends Exception {

    /**
     * Default constructor
     */
    public DuplicateAttributeException() {
        super();
    }

    public DuplicateAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateAttributeException(String message) {
        super(message);
    }

    public DuplicateAttributeException(Throwable cause) {
        super(cause);
    }

}
