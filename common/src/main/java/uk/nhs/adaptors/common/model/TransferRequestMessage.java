package uk.nhs.adaptors.common.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder
@Getter
@EqualsAndHashCode(callSuper = true)
@Jacksonized
public class TransferRequestMessage extends PssQueueMessage {
    private String patientNhsNumber;
    private String toAsid;
    private String fromAsid;
    private String toOds;
    private String fromOds;
}
