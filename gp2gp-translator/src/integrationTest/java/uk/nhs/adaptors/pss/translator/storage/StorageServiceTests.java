package uk.nhs.adaptors.pss.translator.storage;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.nhs.adaptors.pss.translator.Gp2gpTranslatorApplication;

@SpringBootTest(classes = {LocalStorageService.class, Gp2gpTranslatorApplication.class})
@ExtendWith({SpringExtension.class})
public class StorageServiceTests {

    @Autowired
    private StorageService storageService;


    private String filename;

    @BeforeEach
    public void setUpBeforeEachTest() {
        filename = UUID.randomUUID().toString() + ".txt";
    }

    @AfterEach
    public void tearDownTest() {
        storageService.deleteFile(filename);
    }

    @Test
    public void When_FileIsUploadedToStorage_Expect_SameFileIsDownloaded() throws IOException {

        try (var fileUploadStream = StorageServiceTests.class.getResourceAsStream("/test.txt");
            var expectedFileStream = StorageServiceTests.class.getResourceAsStream("/test.txt")) {
            var expectedFileBytes = expectedFileStream.readAllBytes();

            storageService.uploadFile(filename, fileUploadStream.readAllBytes());

            byte[] fileDownloadAsByteArray = storageService.downloadFile(filename);
            assertThat(fileDownloadAsByteArray).isEqualTo(expectedFileBytes);
        }
    }

}
