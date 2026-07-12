package ru.ifmo.person.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MinioService {

    @Inject
    private MinioClient minioClient;

    private static final String BUCKET_NAME = System.getenv("MINIO_BUCKET") != null 
        ? System.getenv("MINIO_BUCKET") 
        : "person-imports";

    public void ensureBucketExists() throws Exception {
        boolean found = minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket(BUCKET_NAME)
                .build()
        );
        
        if (!found) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(BUCKET_NAME)
                    .build()
            );
            System.out.println("Bucket " + BUCKET_NAME + " created successfully");
        }
    }

    public void uploadFile(String fileName, byte[] content, String contentType) throws Exception {
        ensureBucketExists();
        
        ByteArrayInputStream stream = new ByteArrayInputStream(content);
        
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(BUCKET_NAME)
                .object(fileName)
                .stream(stream, content.length, -1)
                .contentType(contentType != null ? contentType : "application/json")
                .build()
        );
        
        System.out.println("File " + fileName + " uploaded successfully to MinIO");
    }

    public InputStream downloadFile(String fileName) throws Exception {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(BUCKET_NAME)
                .object(fileName)
                .build()
        );
    }

    public void deleteFile(String fileName) throws Exception {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(BUCKET_NAME)
                .object(fileName)
                .build()
        );
        
        System.out.println("File " + fileName + " deleted from MinIO");
    }

    public boolean fileExists(String fileName) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(fileName)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String generatePresignedUrl(String fileName, int expiryInDays) throws Exception {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(BUCKET_NAME)
                .object(fileName)
                .expiry(expiryInDays, TimeUnit.DAYS)
                .build()
        );
    }

    public String generatePresignedUrl(String fileName) throws Exception {
        return generatePresignedUrl(fileName, 7);
    }

    public StatObjectResponse getFileInfo(String fileName) throws Exception {
        return minioClient.statObject(
            StatObjectArgs.builder()
                .bucket(BUCKET_NAME)
                .object(fileName)
                .build()
        );
    }

    public boolean isMinioAvailable() {
        try {
            minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(BUCKET_NAME)
                    .build()
            );
            return true;
        } catch (Exception e) {
            System.err.println("MinIO is not available: " + e.getMessage());
            return false;
        }
    }
}
