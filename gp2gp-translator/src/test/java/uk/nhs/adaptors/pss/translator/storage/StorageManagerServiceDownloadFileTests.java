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
    public void When_ValidFilenameIsGiven_Expect_FunctionDoesNotThrow() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");

        storageManagerService.downloadFile(filename);

        // No assertion required to check if function completes successfully
    }

    @Test
    public void When_ValidFilenameIsGiven_Expect_StorageServiceDownloadFileIsCalled() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");

        storageManagerService.downloadFile(filename);

        verify(storageService).downloadFile(filename);
    }

    @Test
    public void When_ValidFilenameIsGiven_Expect_ExpectedByteStringIsReturned() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        byte[] expectedResponse = "File byte response example".getBytes(UTF_8);
        when(storageService.downloadFile(filename)).thenReturn(expectedResponse);

        byte[] result = storageManagerService.downloadFile(filename);

        assertTrue(java.util.Arrays.equals(result, expectedResponse));
    }

    @Test
    public void When_StorageServiceDownloadFileThrows_Expect_StorageExceptionIsThrown() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(storageService.downloadFile(filename))
                .thenThrow(new StorageException("", null));


        Exception exception = assertThrows(RuntimeException.class, () -> {
            storageManagerService.downloadFile(filename);
        });

        String expectedMessage = "Error occurred downloading from Storage";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
