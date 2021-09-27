package com.mmaozi.resti.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RestiBaseException extends RuntimeException {

    private final String message;

    public RestiBaseException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public RestiBaseException(String message) {
        super(message);
        this.message = message;
    }
}
