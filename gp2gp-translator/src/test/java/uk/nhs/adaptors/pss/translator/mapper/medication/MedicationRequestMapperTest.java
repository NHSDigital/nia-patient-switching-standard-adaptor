package uk.nhs.adaptors.pss.translator.mapper.medication;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UKAuthorise;
import org.hl7.v3.RCMRMT030101UKConsumable;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKMedicationStatement;
import org.hl7.v3.RCMRMT030101UKPrescribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestDispenseRequestComponent;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestIntent;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.Date;
import java.util.List;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@ExtendWith(MockitoExtension.class)
public class MedicationRequestMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/MedicationStatement/";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final int SINGLE_INVOCATION = 1;
    private static final int EXPECTED_RESOURCES_MAPPED = 6;

    private static final DateTimeType EXPECTED_DATE_TIME_TYPE = DateFormatUtil.parseToDateTimeType("20100115");

    private static final String PATIENT_ID = "d7d4ab01-c3a9-4120-9364-4a5b3fd614d0";

    private static final Extension ACUTE_PRESCRIPTION_EXTENSION = new Extension(
        "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1",
        new CodeableConcept(
            new Coding("https://fhir.nhs.uk/STU3/CodeSystem/CareConnect-PrescriptionType-1",
                "acute",
                "Acute"
            )
        )
    );
    private static final Extension REPEAT_PRESCRIPTION_EXTENSION = new Extension(
        "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1",
        new CodeableConcept(
            new Coding("https://fhir.nhs.uk/STU3/CodeSystem/CareConnect-PrescriptionType-1",
                "repeat",
                "Repeat"
            )
        )
    );
    private static final Reference REFERENCE_TO_PLAN = new Reference(
        new IdType(
            ResourceType.MedicationRequest.name(),
            "000EEA41-289B-4B1C-A5AB-421A666A0D2C"
        )
    );

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

    @BeforeEach
    void beforeEach() {
        setupCommonStubs();
    }

    @Test
    public void When_MappingMedicationStatement_Expect_CorrectMappersToBeCalled() {
        final RCMRMT030101UKEhrExtract ehrExtract = unmarshallEhrExtract("ehrExtract1.xml");
        final RCMRMT030101UKEhrComposition ehrComposition =
                ehrExtract.getComponent().getFirst().getEhrFolder().getComponent().getFirst().getEhrComposition();
        final RCMRMT030101UKMedicationStatement medicationStatement =
                ehrComposition.getComponent().getFirst().getMedicationStatement();

        var resources = medicationRequestMapper
                .mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(), PRACTISE_CODE);

        verify(medicationRequestPlanMapper, times(SINGLE_INVOCATION)).mapToPlanMedicationRequest(
            eq(ehrExtract),
            eq(ehrComposition),
            eq(medicationStatement),
            eq(medicationStatement.getComponent().getFirst().getEhrSupplyAuthorise()),
            eq(PRACTISE_CODE)
        );

        verify(medicationRequestOrderMapper, times(SINGLE_INVOCATION)).mapToOrderMedicationRequest(
            eq(ehrExtract),
            eq(ehrComposition),
            eq(medicationStatement),
            eq(medicationStatement.getComponent().get(2).getEhrSupplyPrescribe()),
            eq(PRACTISE_CODE)
        );

        verify(medicationStatementMapper, times(SINGLE_INVOCATION)).mapToMedicationStatement(
            eq(ehrExtract),
            eq(ehrComposition),
            eq(medicationStatement),
            eq(medicationStatement.getComponent().getFirst().getEhrSupplyAuthorise()),
            eq(PRACTISE_CODE),
            any(DateTimeType.class)
        );

        verify(medicationMapper, times(SINGLE_INVOCATION))
                .createMedication(any());

        assertThat(resources).hasSize(EXPECTED_RESOURCES_MAPPED);

        assertMedicationRequests(resources);
        assertMedicationStatements(resources);
    }

    @Test
    public void When_MappingMedicationRequestWithAvailabilityTimeInMedicationStatement_Expect_UseThatAvailabilityTime() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract_AvailabilityTimeSetInMedicationStatement.xml");
        var expectedAuthoredOn = DateFormatUtil.parseToDateTimeType("20100116");

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

        var resources = medicationRequestMapper.mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(),
                PRACTISE_CODE);

        assertThat(resources).hasSize(expectedResourcesMapped);

        resources
                .stream()
                .filter(resource -> ResourceType.MedicationStatement.equals(resource.getResourceType()))
                .map(MedicationStatement.class::cast)
                .forEach(medicationStatement ->
                    assertThat(medicationStatement.getDateAssertedElement().getValue()).isEqualTo(expectedAvailabilityTime.getValue()));
    }

    @Test
    public void When_MappingMedicationRequestWithNoEhrCompositionAuthorTime_Expect_UseEhrExtractAvailabilityTime() {
        final int expectedResourcesMapped = 6;
        final DateTimeType expectedAvailabilityTime = DateFormatUtil.parseToDateTimeType("20100115");

        var ehrExtract = unmarshallEhrExtract("ehrExtract_hasNoAuthorTime.xml");

        var resources = medicationRequestMapper.mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(),
                PRACTISE_CODE);

        assertThat(resources).hasSize(expectedResourcesMapped);

        resources
                .stream()
                .filter(resource -> ResourceType.MedicationStatement.equals(resource.getResourceType()))
                .map(MedicationStatement.class::cast)
                .forEach(medicationStatement ->
                        assertThat(medicationStatement.getDateAssertedElement().getValue()).isEqualTo(expectedAvailabilityTime.getValue()));
    }

    @Test
    public void When_MappingMedicationRequestWithNoAuthoredOn_Expect_NullAuthoredOn() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract_AvailabilityTimeNotInMedicationStatementOrEhrComposition.xml");

        when(medicationStatementMapper.mapToMedicationStatement(
            any(RCMRMT030101UKEhrExtract.class),
            any(RCMRMT030101UKEhrComposition.class),
            any(RCMRMT030101UKMedicationStatement.class),
            any(RCMRMT030101UKAuthorise.class),
            any(String.class),
            eq(null)
        )).thenReturn(new MedicationStatement());

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
    void When_MedicationStatementsWithMultipleSupplyPrescribeBasedOnTheSameRepeatSupplyAuthorise_Expect_NoAdditionalPlanCreated() {
        var ehrExtract = unmarshallEhrExtract(
            "ehrExtract_MultipleSupplyPrescribeInFulfilmentOfSingleNonAcuteSupplyAuthorise.xml"
        );
        when(
            medicationRequestPlanMapper.mapToPlanMedicationRequest(
                eq(ehrExtract),
                any(RCMRMT030101UKEhrComposition.class),
                any(RCMRMT030101UKMedicationStatement.class),
                any(RCMRMT030101UKAuthorise.class),
                any(String.class)
            )
        ).thenReturn(
            (MedicationRequest) new MedicationRequest()
                .setIntent(MedicationRequestIntent.PLAN)
                .setExtension(List.of(REPEAT_PRESCRIPTION_EXTENSION))
                .setId("000EEA41-289B-4B1C-A5AB-421A666A0D2C")

        );
        when(
            medicationRequestOrderMapper.mapToOrderMedicationRequest(
                eq(ehrExtract),
                any(RCMRMT030101UKEhrComposition.class),
                any(RCMRMT030101UKMedicationStatement.class),
                any(RCMRMT030101UKPrescribe.class),
                any(String.class)
            )
        ).thenReturn(
            new MedicationRequest()
                .setIntent(MedicationRequestIntent.ORDER)
                .addBasedOn(REFERENCE_TO_PLAN)
        );

        var resources = medicationRequestMapper
            .mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(), PRACTISE_CODE
            );

        var planMedicationRequests = resources.stream()
            .filter(MedicationRequest.class::isInstance)
            .map(MedicationRequest.class::cast)
            .filter(medicationRequest -> MedicationRequestIntent.PLAN.equals(medicationRequest.getIntent()));

        var orderMedicationRequests = resources.stream()
            .filter(MedicationRequest.class::isInstance)
            .map(MedicationRequest.class::cast)
            .filter(medicationRequest -> MedicationRequestIntent.ORDER.equals(medicationRequest.getIntent()))
            .toList();

        assertAll(
            () -> assertThat(planMedicationRequests).as("Plans").hasSize(1),
            () -> assertThat(orderMedicationRequests).as("Orders").hasSize(2)
        );
    }

    @Test
    void When_DuplicatingAcutePlan_Expect_TheEarliestOrderReferencesTheOriginalPlan() {
        var ehrExtract = unmarshallEhrExtract(
            "ehrExtract_MultipleSupplyPrescribeInFulfilmentOfSingleAcuteSupplyAuthorise.xml"
        );

        final var initialPlanId = "000EEA41-289B-4B1C-A5AB-421A666A0D2C";

        when(
            medicationRequestPlanMapper.mapToPlanMedicationRequest(
                eq(ehrExtract),
                any(RCMRMT030101UKEhrComposition.class),
                any(RCMRMT030101UKMedicationStatement.class),
                any(RCMRMT030101UKAuthorise.class),
                any(String.class)
            )
        ).thenReturn(
            (MedicationRequest) new MedicationRequest()
                .setIntent(MedicationRequestIntent.PLAN)
                .setExtension(List.of(ACUTE_PRESCRIPTION_EXTENSION))
                .setId(initialPlanId)
        );

        when(
            medicationRequestOrderMapper.mapToOrderMedicationRequest(
                eq(ehrExtract),
                any(RCMRMT030101UKEhrComposition.class),
                any(RCMRMT030101UKMedicationStatement.class),
                any(RCMRMT030101UKPrescribe.class),
                any(String.class)
            )
        ).thenReturn(
            new MedicationRequest()
                .setIntent(MedicationRequestIntent.ORDER)
                .addBasedOn(REFERENCE_TO_PLAN)
                .setDispenseRequest(
                    new MedicationRequestDispenseRequestComponent()
                        .setValidityPeriod(
                            new Period().setStartElement(DateFormatUtil.parseToDateTimeType("20240101"))
                        )
                ),
            new MedicationRequest()
                .setIntent(MedicationRequestIntent.ORDER)
                .addBasedOn(REFERENCE_TO_PLAN)
                .setDispenseRequest(
                    new MedicationRequestDispenseRequestComponent()
                        .setValidityPeriod(
                            new Period().setStartElement(DateFormatUtil.parseToDateTimeType("20240202"))
                        )
                )
        );

        var resources = medicationRequestMapper
            .mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(), PRACTISE_CODE
            );

        var firstOrderMedicationRequest = resources.stream()
            .filter(MedicationRequest.class::isInstance)
            .map(MedicationRequest.class::cast)
            .filter(medicationRequest -> MedicationRequestIntent.ORDER.equals(medicationRequest.getIntent()))
            .findFirst()
            .orElseThrow();

        assertAll(
            () -> assertThat(firstOrderMedicationRequest.getBasedOn().getFirst().getReference())
                .isEqualTo("MedicationRequest/" + initialPlanId)
        );
    }

    @Test
    public void When_MedicationRequestMapperThrowsException_Expect_MedicationMapperContextToBeReset() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract1.xml");

        when(medicationRequestOrderMapper.mapToOrderMedicationRequest(any(), any(), any(), any(), any()))
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

    private void assertMedicationRequests(List<? extends DomainResource> resources) {
        resources.stream()
            .filter(resource -> ResourceType.MedicationRequest.equals(resource.getResourceType()))
            .map(MedicationRequest.class::cast)
            .map(MedicationRequest::getSubject)
            .forEach(this::assertReferenceIdIsPatientId);
    }

    private void assertMedicationStatements(List<? extends DomainResource> resources) {
        resources.stream()
            .filter(resource -> ResourceType.MedicationStatement.equals(resource.getResourceType()))
            .map(MedicationStatement.class::cast)
            .forEach(this::assertMedicationStatementDateAndIdIsPatient);
    }

    private void assertMedicationStatementDateAndIdIsPatient(MedicationStatement statement) {
        final Date date = statement.getDateAssertedElement().getValue();
        final Reference reference = statement.getSubject();

        assertReferenceIdIsPatientId(reference);
        assertThat(date).isEqualTo(EXPECTED_DATE_TIME_TYPE.getValue());
    }

    private void assertReferenceIdIsPatientId(Reference reference) {
        final String medicationRequestId = reference
            .getResource()
            .getIdElement()
            .getIdPart();

        assertThat(medicationRequestId).isEqualTo(PATIENT_ID);
    }

    private void setupCommonStubs() {
        Mockito.lenient().when(medicationRequestPlanMapper.mapToPlanMedicationRequest(
            any(RCMRMT030101UKEhrExtract.class),
            any(RCMRMT030101UKEhrComposition.class),
            any(RCMRMT030101UKMedicationStatement.class),
            any(RCMRMT030101UKAuthorise.class),
            any(String.class)
        )).thenReturn(new MedicationRequest());

        Mockito.lenient().when(medicationRequestOrderMapper.mapToOrderMedicationRequest(
            any(RCMRMT030101UKEhrExtract.class),
            any(RCMRMT030101UKEhrComposition.class),
            any(RCMRMT030101UKMedicationStatement.class),
            any(RCMRMT030101UKPrescribe.class),
            any(String.class)
        )).thenReturn(new MedicationRequest());

        Mockito.lenient().when(medicationStatementMapper.mapToMedicationStatement(
            any(RCMRMT030101UKEhrExtract.class),
            any(RCMRMT030101UKEhrComposition.class),
            any(RCMRMT030101UKMedicationStatement.class),
            any(RCMRMT030101UKAuthorise.class),
            any(String.class),
            any(DateTimeType.class)
        )).thenReturn(new MedicationStatement());

        Mockito.lenient().when(medicationMapper.createMedication(
            any(RCMRMT030101UKConsumable.class)
        )).thenReturn(new Medication());
    }
}