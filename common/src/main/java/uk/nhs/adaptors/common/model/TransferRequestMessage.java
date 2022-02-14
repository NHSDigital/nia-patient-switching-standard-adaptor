package uk.nhs.adaptors.common.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@EqualsAndHashCode(callSuper = true)
public class TransferRequestMessage extends PssQueueMessage {
    private String patientNhsNumber;
}
