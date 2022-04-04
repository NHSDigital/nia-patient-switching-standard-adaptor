package uk.nhs.adaptors.pss.translator.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StorageDataDownloadWrapper {
    private String filename;
    private String type;
    private String conversationId;
    private byte[] data;
}