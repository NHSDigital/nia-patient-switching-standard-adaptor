package uk.nhs.adaptors.pss.gpc.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static uk.nhs.adaptors.pss.gpc.utils.TestResourceUtils.readResourceAsString;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.model.RequestStatus;
import uk.nhs.adaptors.pss.gpc.containers.ActiveMqExtension;
import uk.nhs.adaptors.pss.gpc.service.FhirParser;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith({SpringExtension.class, ActiveMqExtension.class})
@DirtiesContext
@AutoConfigureMockMvc
public class PatientTransferControllerIT {
    private static final int NHS_NUMBER_MIN_MAX_LENGTH = 10;
    private static final String APPLICATION_FHIR_JSON_VALUE = "application/fhir+json";
    private static final String MIGRATE_PATIENT_RECORD_ENDPOINT = "/Patient/$gpc.migratestructuredrecord";
    private static final String VALID_REQUEST_BODY_PATH = "/requests/migrate-patient-record/validRequestBody.json";

    @Autowired
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Autowired
    private FhirParser fhirParser;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void sendNewPatientTransferRequest() throws Exception {
        var patientNhsNumber = generatePatientNhsNumber();
        var requestBody = getRequestBody(VALID_REQUEST_BODY_PATH, patientNhsNumber);

        var migrationRequestBefore = patientMigrationRequestDao.getMigrationRequest(patientNhsNumber);
        assertThat(migrationRequestBefore).isNull();

        mockMvc.perform(
            post(MIGRATE_PATIENT_RECORD_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .content(requestBody))
            .andExpect(status().isAccepted());

        var migrationRequestAfterFirstRequest = patientMigrationRequestDao.getMigrationRequest(patientNhsNumber);
        verifyPatientMigrationRequest(migrationRequestAfterFirstRequest, RequestStatus.RECEIVED);
    }

    @Test
    public void sendPatientTransferRequestWhenTransferIsAlreadyInProgress() throws Exception {
        var patientNhsNumber = generatePatientNhsNumber();
        var requestBody = getRequestBody(VALID_REQUEST_BODY_PATH, patientNhsNumber);

        var migrationRequestBefore = patientMigrationRequestDao.getMigrationRequest(patientNhsNumber);
        assertThat(migrationRequestBefore).isNull();

        mockMvc.perform(
            post(MIGRATE_PATIENT_RECORD_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .content(requestBody))
            .andExpect(status().isAccepted());

        mockMvc.perform(
            post(MIGRATE_PATIENT_RECORD_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .content(requestBody))
            .andExpect(status().isNoContent());

        var migrationRequestAfterSecondRequest = patientMigrationRequestDao.getMigrationRequest(patientNhsNumber);
        verifyPatientMigrationRequest(migrationRequestAfterSecondRequest, RequestStatus.RECEIVED);
    }

    @Test
    public void sendPatientTransferRequestWithIncorrectContentTypeHeader() throws Exception {
        var requestBody = getRequestBody(VALID_REQUEST_BODY_PATH, generatePatientNhsNumber());
        var expectedResponseBody = readResourceAsString("/responses/common/unsupportedMediaTypeResponseBody.json");

        mockMvc.perform(
            post(MIGRATE_PATIENT_RECORD_ENDPOINT)
                .contentType(MediaType.TEXT_PLAIN)
                .content(requestBody))
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(content().json(expectedResponseBody));
    }

    @Test
    public void sendPatientTransferRequestToNonexistentEndpoint() throws Exception {
        var requestBody = getRequestBody(VALID_REQUEST_BODY_PATH, generatePatientNhsNumber());
        var nonexistentEndpoint = "/Patient/$gpc.migrateCatRecord";
        var expectedResponseBody = readResourceAsString("/responses/common/notFoundResponseBody.json")
            .replace("{{endpointUrl}}", nonexistentEndpoint);

        mockMvc.perform(
            post(nonexistentEndpoint)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(content().json(expectedResponseBody));
    }

    @Test
    public void sendPatientTransferRequestUsingIncorrectMethod() throws Exception {
        var requestBody = getRequestBody(VALID_REQUEST_BODY_PATH, generatePatientNhsNumber());
        var expectedResponseBody = readResourceAsString("/responses/common/methodNotAllowedResponseBody.json")
            .replace("{{requestMethod}}", "PATCH");

        mockMvc.perform(
            patch(MIGRATE_PATIENT_RECORD_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .content(requestBody))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().json(expectedResponseBody));
    }

    @Test
    public void sendPatientTransferRequestWithoutNhsNumber() throws Exception {
        var requestBody = readResourceAsString("/requests/migrate-patient-record/missingNhsNumberRequestBody.json");
        var expectedResponseBody = readResourceAsString(
            "/responses/migrate-patient-record/unprocessableEntityMissingNhsNumberResponseBody.json");

        mockMvc.perform(
            post(MIGRATE_PATIENT_RECORD_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .content(requestBody))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().json(expectedResponseBody));
    }

    @Test
    public void sendPatientTransferRequestWithInvalidBody() throws Exception {
        var requestBody = getRequestBody("/requests/migrate-patient-record/invalidRequestBody.json", generatePatientNhsNumber());
        var expectedResponseBody = readResourceAsString(
            "/responses/migrate-patient-record/unprocessableEntityInvalidJsonResponseBody.json");

        mockMvc.perform(
            post(MIGRATE_PATIENT_RECORD_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .content(requestBody))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().json(expectedResponseBody));
    }

    @Test
    public void sendPatientTransferRequestWithEmptyBody() throws Exception {
        var expectedResponseBody = readResourceAsString(
            "/responses/migrate-patient-record/unprocessableEntityEmptyBodyResponseBody.json");

        mockMvc.perform(
            post(MIGRATE_PATIENT_RECORD_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .content(StringUtils.EMPTY))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().json(expectedResponseBody));
    }

    private String getRequestBody(String path, String patientNhsNumber) {
        return readResourceAsString(path).replace("{{nhsNumber}}", patientNhsNumber);
    }

    private void verifyPatientMigrationRequest(PatientMigrationRequest patientMigrationRequest, RequestStatus status) {
        assertThat(patientMigrationRequest).isNotNull();
        assertThat(patientMigrationRequest.getRequestStatus()).isEqualTo(status);
    }

    private String generatePatientNhsNumber() {
        return RandomStringUtils.randomNumeric(NHS_NUMBER_MIN_MAX_LENGTH, NHS_NUMBER_MIN_MAX_LENGTH);
    }
}
