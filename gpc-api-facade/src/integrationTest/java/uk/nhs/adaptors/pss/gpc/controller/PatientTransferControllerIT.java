package uk.nhs.adaptors.pss.gpc.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import static io.restassured.RestAssured.given;
import static uk.nhs.adaptors.pss.gpc.utils.TestResourceUtils.readResourceAsString;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.model.RequestStatus;
import uk.nhs.adaptors.pss.gpc.containers.ActiveMqExtension;
import uk.nhs.adaptors.pss.gpc.service.FhirParser;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith({SpringExtension.class, ActiveMqExtension.class})
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
    private FhirParser fhirParser;

    @Test
    public void sendNewPatientTransferRequest() {
        var patientNhsNumber = RandomStringUtils.randomNumeric(10, 10);
        var requestBody = getRequestBody(VALID_REQUEST_BODY_PATH, patientNhsNumber);

        var migrationRequestBefore = patientMigrationRequestDao.getMigrationRequest(patientNhsNumber);
        assertThat(migrationRequestBefore).isNull();

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
    }

    @Test
    public void sendPatientTransferRequestWhenTransferIsAlreadyInProgress() {
        var patientNhsNumber = RandomStringUtils.randomNumeric(10, 10);
        var requestBody = getRequestBody(VALID_REQUEST_BODY_PATH, patientNhsNumber);

        var migrationRequestBefore = patientMigrationRequestDao.getMigrationRequest(patientNhsNumber);
        assertThat(migrationRequestBefore).isNull();

        given()
            .port(port)
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .body(requestBody)
        .when()
            .post(MIGRATE_PATIENT_RECORD_ENDPOINT)
        .then()
            .statusCode(ACCEPTED.value());

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
    }

    private String getRequestBody(String path, String patientNhsNumber) {
        return readResourceAsString(path).replace("{{nhsNumber}}", patientNhsNumber);
    }

    private void verifyPatientMigrationRequest(PatientMigrationRequest patientMigrationRequest, RequestStatus status) {
        assertThat(patientMigrationRequest).isNotNull();
        assertThat(patientMigrationRequest.getRequestStatus()).isEqualTo(status);
    }
}
