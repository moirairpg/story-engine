package me.moirai.storyengine.infrastructure.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

@Configuration
public class StorageConfig {

    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucket;
    private final String region;

    public StorageConfig(
            @Value("${moirai.storage.endpoint}") String endpoint,
            @Value("${moirai.storage.access-key}") String accessKey,
            @Value("${moirai.storage.secret-key}") String secretKey,
            @Value("${moirai.storage.bucket}") String bucket,
            @Value("${moirai.storage.region}") String region) {

        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
        this.region = region;
    }

    @Bean
    S3Client s3Client() {

        var credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(credentials)
                .region(Region.of(region))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    ApplicationRunner initStorageBucket(S3Client s3Client) {

        return args -> {
            try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            } catch (NoSuchBucketException e) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            }
        };
    }
}
