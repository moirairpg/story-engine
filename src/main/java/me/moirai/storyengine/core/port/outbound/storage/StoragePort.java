package me.moirai.storyengine.core.port.outbound.storage;

public interface StoragePort {

    String upload(String key, byte[] bytes, String contentType);

    void delete(String key);

    String resolveUrl(String key);
}
