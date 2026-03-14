package me.moirai.storyengine.common.exception;

public class AssetNotFoundException extends RuntimeException {

    public AssetNotFoundException(String message) {

        super(message);
    }

    public AssetNotFoundException(String message, Throwable throwable) {

        super(message, throwable);
    }

    public AssetNotFoundException(Throwable throwable) {

        super(throwable);
    }
}
