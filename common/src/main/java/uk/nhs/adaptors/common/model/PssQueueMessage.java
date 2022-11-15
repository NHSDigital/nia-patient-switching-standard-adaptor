package uk.nhs.adaptors.common.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.nhs.adaptors.common.enums.QueueMessageType;

@Getter
@SuperBuilder
@EqualsAndHashCode
@Jacksonized
public class PssQueueMessage {
    private QueueMessageType messageType;
    private String conversationId;
}
