package me.moirai.storyengine.infrastructure.outbound.adapter.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3StorageAdapter implements StoragePort {

    private final S3Client s3Client;
    private final String bucket;
    private final String endpoint;

    public S3StorageAdapter(
            S3Client s3Client,
            @Value("${moirai.storage.bucket}") String bucket,
            @Value("${moirai.storage.endpoint}") String endpoint) {

        this.s3Client = s3Client;
        this.bucket = bucket;
        this.endpoint = endpoint;
    }

    @Override
    public String upload(String key, byte[] bytes, String contentType) {

        var request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(bytes));
        return key;
    }

    @Override
    public void delete(String key) {

        var request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    @Override
    public String resolveUrl(String key) {

        if (key == null) {
            return null;
        }

        return endpoint + "/" + bucket + "/" + key;
    }
}
