package uk.nhs.adaptors.pss.gpc.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import static io.restassured.RestAssured.given;
import static uk.nhs.adaptors.pss.gpc.utils.TestResourceUtils.readResourceAsString;

import javax.jms.Message;

import org.apache.commons.lang3.RandomStringUtils;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.model.RequestStatus;
import uk.nhs.adaptors.pss.gpc.containers.IntegrationTestsExtension;
import uk.nhs.adaptors.pss.gpc.service.FhirParser;
import uk.nhs.adaptors.pss.gpc.utils.QueueUtils;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith({SpringExtension.class, IntegrationTestsExtension.class})
@DirtiesContext
public class PatientTransferControllerIT {
    private static final String APPLICATION_FHIR_JSON_VALUE = "application/fhir+json";
    private static final String MIGRATE_PATIENT_RECORD_ENDPOINT = "/Patient/$gpc.migratestructuredrecord";
    private static final String VALID_REQUEST_BODY_PATH = "/requests/migrate-patient-record/validRequestBody.json";

    @LocalServerPort
    private int port;

    @Autowired
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Autowired
    private QueueUtils queueUtils;

    @Autowired
    private FhirParser fhirParser;

    @Test
    public void migratePatientStructuredRecord() {
        var patientNhsNumber = RandomStringUtils.randomNumeric(10, 10);
        var requestBody = getRequestBody(VALID_REQUEST_BODY_PATH, patientNhsNumber);

        var migrationRequestBefore = patientMigrationRequestDao.getMigrationRequest(patientNhsNumber);
        assertThat(migrationRequestBefore).isNull();

        verifyNoPendingMessagesPresent();

        given()
            .port(port)
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .body(requestBody)
        .when()
            .post(MIGRATE_PATIENT_RECORD_ENDPOINT)
        .then()
            .statusCode(ACCEPTED.value());

        var migrationRequestAfterFirstRequest = patientMigrationRequestDao.getMigrationRequest(patientNhsNumber);
        verifyPatientMigrationRequest(migrationRequestAfterFirstRequest, RequestStatus.RECEIVED);

        Parameters parameters = parseMessageToParametersResource(queueUtils.receiveMessage());
        assertThat(retrievePatientNhsNumber(parameters)).isEqualTo(patientNhsNumber);

        given()
            .port(port)
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .body(requestBody)
        .when()
            .post(MIGRATE_PATIENT_RECORD_ENDPOINT)
        .then()
            .statusCode(NO_CONTENT.value());

        var migrationRequestAfterSecondRequest = patientMigrationRequestDao.getMigrationRequest(patientNhsNumber);
        verifyPatientMigrationRequest(migrationRequestAfterSecondRequest, RequestStatus.RECEIVED);
        verifyNoPendingMessagesPresent();
    }

    private String getRequestBody(String path, String patientNhsNumber) {
        return readResourceAsString(path).replace("{{nhsNumber}}", patientNhsNumber);
    }

    private void verifyPatientMigrationRequest(PatientMigrationRequest patientMigrationRequest, RequestStatus status) {
        assertThat(patientMigrationRequest).isNotNull();
        assertThat(patientMigrationRequest.getRequestStatus()).isEqualTo(status);
    }

    @SneakyThrows
    private Parameters parseMessageToParametersResource(Message message) {
        return fhirParser.parseResource(message.getBody(String.class), Parameters.class);
    }

    private String retrievePatientNhsNumber(Parameters parameters) {
        var identifier = (Identifier) parameters.getParameter()
            .stream()
            .filter(it -> "patientNHSNumber".equals(it.getName()))
            .map(Parameters.ParametersParameterComponent::getValue)
            .findFirst()
            .get();

        return identifier.getValue();
    }

    private void verifyNoPendingMessagesPresent() {
        var pendingMessages = queueUtils.countPendingMessages();
        assertThat(pendingMessages).isEqualTo(0);
    }
}
