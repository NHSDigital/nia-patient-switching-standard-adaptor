package uk.nhs.adaptors.pss.translator.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static java.nio.charset.StandardCharsets.UTF_8;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StorageManagerServiceDownloadFileTests {
    private static final String TEST_ID = "SOME_ID";
    private static final String CONVERSATION_ID = "6E242658-3D8E-11E3-A7DC-172BDA00FA84";

    @Mock
    private StorageService storageService;
    @InjectMocks
    private StorageManagerService storageManagerService;
    @Mock
    private StorageDataUploadWrapper anyStorageDataWrapper;
    @Mock
    private ObjectMapper objectMapper;
    @Captor
    private ArgumentCaptor<byte[]> inputStreamArgumentCaptor;

    @Test
    public void When_ValidFilenameIsGiven_Expect_FunctionDoesNotThrow() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");

        storageManagerService.downloadFile(filename, CONVERSATION_ID);

        // No assertion required to check if function completes successfully
    }

    @Test
    public void When_ValidFilenameIsGiven_Expect_StorageServiceDownloadFileIsCalled() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        String processedFilename = CONVERSATION_ID + "_" + filename;

        storageManagerService.downloadFile(filename, CONVERSATION_ID);

        verify(storageService).downloadFile(processedFilename);
    }

    @Test
    public void When_ValidFilenameIsGiven_Expect_ExpectedByteStringIsReturned() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        String processedFilename = CONVERSATION_ID + "_" + filename;
        byte[] expectedResponse = "File byte response example".getBytes(UTF_8);
        when(storageService.downloadFile(processedFilename)).thenReturn(expectedResponse);

        byte[] result = storageManagerService.downloadFile(filename, CONVERSATION_ID);

        assertTrue(java.util.Arrays.equals(result, expectedResponse));
    }

    @Test
    public void When_StorageServiceDownloadFileThrows_Expect_StorageExceptionIsThrown() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(storageService.downloadFile(filename))
                .thenThrow(new StorageException("", null));


        Exception exception = assertThrows(RuntimeException.class, () -> {
            storageManagerService.downloadFile(filename, CONVERSATION_ID);
        });

        String expectedMessage = "Error occurred downloading from Storage";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
