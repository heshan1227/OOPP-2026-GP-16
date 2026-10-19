package com.fams.exception;

public class FAMSException extends Exception {
    public FAMSException(String message) {
        super(message);
    }

    public FAMSException(String message, Throwable cause) {
        super(message, cause);
    }
}
