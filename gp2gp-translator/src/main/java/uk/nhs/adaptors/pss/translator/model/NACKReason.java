package uk.nhs.adaptors.pss.translator.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NACKReason {
    LARGE_MESSAGE_REASSEMBLY_FAILURE("29"),
    LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED("31"),
    LARGE_MESSAGE_GENERAL_FAILURE("30"),
    LARGE_MESSAGE_TIMEOUT("25");

    @Getter
    private final String code;
}

