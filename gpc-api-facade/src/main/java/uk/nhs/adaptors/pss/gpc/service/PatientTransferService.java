package uk.nhs.adaptors.pss.gpc.service;

import static uk.nhs.adaptors.connector.model.RequestStatus.RECEIVED;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatusLog;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.pss.gpc.amqp.PssQueuePublisher;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PatientTransferService {
    private static final String PATIENT_NHS_NUMBER_PARAM_NAME = "patientNHSNumber";

    private final FhirParser fhirParser;
    private final PatientMigrationRequestDao patientMigrationRequestDao;
    private final MigrationStatusLogDao migrationStatusLogDao;
    private final PssQueuePublisher pssQueuePublisher;
    private final DateUtils dateUtils;

    public MigrationStatusLog handlePatientMigrationRequest(Parameters parameters) {
        var patientNhsNumber = retrievePatientNhsNumber(parameters);
        PatientMigrationRequest patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(patientNhsNumber);
        MigrationStatusLog migrationStatusLog = migrationStatusLogDao.getMigrationStatusLog(patientNhsNumber);

        if (patientMigrationRequest == null) {
            pssQueuePublisher.sendToPssQueue(fhirParser.encodeToJson(parameters));
            patientMigrationRequestDao.addNewRequest(patientNhsNumber);

            int addedId = patientMigrationRequestDao.getMigrationRequestId(patientNhsNumber);
            migrationStatusLogDao.addMigrationStatusLog(patientNhsNumber, RECEIVED.name(), dateUtils.getCurrentOffsetDateTime(), addedId);
        }

        return migrationStatusLog;
    }

    public String getEmptyBundle() {
        return fhirParser.encodeToJson(new Bundle());
    }

    private String retrievePatientNhsNumber(Parameters parameters) {
        var identifier = (Identifier) parameters.getParameter()
            .stream()
            .filter(it -> PATIENT_NHS_NUMBER_PARAM_NAME.equals(it.getName()))
            .map(Parameters.ParametersParameterComponent::getValue)
            .findFirst()
            .get();

        return identifier.getValue();
    }
}
