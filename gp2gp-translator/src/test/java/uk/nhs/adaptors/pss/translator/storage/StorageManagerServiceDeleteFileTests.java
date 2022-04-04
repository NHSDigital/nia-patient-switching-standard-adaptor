package uk.nhs.adaptors.pss.translator.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StorageManagerServiceDeleteFileTests {
    private String testId = "SomeID";

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

        String filename = testId.concat("/").concat(testId).concat("_gpc_structured.json");

        storageManagerService.deleteFile(filename);

        // No assertion required to check if function completes successfully
    }

    @Test
    public void When_ValidFilenameIsGiven_Expect_StorageServiceDeleteFileIsCalled() {

        String filename = testId.concat("/").concat(testId).concat("_gpc_structured.json");

        storageManagerService.deleteFile(filename);

        verify(storageService).deleteFile(filename);
    }

    @Test
    public void When_StorageServiceDeleteFileThrows_Expect_StorageExceptionIsThrown() {

        String filename = testId.concat("/").concat(testId).concat("_gpc_structured.json");
        willThrow(new StorageException("Error occurred deleting from Storage", null))
                .given(storageService).deleteFile(filename);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            storageManagerService.deleteFile(filename);
        });

        String expectedMessage = "Error occurred deleting from Storage";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
