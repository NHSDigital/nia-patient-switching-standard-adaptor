package uk.nhs.adaptors.pss.translator.storage;

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

import java.io.*;

@Service
public class AWSStorageService implements StorageService {

    private final AmazonS3 _s3Client;
    private String _bucketName;

    public AWSStorageService(StorageServiceConfiguration configuration){

        _bucketName = configuration.getContainerName();

        AWSCredentials credentials = new BasicAWSCredentials(
                configuration.getAccountReference(),
                configuration.getAccountSecret()
        );

        _s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(configuration.getRegion())
                .build();
    }

    public void UploadFile(String filename, byte[] fileAsString) throws StorageException {

        try {
            com.amazonaws.services.s3.model.ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileAsString.length);
            InputStream inputStream = new ByteArrayInputStream(fileAsString);

            _s3Client.putObject(_bucketName, filename, inputStream, metadata );
        }
        catch (Exception e) {
            throw new StorageException("Error occurred uploading to S3 Bucket", e);
        }
    }

    public byte[] DownloadFile(String filename) throws StorageException {
        try {
            S3ObjectInputStream stream = DownloadFileToStream(filename);
            return IOUtils.toByteArray(stream);
        }
        catch (IOException e) {
            throw new StorageException("Error occurred downloading from S3 Bucket", e);
        }
    }

    public void DeleteFile(String filename){
        _s3Client.deleteObject(_bucketName,filename);
    }

    private S3ObjectInputStream DownloadFileToStream(String filename) throws StorageException {
        try {
            S3Object s3Object = _s3Client.getObject(_bucketName, filename);
            return s3Object.getObjectContent();
        } catch (Exception exception) {
            throw new StorageException("Error occurred downloading from S3 Bucket", exception);
        }
    }
}