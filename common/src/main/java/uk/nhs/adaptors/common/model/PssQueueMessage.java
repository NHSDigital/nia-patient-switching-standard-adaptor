package uk.nhs.adaptors.common.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@EqualsAndHashCode
public class PssQueueMessage {
    private String conversationId;
    private String toAsid;
    private String fromAsid;
    private String toOds;
    private String fromOds;
}
