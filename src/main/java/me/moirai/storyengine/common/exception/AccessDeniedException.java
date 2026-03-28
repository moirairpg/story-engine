package me.moirai.storyengine.common.exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {

        super(message);
    }

    public AccessDeniedException(String message, Throwable throwable) {

        super(message, throwable);
    }

    public AccessDeniedException(Throwable throwable) {

        super(throwable);
    }
}
