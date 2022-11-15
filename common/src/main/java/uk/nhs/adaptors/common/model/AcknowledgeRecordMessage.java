package uk.nhs.adaptors.common.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.nhs.adaptors.common.enums.ConfirmationResponse;

@SuperBuilder
@Getter
@EqualsAndHashCode(callSuper = true)
@Jacksonized
public class AcknowledgeRecordMessage extends PssQueueMessage {
    private ConfirmationResponse confirmationResponse;
    private String originalMessage;
}
