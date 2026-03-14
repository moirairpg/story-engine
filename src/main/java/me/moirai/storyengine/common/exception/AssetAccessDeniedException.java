package me.moirai.storyengine.common.exception;

public class AssetAccessDeniedException extends RuntimeException {

    public AssetAccessDeniedException(String message) {

        super(message);
    }

    public AssetAccessDeniedException(String message, Throwable throwable) {

        super(message, throwable);
    }

    public AssetAccessDeniedException(Throwable throwable) {

        super(throwable);
    }
}
