package uk.nhs.adaptors.pss.translator.mapper.medication;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@ExtendWith(MockitoExtension.class)
public class MedicationRequestMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/MedicationStatement/";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final int SINGLE_INVOCATION = 1;
    private static final int DOUBLE_INVOCATION = 2;
    private static final int EXPECTED_RESOURCES_MAPPED = 6;

    private static final DateTimeType EXPECTED_DATE_TIME_TYPE = DateFormatUtil.parseToDateTimeType("20100115");

    private static final String PATIENT_ID = "d7d4ab01-c3a9-4120-9364-4a5b3fd614d0";

    @Mock
    private MedicationMapper medicationMapper;
    @Mock
    private MedicationRequestOrderMapper medicationRequestOrderMapper;
    @Mock
    private MedicationRequestPlanMapper medicationRequestPlanMapper;
    @Mock
    private MedicationStatementMapper medicationStatementMapper;
    @Mock
    private MedicationMapperContext medicationMapperContext;

    @InjectMocks
    private MedicationRequestMapper medicationRequestMapper;

    @Test
    public void When_MappingMedicationStatement_Expect_CorrectMappersToBeCalled() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract1.xml");

        when(medicationRequestPlanMapper.mapToPlanMedicationRequest(any(), any(), any(), any()))
                .thenReturn(new MedicationRequest());
        when(medicationRequestOrderMapper.mapToOrderMedicationRequest(any(), any(), any(), any()))
                .thenReturn(new MedicationRequest());
        when(medicationStatementMapper.mapToMedicationStatement(any(), any(), any(), any(), any()))
                .thenReturn(new MedicationStatement());
        when(medicationMapper.createMedication(any()))
                .thenReturn(new Medication());

        var resources = medicationRequestMapper
                .mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(), PRACTISE_CODE);

        verify(medicationRequestPlanMapper, times(DOUBLE_INVOCATION))
                .mapToPlanMedicationRequest(any(), any(), any(), any());
        verify(medicationRequestOrderMapper, times(SINGLE_INVOCATION))
                .mapToOrderMedicationRequest(any(), any(), any(), any());
        verify(medicationStatementMapper, times(DOUBLE_INVOCATION))
                .mapToMedicationStatement(any(), any(), any(), any(), any());
        verify(medicationMapper, times(SINGLE_INVOCATION))
                .createMedication(any());

        assertThat(resources).hasSize(EXPECTED_RESOURCES_MAPPED);
        resources
            .stream()
            .filter(resource -> ResourceType.MedicationRequest.equals(resource.getResourceType()))
            .map(MedicationRequest.class::cast)
            .forEach(medicationRequest -> {
                assertThat(medicationRequest.getSubject().getResource().getIdElement().getIdPart()).isEqualTo(PATIENT_ID);
            });

        resources
            .stream()
            .filter(resource -> ResourceType.MedicationStatement.equals(resource.getResourceType()))
            .map(MedicationStatement.class::cast)
            .forEach(medicationStatement -> {
                assertThat(medicationStatement.getSubject().getResource().getIdElement().getIdPart()).isEqualTo(PATIENT_ID);
                assertThat(medicationStatement.getDateAssertedElement().getValue())
                    .isEqualTo(EXPECTED_DATE_TIME_TYPE.getValue());
            });
    }

    @Test
    public void When_MappingMedicationRequestWithAvailabilityTimeInMedicationStatement_Expect_UseThatAvailabilityTime() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract_AvailabilityTimeSetInMedicationStatement.xml");
        var expectedAuthoredOn = DateFormatUtil.parseToDateTimeType("20100116");

        when(medicationRequestPlanMapper.mapToPlanMedicationRequest(any(), any(), any(), any()))
                .thenReturn(new MedicationRequest());
        when(medicationRequestOrderMapper.mapToOrderMedicationRequest(any(), any(), any(), any()))
                .thenReturn(new MedicationRequest());
        when(medicationStatementMapper.mapToMedicationStatement(any(), any(), any(), any(), any()))
                .thenReturn(new MedicationStatement());
        when(medicationMapper.createMedication(any()))
                .thenReturn(new Medication());

        var resources = medicationRequestMapper
                .mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(), PRACTISE_CODE);

        assertThat(resources).hasSize(EXPECTED_RESOURCES_MAPPED);

        var medicationRequest = resources
                .stream()
                        .filter(resource -> ResourceType.MedicationRequest.equals(resource.getResourceType()))
                        .map(MedicationRequest.class::cast)
                        .findFirst()
                .get();

        assertThat(medicationRequest.getAuthoredOnElement().getValue()).isEqualTo(expectedAuthoredOn.getValue());
    }

    @Test
    public void When_MappingMedicationRequestWithAvailabilityTimeNotInMedicationStatement_Expect_AuthoredOnMapped() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract_AvailabilityTimeNotInMedicationStatement.xml");
        var expectedAuthoredOn = DateFormatUtil.parseToDateTimeType("20100117");

        when(medicationRequestPlanMapper.mapToPlanMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationRequestOrderMapper.mapToOrderMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationStatementMapper.mapToMedicationStatement(any(), any(), any(), any(), any())).thenReturn(new MedicationStatement());
        when(medicationMapper.createMedication(any())).thenReturn(new Medication());

        var resources = medicationRequestMapper.mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(),
                PRACTISE_CODE);

        assertThat(resources).hasSize(EXPECTED_RESOURCES_MAPPED);

        var medicationRequest = resources
                .stream()
                .filter(resource -> ResourceType.MedicationRequest.equals(resource.getResourceType()))
                .map(MedicationRequest.class::cast)
                .findFirst()
                .get();

        assertThat(medicationRequest.getAuthoredOnElement().getValue()).isEqualTo(expectedAuthoredOn.getValue());
    }

    @Test
    public void When_MappingMedicationRequestWithAvailabilityTimeInEhrCompositionAuthor_Expect_UseThatAvailabilityTime() {
        final int expectedResourcesMapped = 6;
        final DateTimeType expectedAvailabilityTime = DateFormatUtil.parseToDateTimeType("20220101010101");

        var ehrExtract = unmarshallEhrExtract("ehrExtract_hasAuthorTime.xml");

        when(medicationRequestPlanMapper.mapToPlanMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationRequestOrderMapper.mapToOrderMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationStatementMapper.mapToMedicationStatement(any(), any(), any(), any(), any())).thenReturn(new MedicationStatement());
        when(medicationMapper.createMedication(any())).thenReturn(new Medication());

        var resources = medicationRequestMapper.mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(),
                PRACTISE_CODE);

        assertThat(resources).hasSize(expectedResourcesMapped);

        resources
                .stream()
                .filter(resource -> ResourceType.MedicationStatement.equals(resource.getResourceType()))
                .map(MedicationStatement.class::cast)
                .forEach(medicationStatement -> {
                    assertThat(medicationStatement.getDateAssertedElement().getValue()).isEqualTo(expectedAvailabilityTime.getValue());
                });
    }

    @Test
    public void When_MappingMedicationRequestWithNoEhrCompositionAuthorTime_Expect_UseEhrExtractAvailabilityTime() {
        final int expectedResourcesMapped = 6;
        final DateTimeType expectedAvailabilityTime = DateFormatUtil.parseToDateTimeType("20100115");

        var ehrExtract = unmarshallEhrExtract("ehrExtract_hasNoAuthorTime.xml");

        when(medicationRequestPlanMapper.mapToPlanMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationRequestOrderMapper.mapToOrderMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationStatementMapper.mapToMedicationStatement(any(), any(), any(), any(), any())).thenReturn(new MedicationStatement());
        when(medicationMapper.createMedication(any())).thenReturn(new Medication());

        var resources = medicationRequestMapper.mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(),
                PRACTISE_CODE);

        assertThat(resources).hasSize(expectedResourcesMapped);

        resources
                .stream()
                .filter(resource -> ResourceType.MedicationStatement.equals(resource.getResourceType()))
                .map(MedicationStatement.class::cast)
                .forEach(medicationStatement -> {
                    assertThat(medicationStatement.getDateAssertedElement().getValue()).isEqualTo(expectedAvailabilityTime.getValue());
                });
    }

    @Test
    public void When_MappingMedicationRequestWithNoAuthoredOn_Expect_NullAuthoredOn() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract_AvailabilityTimeNotInMedicationStatementOrEhrComposition.xml");

        when(medicationRequestPlanMapper.mapToPlanMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationRequestOrderMapper.mapToOrderMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationStatementMapper.mapToMedicationStatement(any(), any(), any(), any(), any())).thenReturn(new MedicationStatement());
        when(medicationMapper.createMedication(any())).thenReturn(new Medication());

        var resources = medicationRequestMapper.mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(),
                PRACTISE_CODE);

        assertThat(resources).hasSize(EXPECTED_RESOURCES_MAPPED);

        var medicationRequest = resources
                .stream()
                .filter(resource -> ResourceType.MedicationRequest.equals(resource.getResourceType()))
                .map(MedicationRequest.class::cast)
                .findFirst()
                .get();

        assertThat(medicationRequest.getAuthoredOnElement().getValue()).isNull();
    }

    @Test
    public void When_MappingMedicationRequestWithAuthoredOnValidDate_Expect_AuthoredOnToUseMedicationAvailabilityTime() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract_hasAuthorTime.xml");
        var expectedAuthoredOn = DateFormatUtil.parseToDateTimeType("20100115");

        when(medicationRequestPlanMapper.mapToPlanMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationRequestOrderMapper.mapToOrderMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationStatementMapper.mapToMedicationStatement(any(), any(), any(), any(), any())).thenReturn(new MedicationStatement());
        when(medicationMapper.createMedication(any())).thenReturn(new Medication());

        var resources = medicationRequestMapper.mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(),
                PRACTISE_CODE);

        assertThat(resources).hasSize(EXPECTED_RESOURCES_MAPPED);

        var medicationRequest = resources
                .stream()
                .filter(resource -> ResourceType.MedicationRequest.equals(resource.getResourceType()))
                .map(MedicationRequest.class::cast)
                .findFirst()
                .get();

        assertThat(medicationRequest.getAuthoredOnElement().getValue()).isEqualTo(expectedAuthoredOn.getValue());
    }

    @Test
    public void When_MappingMedicationRequestWithAuthoredOnInExtractAndComposition_Expect_AuthoredOnToUseAvailabilityTimeInStatement() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract_hasAuthorTimeInExtract.xml");
        var expectedAuthoredOn = DateFormatUtil.parseToDateTimeType("20100115");

        when(medicationRequestPlanMapper.mapToPlanMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationRequestOrderMapper.mapToOrderMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationStatementMapper.mapToMedicationStatement(any(), any(), any(), any(), any())).thenReturn(new MedicationStatement());
        when(medicationMapper.createMedication(any())).thenReturn(new Medication());

        var resources = medicationRequestMapper.mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(),
                PRACTISE_CODE);

        assertThat(resources).hasSize(EXPECTED_RESOURCES_MAPPED);

        var medicationRequest = resources
                .stream()
                .filter(resource -> ResourceType.MedicationRequest.equals(resource.getResourceType()))
                .map(MedicationRequest.class::cast)
                .findFirst()
                .get();

        assertThat(medicationRequest.getAuthoredOnElement().getValue()).isEqualTo(expectedAuthoredOn.getValue());
    }

    @Test
    public void When_MappingMedicationRequestWithAuthoredOnValidDateInExtractOnly_Expect_AuthoredOnToUseAvailabilityTimeInStatement() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract_hasAuthorTimeInExtractOnly.xml");
        var expectedAuthoredOn = DateFormatUtil.parseToDateTimeType("20100115");

        when(medicationRequestPlanMapper.mapToPlanMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationRequestOrderMapper.mapToOrderMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationStatementMapper.mapToMedicationStatement(any(), any(), any(), any(), any())).thenReturn(new MedicationStatement());
        when(medicationMapper.createMedication(any())).thenReturn(new Medication());

        var resources = medicationRequestMapper.mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(),
                PRACTISE_CODE);

        assertThat(resources).hasSize(EXPECTED_RESOURCES_MAPPED);

        var medicationRequest = resources
                .stream()
                .filter(resource -> ResourceType.MedicationRequest.equals(resource.getResourceType()))
                .map(MedicationRequest.class::cast)
                .findFirst()
                .get();

        assertThat(medicationRequest.getAuthoredOnElement().getValue()).isEqualTo(expectedAuthoredOn.getValue());
    }

    @Test
    public void When_MedicationRequestMapperThrowsException_Expect_MedicationMapperContextToBeReset() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract1.xml");

        when(medicationRequestOrderMapper.mapToOrderMedicationRequest(any(), any(), any(), any()))
                .thenThrow(NullPointerException.class);

        assertThrows(
                NullPointerException.class,
                () -> medicationRequestMapper.mapResources(
                        ehrExtract,
                        (Patient) new Patient().setId(PATIENT_ID),
                        List.of(),
                        PRACTISE_CODE));

        verify(medicationMapperContext).reset();
    }

    @Test
    public void When_MedicationRequestMapperCompletesSuccessfully_Expect_MedicationMapperContextToBeReset() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract1.xml");

        medicationRequestMapper
                .mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(), PRACTISE_CODE);

        verify(medicationMapperContext).reset();
    }

    @SneakyThrows
    private RCMRMT030101UKEhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UKEhrExtract.class);
    }
}
