package ru.ifmo.person.config;

import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class MinioConfig {

    @Produces
    @Dependent  // Changed from @ApplicationScoped - MinioClient is final class and not proxyable
    public MinioClient createMinioClient() {
        String endpoint = System.getenv("MINIO_ENDPOINT");
        String accessKey = System.getenv("MINIO_ACCESS_KEY");
        String secretKey = System.getenv("MINIO_SECRET_KEY");

        if (endpoint == null || endpoint.isEmpty()) {
            endpoint = "http://localhost:9000";
        }
        if (accessKey == null || accessKey.isEmpty()) {
            accessKey = "minioadmin";
        }
        if (secretKey == null || secretKey.isEmpty()) {
            secretKey = "minioadmin";
        }

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public void disposeMinioClient(@Disposes MinioClient client) {
        System.out.println("MinIO client disposed");
    }
}
