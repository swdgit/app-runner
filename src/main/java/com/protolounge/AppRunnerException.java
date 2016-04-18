/**
 * 
 */
package com.protolounge;

/**
 * @author stacytt
 *
 */
public class AppRunnerException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -6787359629432377383L;

    /**
     * 
     */
    public AppRunnerException() {
    }

    /**
     * @param message
     */
    public AppRunnerException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public AppRunnerException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public AppRunnerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public AppRunnerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
