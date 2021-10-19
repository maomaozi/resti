package com.mmaozi.resti.exception;

public class StreamIOException extends RestiBaseException {

    public StreamIOException(String message) {
        super(message);
    }

    public StreamIOException(String message, Exception ex) {
        super(message, ex);
    }

}
