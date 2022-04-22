package uk.nhs.adaptors.pss.translator.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Date;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.springframework.stereotype.Service;


@Service
public class AWSStorageService implements StorageService {

    private final AmazonS3 s3Client;
    private String bucketName;

    public AWSStorageService(StorageServiceConfiguration configuration) {

        bucketName = configuration.getContainerName();

        AWSCredentials credentials = new BasicAWSCredentials(
                configuration.getAccountReference(),
                configuration.getAccountSecret()
        );

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(configuration.getRegion())
                .build();
    }

    public void uploadFile(String filename, byte[] fileAsString) throws StorageException {

        try {
            com.amazonaws.services.s3.model.ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileAsString.length);
            InputStream inputStream = new ByteArrayInputStream(fileAsString);

            s3Client.putObject(bucketName, filename, inputStream, metadata);
        } catch (Exception e) {
            throw new StorageException("Error occurred uploading to S3 Bucket", e);
        }
    }

    public byte[] downloadFile(String filename) throws StorageException {
        try {
            S3ObjectInputStream stream = downloadFileToStream(filename);
            return IOUtils.toByteArray(stream);
        } catch (IOException e) {
            throw new StorageException("Error occurred downloading from S3 Bucket", e);
        }
    }

    public void deleteFile(String filename) {
        s3Client.deleteObject(bucketName, filename);
    }

    public String getFileLocation(String filename) {
        // https://docs.aws.amazon.com/AmazonS3/latest/userguide/ShareObjectPreSignedURL.html
        // sharing file location from AWS is not straightforward as files are private by default
        Date expiration = new Date();
        long expTimeMillis = Instant.now().toEpochMilli();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);

        URL url = s3Client.generatePresignedUrl(bucketName, filename, expiration);
        return url.toString();
    }

    private S3ObjectInputStream downloadFileToStream(String filename) throws StorageException {
        try {
            S3Object s3Object = s3Client.getObject(bucketName, filename);
            return s3Object.getObjectContent();
        } catch (Exception exception) {
            throw new StorageException("Error occurred downloading from S3 Bucket", exception);
        }
    }
}