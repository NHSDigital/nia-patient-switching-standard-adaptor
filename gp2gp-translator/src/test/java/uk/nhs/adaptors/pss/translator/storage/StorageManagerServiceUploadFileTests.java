package uk.nhs.adaptors.pss.translator.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StorageManagerServiceUploadFileTests {
    private static final String TEST_ID = "SOME_ID";
    private static final Integer RETRY_CONST = 3;

    @Mock
    private StorageService storageService;
    @InjectMocks
    private StorageManagerService storageManagerService;
    @Mock
    private StorageDataUploadWrapper anyStorageDataWrapper;
    @Mock
    private StorageServiceConfiguration storageServiceConfiguration;

    @Test
    public void When_UploadFileHasValidStorageDataWrapperPassed_Expect_FunctionDoesNotThrow() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.downloadFile(filename)).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageServiceConfiguration.getRetryLimit()).thenReturn(RETRY_CONST);

        storageManagerService.uploadFile(filename, anyStorageDataWrapper);

        // No assertion required to check if function completes successfully
    }

    @Test
    public void When_UploadFileHasValidStorageDataWrapperPassed_Expect_StorageServiceUploadFileIsCalled() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.downloadFile(filename)).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageServiceConfiguration.getRetryLimit()).thenReturn(RETRY_CONST);

        storageManagerService.uploadFile(filename, anyStorageDataWrapper);

        verify(storageService).uploadFile(filename, anyStorageDataWrapper.getData());
    }

    @Test
    public void When_UploadFileHasValidStorageDataWrapperPassed_Expect_LocalValidationReDownloadsFile() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.downloadFile(filename)).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageServiceConfiguration.getRetryLimit()).thenReturn(RETRY_CONST);

        storageManagerService.uploadFile(filename, anyStorageDataWrapper);

        verify(storageService).downloadFile(filename);
    }

    @Test
    public void When_UploadFileHasValidStorageDataWrapperPassedButFirstUploadFailsViaValidation_Expect_DeleteFileIsCalled() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.downloadFile(filename))
                .thenReturn("Not Hello World".getBytes(UTF_8),
                            "Hello World Data".getBytes(UTF_8));
        when(storageServiceConfiguration.getRetryLimit()).thenReturn(RETRY_CONST);


        storageManagerService.uploadFile(filename, anyStorageDataWrapper);

        verify(storageService).deleteFile(filename);
    }

    @Test
    public void When_UploadFileHasValidStorageDataWrapperPassedButFirstUploadFailsViaValidation_Expect_UploadFileIsCalledTwice() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.downloadFile(filename))
                .thenReturn("Not Hello World".getBytes(UTF_8),
                        "Hello World Data".getBytes(UTF_8));
        when(storageServiceConfiguration.getRetryLimit()).thenReturn(RETRY_CONST);


        storageManagerService.uploadFile(filename, anyStorageDataWrapper);

        verify(storageService, times(2)).uploadFile(filename, anyStorageDataWrapper.getData());
    }

    @Test
    public void When_UploadFileHasValidStorageDataWrapperPassedButUploadsFailPastRetryLimit_Expect_ExceptionIsThrown() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.downloadFile(filename))
                .thenReturn("Not Hello World".getBytes(UTF_8),
                            "Not Hello World Data 2".getBytes(UTF_8),
                            "Not Hello World Data 3".getBytes(UTF_8));
        when(storageServiceConfiguration.getRetryLimit()).thenReturn(RETRY_CONST);


        Exception exception = assertThrows(RuntimeException.class, () -> {
            storageManagerService.uploadFile(filename, anyStorageDataWrapper);
        });

        String expectedMessage = "Error occurred uploading to Storage";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
