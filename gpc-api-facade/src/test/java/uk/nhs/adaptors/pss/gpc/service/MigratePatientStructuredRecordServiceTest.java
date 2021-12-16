package uk.nhs.adaptors.pss.gpc.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.RequestStatus;
import uk.nhs.adaptors.pss.gpc.GpcFacadeApplication;
import uk.nhs.adaptors.pss.gpc.amqp.PssQueuePublisher;

@SpringBootTest(classes = {GpcFacadeApplication.class})
public class MigratePatientStructuredRecordServiceTest {
    private static final String REQUEST_BODY = "{testBody}";
    private static final String RESPONSE_BODY = "{responseBody}";
    private static final String PATIENT_NHS_NUMBER = "123456789";

    @Mock
    private FhirParseService fhirParseService;

    @Mock
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Mock
    private PssQueuePublisher pssQueuePublisher;

    @InjectMocks
    private MigratePatientStructuredRecordService service;

    @BeforeEach
    void setUp() {
        Parameters parameters = createParametersResource();
        when(fhirParseService.parseResource(REQUEST_BODY, Parameters.class)).thenReturn(parameters);
        when(fhirParseService.parseResourceToString(any(Bundle.class))).thenReturn(RESPONSE_BODY);
    }

    @Test
    public void handlePatientMigrationRequestWhenRequestIsNew() {
        when(patientMigrationRequestDao.isRequestInProgress(PATIENT_NHS_NUMBER)).thenReturn(false);

        ResponseEntity<String> responseEntity = service.handlePatientMigrationRequest(REQUEST_BODY);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isEqualTo(RESPONSE_BODY);
        verify(pssQueuePublisher).sendToPssQueue(REQUEST_BODY);
        verify(patientMigrationRequestDao).addNewRequest(PATIENT_NHS_NUMBER, RequestStatus.RECEIVED.getValue());
    }

    @Test
    public void handlePatientMigrationRequestWhenRequestIsInProgress() {
        when(patientMigrationRequestDao.isRequestInProgress(PATIENT_NHS_NUMBER)).thenReturn(true);

        ResponseEntity<String> responseEntity = service.handlePatientMigrationRequest(REQUEST_BODY);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseEntity.getBody()).isEqualTo(RESPONSE_BODY);
        verifyNoInteractions(pssQueuePublisher);
        verify(patientMigrationRequestDao).isRequestInProgress(PATIENT_NHS_NUMBER);
        verifyNoMoreInteractions(patientMigrationRequestDao);
    }

    private Parameters createParametersResource() {
        Parameters parameters = new Parameters();

        Parameters.ParametersParameterComponent nhsNumberComponent = new Parameters.ParametersParameterComponent();
        nhsNumberComponent.setName("patientNHSNumber");
        Identifier identifier = new Identifier();
        identifier
            .setSystem("https://fhir.nhs.uk/Id/nhs-number")
            .setValue(PATIENT_NHS_NUMBER);
        nhsNumberComponent.setValue(identifier);

        BooleanType booleanType = new BooleanType();
        booleanType.setValue(true);
        Parameters.ParametersParameterComponent sensitiveInformationPart = new Parameters.ParametersParameterComponent();
        sensitiveInformationPart
            .setName("includeSensitiveInformation")
            .setValue(booleanType);
        Parameters.ParametersParameterComponent fullRecordComponent = new Parameters.ParametersParameterComponent();
        fullRecordComponent
            .setName("includeFullRecord")
            .addPart(sensitiveInformationPart);

        parameters
            .addParameter(nhsNumberComponent)
            .addParameter(fullRecordComponent);

        return parameters;
    }
}
