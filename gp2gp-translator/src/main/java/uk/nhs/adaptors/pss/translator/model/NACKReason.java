package uk.nhs.adaptors.pss.translator.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NACKReason {
    LARGE_MESSAGE_REASSEMBLY_FAILURE("29"),
    LARGE_MESSAGE_ATTACHMENTS_NOT_RECEIVED("31"),
    LARGE_MESSAGE_GENERAL_FAILURE("30"),
    LARGE_MESSAGE_TIMEOUT("25"),
    CLINICAL_SYSTEM_INTEGRATION_FAILURE("11"),
    EHR_EXTRACT_CANNOT_BE_PROCESSED("21"),
    UNEXPECTED_CONDITION("99");

    @Getter
    private final String code;
}

