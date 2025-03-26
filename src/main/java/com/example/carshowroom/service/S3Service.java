package com.example.carshowroom.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private final AmazonS3 s3Client;
    private static final String BUCKET_NAME = "carsshowroom";
    private static final String AWS_REGION = "eu-central-1";

    public S3Service() {
        // Load credentials from environment variables or IAM Role
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(
                System.getenv("AWS_ACCESS_KEY_ID"),
                System.getenv("AWS_SECRET_ACCESS_KEY")
        );

        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(AWS_REGION)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

    /*
     * method to upload file to S3 bucket
     */
    public String uploadFile(String fileName, byte[] fileData) {
        try {
            // Convert byte[] to InputStream
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileData.length);

            s3Client.putObject(new PutObjectRequest(BUCKET_NAME, fileName, 
                new java.io.ByteArrayInputStream(fileData), metadata));

            // Construct S3 URL
            return "https://" + BUCKET_NAME + ".s3." + AWS_REGION + ".amazonaws.com/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    /*
     * method to list files in S3 bucket
     */
    public S3ListResponse listFiles(Integer maxKeys, String continuationToken) {
        try {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(BUCKET_NAME)
                    .withMaxKeys(maxKeys);

            if (continuationToken != null && !continuationToken.isEmpty()) {
                request.setContinuationToken(continuationToken);
            }

            ListObjectsV2Result response = s3Client.listObjectsV2(request);

            List<String> fileUrls = response.getObjectSummaries().stream()
                    .map(s3Object -> "https://" + BUCKET_NAME + ".s3." + AWS_REGION + ".amazonaws.com/" + s3Object.getKey())
                    .collect(Collectors.toList());

            return new S3ListResponse(fileUrls, response.getNextContinuationToken());
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
     * method to delete file from s3 bucket
     */
    public boolean deleteFile(String fileName) {
        try {
            s3Client.deleteObject(new DeleteObjectRequest(BUCKET_NAME, fileName));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
