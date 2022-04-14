package uk.nhs.adaptors.pss.translator.storage;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@Service
public class AzureStorageService implements StorageService {

    // Consistent objects
    private final BlobServiceClient blobServiceClient;
    private String containerName;

    public AzureStorageService(StorageServiceConfiguration configuration) {
        if (!configuration.getAccountReference().isEmpty()) {

            StorageSharedKeyCredential credentials = createAzureCredentials(
                configuration.getAccountReference(), configuration.getAccountSecret());

            String azureEndpoint = createAzureStorageEndpoint(configuration.getAccountReference());
            blobServiceClient = createBlobServiceClient(azureEndpoint, credentials);
            containerName = configuration.getContainerName();
        } else {
            blobServiceClient = null;
        }
    }

    public void uploadFile(String filename, byte[] fileAsString) throws StorageException {
        try {
            addFileStringToMainContainer(filename, fileAsString);
        } catch (IOException e) {
            throw new StorageException("Failed adding file to Azure Blob storage", e);
        }
    }

    public byte[] downloadFile(String filename) throws StorageException {
        ByteArrayOutputStream stream = downloadFileToStream(filename);
        return stream.toByteArray();
    }

    public void deleteFile(String filename) {
        BlobContainerClient containerClient = createBlobContainerClient();
        BlockBlobClient blobClient = containerClient.getBlobClient(filename).getBlockBlobClient();
        blobClient.delete();
    }

    private StorageSharedKeyCredential createAzureCredentials(String accountName, String accountKey) {
        return new StorageSharedKeyCredential(accountName, accountKey);
    }

    private String createAzureStorageEndpoint(String containerName) {
        return String.format(Locale.ROOT, "https://%s.blob.core.windows.net", containerName);
    }

    private BlobServiceClient createBlobServiceClient(String endpoint, StorageSharedKeyCredential credentials) {

        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(credentials)
                .buildClient();
    }

    private BlobContainerClient createBlobContainerClient() {
        return blobServiceClient.getBlobContainerClient(containerName);
    }

    private void addFileStringToMainContainer(String filename, byte[] fileAsString) throws StorageException, IOException {
        try {
            BlobContainerClient containerClient = createBlobContainerClient();
            BlockBlobClient blobClient = containerClient.getBlobClient(filename).getBlockBlobClient();
            InputStream dataStream = new ByteArrayInputStream(fileAsString);
            blobClient.upload(dataStream, fileAsString.length);
            dataStream.close();
        } catch (IOException e) {
            throw new StorageException("Failed to upload blob to Azure Blob storage", e);
        }
    }

    private ByteArrayOutputStream downloadFileToStream(String filename) throws StorageException {
        try {
            BlobContainerClient containerClient = createBlobContainerClient();
            BlockBlobClient blobClient = containerClient.getBlobClient(filename).getBlockBlobClient();
            int dataSize = (int) blobClient.getProperties().getBlobSize();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
            blobClient.downloadStream(outputStream);
            outputStream.close();
            return outputStream;
        } catch (IOException e) {
            throw new StorageException("Failed to download blob from Azure Blob storage", e);
        }
    }

}
