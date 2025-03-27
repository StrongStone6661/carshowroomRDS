package com.example.carshowroom.service;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private final S3Client s3Client;
    private static final String BUCKET_NAME = "carsshowroom";
    private static final String AWS_REGION = "eu-central-1";

    public S3Service() {
        // Load credentials from environment variables or IAM Role
        this.s3Client = S3Client.builder()
                .region(Region.of(AWS_REGION))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }

    /*
     * Method to upload file to S3 bucket
     */
    public String uploadFile(String fileName, byte[] fileData) {
        try {
            // Upload the file to S3
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(fileName)
                    .build(),
                    RequestBody.fromBytes(fileData));

            // Construct S3 URL
            return "https://" + BUCKET_NAME + ".s3." + AWS_REGION + ".amazonaws.com/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    /*
     * Method to list files in S3 bucket
     */
    public S3ListResponse listFiles(Integer maxKeys, String continuationToken) {
        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(BUCKET_NAME)
                    .maxKeys(maxKeys);

            if (continuationToken != null && !continuationToken.isEmpty()) {
                requestBuilder.continuationToken(continuationToken);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());

            List<String> fileUrls = response.contents().stream()
                    .map(s3Object -> "https://" + BUCKET_NAME + ".s3." + AWS_REGION + ".amazonaws.com/" + s3Object.key())
                    .collect(Collectors.toList());

            return new S3ListResponse(fileUrls, response.nextContinuationToken());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list files: " + e.getMessage());
        }
    }

    /*
     * Response class to hold paginated S3 results
     */
    public static class S3ListResponse {
        private final List<String> fileUrls;
        private final String nextContinuationToken;

        public S3ListResponse(List<String> fileUrls, String nextContinuationToken) {
            this.fileUrls = fileUrls;
            this.nextContinuationToken = nextContinuationToken;
        }

        public List<String> getFileUrls() {
            return fileUrls;
        }

        public String getNextContinuationToken() {
            return nextContinuationToken;
        }
    }

    /*
     * Method to delete file from S3 bucket
     */
    public boolean deleteFile(String fileName) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(fileName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}