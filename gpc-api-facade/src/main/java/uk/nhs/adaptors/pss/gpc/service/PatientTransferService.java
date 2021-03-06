package uk.nhs.adaptors.pss.gpc.service;

import static uk.nhs.adaptors.connector.model.MigrationStatus.REQUEST_RECEIVED;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.FROM_ODS;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ASID;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.TO_ODS;
import static uk.nhs.adaptors.pss.gpc.util.fhir.ParametersUtils.getNhsNumberFromParameters;

import java.util.Map;

import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.common.model.TransferRequestMessage;
import uk.nhs.adaptors.common.service.MDCService;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.pss.gpc.amqp.PssQueuePublisher;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PatientTransferService {
    private final FhirParser fhirParser;
    private final PatientMigrationRequestDao patientMigrationRequestDao;
    private final MigrationStatusLogDao migrationStatusLogDao;
    private final PssQueuePublisher pssQueuePublisher;
    private final DateUtils dateUtils;
    private final MDCService mdcService;

    public MigrationStatusLog handlePatientMigrationRequest(Parameters parameters, Map<String, String> headers) {
        var conversationId = mdcService.getConversationId();
        PatientMigrationRequest patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(conversationId);

        if (patientMigrationRequest == null) {
            var patientNhsNumber = getNhsNumberFromParameters(parameters).get().getValue();
            patientMigrationRequestDao.addNewRequest(patientNhsNumber, conversationId, headers.get(TO_ODS), headers.get(FROM_ODS));

            int addedId = patientMigrationRequestDao.getMigrationRequestId(conversationId);
            migrationStatusLogDao.addMigrationStatusLog(REQUEST_RECEIVED, dateUtils.getCurrentOffsetDateTime(), addedId);

            var pssMessage = createTransferRequestMessage(patientNhsNumber, headers, conversationId);
            pssQueuePublisher.sendToPssQueue(pssMessage);
        } else {
            return migrationStatusLogDao.getLatestMigrationStatusLog(patientMigrationRequest.getId());
        }
        return null;
    }

    public String getBundleResource() {
        var conversationId = mdcService.getConversationId();
        PatientMigrationRequest patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(conversationId);
        return patientMigrationRequest.getBundleResource();
    }

    private TransferRequestMessage createTransferRequestMessage(String patientNhsNumber, Map<String, String> headers,
        String conversationId) {
        return TransferRequestMessage.builder()
            .conversationId(conversationId)
            .patientNhsNumber(patientNhsNumber)
            .toAsid(headers.get(TO_ASID))
            .fromAsid(headers.get(FROM_ASID))
            .toOds(headers.get(TO_ODS))
            .fromOds(headers.get(FROM_ODS))
            .build();
    }
}
