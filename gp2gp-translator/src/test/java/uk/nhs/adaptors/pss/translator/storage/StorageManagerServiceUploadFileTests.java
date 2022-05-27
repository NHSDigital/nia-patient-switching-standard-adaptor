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
    private static final String CONVERSATION_ID = "6E242658-3D8E-11E3-A7DC-172BDA00FA84";

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
        when(storageManagerService.downloadFile(filename, CONVERSATION_ID)).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageServiceConfiguration.getRetryLimit()).thenReturn(RETRY_CONST);

        storageManagerService.uploadFile(filename, anyStorageDataWrapper, CONVERSATION_ID);

        // No assertion required to check if function completes successfully
    }

    @Test
    public void When_UploadFileHasValidStorageDataWrapperPassed_Expect_StorageServiceUploadFileIsCalled() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        String processedFilename = CONVERSATION_ID + "_" + filename;
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.downloadFile(filename, CONVERSATION_ID)).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageServiceConfiguration.getRetryLimit()).thenReturn(RETRY_CONST);

        storageManagerService.uploadFile(filename, anyStorageDataWrapper, CONVERSATION_ID);

        verify(storageService).uploadFile(processedFilename, anyStorageDataWrapper.getData());
    }

    @Test
    public void When_UploadFileHasValidStorageDataWrapperPassed_Expect_LocalValidationReDownloadsFile() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        String processedFilename = CONVERSATION_ID + "_" + filename;
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.downloadFile(filename, CONVERSATION_ID)).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageServiceConfiguration.getRetryLimit()).thenReturn(RETRY_CONST);

        storageManagerService.uploadFile(filename, anyStorageDataWrapper, CONVERSATION_ID);

        verify(storageService).downloadFile(processedFilename);
    }

    @Test
    public void When_UploadFileHasValidStorageDataWrapperPassedButFirstUploadFailsViaValidation_Expect_DeleteFileIsCalled() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        String processedFilename = CONVERSATION_ID + "_" + filename;
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.downloadFile(filename, CONVERSATION_ID))
                .thenReturn("Not Hello World".getBytes(UTF_8),
                            "Hello World Data".getBytes(UTF_8));
        when(storageServiceConfiguration.getRetryLimit()).thenReturn(RETRY_CONST);


        storageManagerService.uploadFile(filename, anyStorageDataWrapper, CONVERSATION_ID);

        verify(storageService).deleteFile(processedFilename);
    }

    @Test
    public void When_UploadFileHasValidStorageDataWrapperPassedButFirstUploadFailsViaValidation_Expect_UploadFileIsCalledTwice() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        String processedFilename = CONVERSATION_ID + "_" + filename;
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.downloadFile(filename, CONVERSATION_ID))
                .thenReturn("Not Hello World".getBytes(UTF_8),
                        "Hello World Data".getBytes(UTF_8));
        when(storageServiceConfiguration.getRetryLimit()).thenReturn(RETRY_CONST);


        storageManagerService.uploadFile(filename, anyStorageDataWrapper, CONVERSATION_ID);

        verify(storageService, times(2)).uploadFile(processedFilename, anyStorageDataWrapper.getData());
    }

    @Test
    public void When_UploadFileHasValidStorageDataWrapperPassedButUploadsFailPastRetryLimit_Expect_ExceptionIsThrown() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        String processedFilename = CONVERSATION_ID + "_" + filename;
        when(anyStorageDataWrapper.getData()).thenReturn("Hello World Data".getBytes(UTF_8));
        when(storageManagerService.downloadFile(processedFilename, CONVERSATION_ID))
                .thenReturn("Not Hello World".getBytes(UTF_8),
                            "Not Hello World Data 2".getBytes(UTF_8),
                            "Not Hello World Data 3".getBytes(UTF_8));
        when(storageServiceConfiguration.getRetryLimit()).thenReturn(RETRY_CONST);


        Exception exception = assertThrows(RuntimeException.class, () -> {
            storageManagerService.uploadFile(processedFilename, anyStorageDataWrapper, CONVERSATION_ID);
        });

        String expectedMessage = "Error occurred uploading to Storage";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
