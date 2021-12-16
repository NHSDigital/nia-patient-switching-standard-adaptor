package uk.nhs.adaptors.pss.gpc.service;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.RequestStatus;
import uk.nhs.adaptors.pss.gpc.amqp.PssQueuePublisher;
import uk.nhs.adaptors.pss.gpc.exception.FhirValidationException;

@Component
@AllArgsConstructor()
public class MigratePatientStructuredRecordService {
    private static final String PATIENT_NHS_NUMBER_PARAM_NAME = "patientNHSNumber";

    private final FhirParseService fhirParseService;
    private final PatientMigrationRequestDao patientMigrationRequestDao;
    private final PssQueuePublisher pssQueuePublisher;

    public ResponseEntity<String> handlePatientMigrationRequest(String body) {
        var parameters = fhirParseService.parseResource(body, Parameters.class);
        var patientNhsNumber = retrievePatientNhsNumber(parameters);
        var isRequestInProgress = patientMigrationRequestDao.isRequestInProgress(patientNhsNumber);
        var responseBody = fhirParseService.parseResourceToString(new Bundle());

        if (isRequestInProgress) {
            return new ResponseEntity<>(responseBody, HttpStatus.NO_CONTENT);
        } else {
            pssQueuePublisher.sendToPssQueue(body);
            patientMigrationRequestDao.addNewRequest(patientNhsNumber, RequestStatus.RECEIVED.getValue());
            return new ResponseEntity<>(responseBody, HttpStatus.ACCEPTED);
        }
    }

    private String retrievePatientNhsNumber(Parameters parameters) {
        var identifier = (Identifier) parameters.getParameter()
            .stream()
            .filter(it -> PATIENT_NHS_NUMBER_PARAM_NAME.equals(it.getName()))
            .map(Parameters.ParametersParameterComponent::getValue)
            .findFirst()
            .orElseThrow(() ->
                new FhirValidationException(PATIENT_NHS_NUMBER_PARAM_NAME + " parameter is missing or invalid"));

        return identifier.getValue();
    }
}
