package com.mmaozi.resti.exception;

public class MethodInvokeException extends RestiBaseException {

    public MethodInvokeException(String message) {
        super(message);
    }

    public MethodInvokeException(String message, Exception ex) {
        super(message, ex);
    }

}
