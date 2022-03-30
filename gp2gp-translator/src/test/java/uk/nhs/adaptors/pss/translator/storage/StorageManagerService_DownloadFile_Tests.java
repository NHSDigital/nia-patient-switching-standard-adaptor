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
import uk.nhs.adaptors.pss.translator.storage.StorageException;
import uk.nhs.adaptors.pss.translator.storage.StorageManagerService;
import uk.nhs.adaptors.pss.translator.storage.StorageService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StorageManagerService_DownloadFile_Tests {
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
    public void DownloadFile_WhenValidFilenameIsGiven_ThenFunctionDoesNotThrow() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");

        storageManagerService.DownloadFile(filename);

        // No assertion required to check if function completes successfully
    }

    @Test
    public void DownloadFile_WhenValidFilenameIsGiven_ThenStorageServiceDownloadFileIsCalled() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");

        storageManagerService.DownloadFile(filename);

        verify(storageService).DownloadFile(filename);
    }

    @Test
    public void DownloadFile_WhenValidFilenameIsGiven_ThenExpectedByteStringIsReturned() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        byte[] expectedResponse = "File byte response example".getBytes(UTF_8);
        when(storageService.DownloadFile(filename)).thenReturn(expectedResponse);

        byte[] result = storageManagerService.DownloadFile(filename);

        assertEquals(result, expectedResponse);
    }

    @Test
    public void DownloadFile_WhenStorageServiceDownloadFileThrows_ThenStorageExceptionIsThrown() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        when(storageService.DownloadFile(filename))
                .thenThrow(new StorageException("", null));


        Exception exception = assertThrows(RuntimeException.class, () -> {
            storageManagerService.DownloadFile(filename);
        });

        String expectedMessage = "Error occurred downloading from Storage";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
