package uk.nhs.adaptors.common.model;

import static uk.nhs.adaptors.common.enums.MigrationStatus.CONTINUE_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.COPC_ACKNOWLEDGED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.COPC_MESSAGE_PROCESSING;
import static uk.nhs.adaptors.common.enums.MigrationStatus.COPC_MESSAGE_RECEIVED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_NEGATIVE_ACK_ABA_INCORRECT_PATIENT;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_NEGATIVE_ACK_FAILED_TO_INTEGRATE;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_NEGATIVE_ACK_NON_ABA_INCORRECT_PATIENT;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_NEGATIVE_ACK_SUPPRESSED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_PROCESSING;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_ACCEPTED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_ACKNOWLEDGED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_EHR_GENERATION_ERROR;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_MISFORMED_REQUEST;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_MULTI_OR_NO_RESPONSES;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_NOT_PRIMARY_HEALTHCARE_PROVIDER;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_PATIENT_NOT_REGISTERED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_SENDER_NOT_CONFIGURED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_REQUEST_NEGATIVE_ACK_UNKNOWN;
import static uk.nhs.adaptors.common.enums.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED;
import static uk.nhs.adaptors.common.enums.MigrationStatus.ERROR_LRG_MSG_GENERAL_FAILURE;
import static uk.nhs.adaptors.common.enums.MigrationStatus.ERROR_LRG_MSG_REASSEMBLY_FAILURE;
import static uk.nhs.adaptors.common.enums.MigrationStatus.ERROR_LRG_MSG_TIMEOUT;
import static uk.nhs.adaptors.common.enums.MigrationStatus.REQUEST_RECEIVED;

import java.util.List;

import uk.nhs.adaptors.common.enums.MigrationStatus;

public class MigrationStatusGroups {
    public static final List<MigrationStatus> IN_PROGRESS_STATUSES = List.of(
            REQUEST_RECEIVED,
            EHR_EXTRACT_REQUEST_ACCEPTED,
            EHR_EXTRACT_RECEIVED,
            EHR_EXTRACT_PROCESSING,
            EHR_EXTRACT_REQUEST_ACKNOWLEDGED,
            EHR_EXTRACT_TRANSLATED,
            CONTINUE_REQUEST_ACCEPTED,
            COPC_MESSAGE_RECEIVED,
            COPC_MESSAGE_PROCESSING,
            COPC_ACKNOWLEDGED
    );

    public static final List<MigrationStatus> GP2GP_NACK_400_ERROR_STATUSES = List.of(
            EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_MISFORMED_REQUEST
    );

    public static final List<MigrationStatus> GP2GP_NACK_404_ERROR_STATUSES = List.of(
            EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_PATIENT_NOT_REGISTERED,
            EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_NOT_PRIMARY_HEALTHCARE_PROVIDER
    );

    public static final List<MigrationStatus> GP2GP_NACK_500_ERROR_STATUSES = List.of(
            EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_EHR_GENERATION_ERROR,
            EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_MULTI_OR_NO_RESPONSES,
            EHR_EXTRACT_REQUEST_NEGATIVE_ACK_UNKNOWN,
            EHR_EXTRACT_NEGATIVE_ACK_ABA_INCORRECT_PATIENT,
            EHR_EXTRACT_NEGATIVE_ACK_NON_ABA_INCORRECT_PATIENT,
            EHR_EXTRACT_NEGATIVE_ACK_FAILED_TO_INTEGRATE,
            EHR_EXTRACT_NEGATIVE_ACK_SUPPRESSED
    );

    public static final List<MigrationStatus> GP2GP_NACK_501_ERROR_STATUSES = List.of(
            EHR_EXTRACT_REQUEST_NEGATIVE_ACK_GP2GP_SENDER_NOT_CONFIGURED
    );

    public static final List<MigrationStatus> LRG_MESSAGE_ERRORS = List.of(
            ERROR_LRG_MSG_REASSEMBLY_FAILURE,
            ERROR_LRG_MSG_ATTACHMENTS_NOT_RECEIVED,
            ERROR_LRG_MSG_GENERAL_FAILURE,
            ERROR_LRG_MSG_TIMEOUT
    );
}
