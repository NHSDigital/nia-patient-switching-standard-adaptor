package uk.nhs.adaptors.pss.gpc.service;

import static uk.nhs.adaptors.connector.model.RequestStatus.RECEIVED;

import java.time.OffsetDateTime;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.pss.gpc.amqp.PssQueuePublisher;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PatientTransferService {
    private static final String PATIENT_NHS_NUMBER_PARAM_NAME = "patientNHSNumber";

    private final FhirParser fhirParser;
    private final PatientMigrationRequestDao patientMigrationRequestDao;
    private final PssQueuePublisher pssQueuePublisher;

    public PatientMigrationRequest handlePatientMigrationRequest(Parameters parameters) {
        var patientNhsNumber = retrievePatientNhsNumber(parameters);
        PatientMigrationRequest patientMigrationRequest = patientMigrationRequestDao.getMigrationRequest(patientNhsNumber);

        if (patientMigrationRequest != null) {
            pssQueuePublisher.sendToPssQueue(fhirParser.encodeToJson(parameters));
            patientMigrationRequestDao.addNewRequest(patientNhsNumber, RECEIVED.getValue(), OffsetDateTime.now());
        }

        return patientMigrationRequest;
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
