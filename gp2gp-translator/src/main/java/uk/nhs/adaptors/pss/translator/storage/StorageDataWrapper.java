package uk.nhs.adaptors.pss.translator.storage;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class StorageDataWrapper {
    private String type;
    private String conversationId;
    private String taskId;
    private byte[] data;
}