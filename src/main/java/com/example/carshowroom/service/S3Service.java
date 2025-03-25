package com.example.carshowroom.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private final S3Client s3Client;
    private static final String BUCKET_NAME = "carsshowroom"; 
    private static final String AWS_REGION = "eu-central-1"; 

    public S3Service() {
        this.s3Client = S3Client.builder()
                .region(Region.of(AWS_REGION))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /*
     * method to upload file to S3 bucket
     */
    public String uploadFile(String fileName, byte[] fileData) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(fileName)
                            .build(),
                    RequestBody.fromBytes(fileData));

            return "File uploaded successfully: " + fileName;
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }


    public S3ListResponse listFiles(Integer maxKeys, String continuationToken) {
        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(BUCKET_NAME);

            if (maxKeys != null) {
                requestBuilder.maxKeys(maxKeys);
            }

            if (continuationToken != null && !continuationToken.isEmpty()) {
                requestBuilder.continuationToken(continuationToken);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());

            List<String> fileUrls = response.contents().stream()
                    .map(s3Object -> "https://" + BUCKET_NAME + ".s3." + AWS_REGION + ".amazonaws.com/" + s3Object.key())
                    .collect(Collectors.toList());

            return new S3ListResponse(fileUrls, response.nextContinuationToken());
        } catch (S3Exception e) {
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
     * method to delete file from s3 bucket
     */
    public boolean deleteFile(String fileName) {
        
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(fileName)
                    .build());
            return true;
        }catch(S3Exception e) {
            return false;
        }
    }
}
