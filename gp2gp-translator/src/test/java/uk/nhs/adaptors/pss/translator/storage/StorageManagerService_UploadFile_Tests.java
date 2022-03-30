package uk.nhs.adaptors.pss.translator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.pss.translator.storage.StorageDataWrapper;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;
import uk.nhs.adaptors.pss.translator.storage.StorageService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StorageManagerService_UploadFile_Tests {
    private static final String TEST_ID = "SOME_ID";

    @Mock
    private StorageService storageService;
    @InjectMocks
    private StorageManagerService storageManagerService;
    @Mock
    private StorageDataWrapper anyStorageDataWrapper;
    @Mock
    private ObjectMapper objectMapper;
    @Captor
    private ArgumentCaptor<byte[]> inputStreamArgumentCaptor;

    @Test
    public void UploadFile_WhenValidStorageDataWrapperIsPassed_ThenFunctionDoesNotThrow() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.DownloadFile(filename)).thenReturn("Hello World Data".getBytes(UTF_8));

        storageManagerService.UploadFile(filename, anyStorageDataWrapper);

        // No assertion required to check if function completes successfully
    }

    @Test
    public void UploadFile_WhenValidStorageDataWrapperIsPassed_ThenStorageServiceUploadFileIsCalled() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.DownloadFile(filename)).thenReturn("Hello World Data".getBytes(UTF_8));

        storageManagerService.UploadFile(filename, anyStorageDataWrapper);

        verify(storageService).UploadFile(filename, anyStorageDataWrapper.getData());
    }

    @Test
    public void UploadFile_WhenValidStorageDataWrapperIsPassed_ThenLocalValidationReDownloadsFile() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.DownloadFile(filename)).thenReturn("Hello World Data".getBytes(UTF_8));

        storageManagerService.UploadFile(filename, anyStorageDataWrapper);

        verify(storageService).DownloadFile(filename);
    }

    @Test
    public void UploadFile_WhenValidDataIsPassedButFirstUploadFailsViaValidation_ThenServiceDeleteFileIsCalled() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.DownloadFile(filename))
                .thenReturn("Not Hello World".getBytes(UTF_8),
                            "Hello World Data".getBytes(UTF_8));

        storageManagerService.UploadFile(filename, anyStorageDataWrapper);

        verify(storageService).DeleteFile(filename);
    }

    @Test
    public void UploadFile_WhenValidDataIsPassedButFirstUploadFailsViaValidation_ThenServiceUploadFileIsCalledTwice() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.DownloadFile(filename))
                .thenReturn("Not Hello World".getBytes(UTF_8),
                        "Hello World Data".getBytes(UTF_8));

        storageManagerService.UploadFile(filename, anyStorageDataWrapper);

        verify(storageService, times(2)).UploadFile(filename, anyStorageDataWrapper.getData());
    }

    @Test
    public void UploadFile_WhenValidDataIsPassedButUploadsFailPastRetryLimit_ThenStorageExceptionISsThrown() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.DownloadFile(filename))
                .thenReturn("Not Hello World".getBytes(UTF_8),
                            "Not Hello World Data 2".getBytes(UTF_8),
                            "Not Hello World Data 3".getBytes(UTF_8)
                        );

        Exception exception = assertThrows(RuntimeException.class, () -> {
            storageManagerService.UploadFile(filename, anyStorageDataWrapper);
        });

        String expectedMessage = "Error occurred uploading to Storage";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
