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
public class  AzureStorageService implements StorageService {

    // Consistent objects
    private final BlobServiceClient _blobServiceClient;
    private String _containerName;

    public AzureStorageService(StorageServiceConfiguration configuration){
        StorageSharedKeyCredential credentials = CreateAzureCredentials(configuration.getAccountReference(), configuration.getAccountSecret());
        String azureEndpoint = CreateAzureStorageEndpoint(configuration.getAccountReference());
        _blobServiceClient = CreateBlobServiceClient(azureEndpoint, credentials);
        _containerName = configuration.getContainerName();
    }

    public void UploadFile(String filename, byte[] fileAsString) throws StorageException {
        try {
            AddFileStringToMainContainer(filename, fileAsString);
        }
        catch (IOException e) {
            throw new StorageException("Failed adding file to Azure Blob storage", e);
        }
    }

    public byte[] DownloadFile(String filename) throws StorageException {
        ByteArrayOutputStream stream = DownloadFileToStream(filename);
        return stream.toByteArray();
    }

    public void DeleteFile(String filename){
        BlobContainerClient containerClient = CreateBlobContainerClient();
        BlockBlobClient blobClient = containerClient.getBlobClient(filename).getBlockBlobClient();
        blobClient.delete();
    }

    private StorageSharedKeyCredential CreateAzureCredentials(String accountName, String accountKey){
        return new StorageSharedKeyCredential(accountName, accountKey);
    }

    private String CreateAzureStorageEndpoint(String containerName){
        return String.format(Locale.ROOT, "https://%s.blob.core.windows.net", containerName);
    }

    private BlobServiceClient CreateBlobServiceClient(String endpoint, StorageSharedKeyCredential credentials){

        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(credentials)
                .buildClient();
    }

    private BlobContainerClient CreateBlobContainerClient() {
        return _blobServiceClient.getBlobContainerClient(_containerName);
    }

    private void AddFileStringToMainContainer(String filename, byte[] fileAsString) throws StorageException, IOException {
        try {
            BlobContainerClient containerClient = CreateBlobContainerClient();
            BlockBlobClient blobClient = containerClient.getBlobClient(filename).getBlockBlobClient();
            InputStream dataStream = new ByteArrayInputStream(fileAsString);
            blobClient.upload(dataStream, fileAsString.length);
            dataStream.close();
        }
        catch (IOException e) {
            throw new StorageException("Failed to upload blob to Azure Blob storage", e);
        }
    }

    private ByteArrayOutputStream DownloadFileToStream(String filename) throws StorageException {
        try {
            BlobContainerClient containerClient = CreateBlobContainerClient();
            BlockBlobClient blobClient = containerClient.getBlobClient(filename).getBlockBlobClient();
            int dataSize = (int) blobClient.getProperties().getBlobSize();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
            blobClient.downloadStream(outputStream);
            outputStream.close();
            return outputStream;
        }
        catch (IOException e) {
            throw new StorageException("Failed to download blob from Azure Blob storage", e);
        }
    }

}
