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
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StorageManagerService_DeleteFile_Tests {
    private String TEST_ID="SomeID";

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
    public void DeleteFile_WhenValidFilenameIsGiven_ThenFunctionDoesNotThrow() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");

        storageManagerService.DeleteFile(filename);

        // No assertion required to check if function completes successfully
    }

    @Test
    public void DeleteFile_WhenValidFilenameIsGiven_ThenStorageServiceDeleteFileIsCalled() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");

        storageManagerService.DeleteFile(filename);

        verify(storageService).DeleteFile(filename);
    }

    @Test
    public void DeleteFile_WhenStorageServiceDeleteFileThrows_ThenStorageExceptionIsThrown() {

        String filename = TEST_ID.concat("/").concat(TEST_ID).concat("_gpc_structured.json");
        willThrow(new StorageException("Error occurred deleting from Storage", null))
                .given(storageService).DeleteFile(filename);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            storageManagerService.DeleteFile(filename);
        });

        String expectedMessage = "Error occurred deleting from Storage";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
