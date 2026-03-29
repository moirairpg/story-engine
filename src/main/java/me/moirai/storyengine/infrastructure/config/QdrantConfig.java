package me.moirai.storyengine.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.CollectionExistsRequest;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.VectorsConfig;
import io.qdrant.client.grpc.CollectionsGrpc;

@Configuration
public class QdrantConfig {

    private final String host;
    private final int port;
    private final String collectionName;
    private final int vectorSize;

    public QdrantConfig(
            @Value("${moirai.qdrant.host}") String host,
            @Value("${moirai.qdrant.port}") int port,
            @Value("${moirai.rag.lorebook.collection-name}") String collectionName,
            @Value("${moirai.rag.lorebook.vector-size}") int vectorSize) {

        this.host = host;
        this.port = port;
        this.collectionName = collectionName;
        this.vectorSize = vectorSize;
    }

    @Bean
    QdrantGrpcClient qdrantClient() {
        return QdrantGrpcClient.newBuilder(host, port, false).build();
    }

    @Bean
    ApplicationRunner initLorebookCollection(QdrantGrpcClient qdrantClient) {

        return args -> {
            var collections = CollectionsGrpc.newBlockingStub(qdrantClient.channel());
            var exists = collections.collectionExists(
                    CollectionExistsRequest.newBuilder()
                            .setCollectionName(collectionName)
                            .build())
                    .getResult()
                    .getExists();

            if (!exists) {
                collections.create(CreateCollection.newBuilder()
                        .setCollectionName(collectionName)
                        .setVectorsConfig(VectorsConfig.newBuilder()
                                .setParams(VectorParams.newBuilder()
                                        .setSize(vectorSize)
                                        .setDistance(Distance.Cosine)
                                        .build())
                                .build())
                        .build());
            }
        };
    }
}
