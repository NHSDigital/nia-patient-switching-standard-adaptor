package uk.nhs.adaptors.pss.translator.mapper.medication;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
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
import org.jetbrains.annotations.NotNull;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.Date;
import java.util.List;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@ExtendWith(MockitoExtension.class)
public class MedicationRequestMapperTest {

    public static final String INITIAL_PLAN_ID = "000EEA41-289B-4B1C-A5AB-421A666A0D2C";
    public static final String GENERATED_PLAN_ID = "00000000-0000-4000-0000-000000000001";
    public static final String EARLIEST_ORDER_ID = "00000000-0000-4000-0000-100000000001";
    public static final String LATEST_ORDER_ID = "00000000-0000-4000-0000-100000000002";

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
            INITIAL_PLAN_ID
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
    @Mock
    private IdGeneratorService idGeneratorService;
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
    void When_MultipleOrdersAreBasedOnOneRepeatPlan_Expect_NoAdditionalPlanCreated() {
        setupMultipleOrdersToOnePlanStubs(REPEAT_PRESCRIPTION_EXTENSION);
        var ehrExtract = unmarshallEhrExtract(
            "ehrExtract_MultipleSupplyPrescribeInFulfilmentOfSingleNonAcuteSupplyAuthorise.xml"
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
    void When_MultipleOrdersAreBasedOnOneAcutePlan_Expect_AdditionalPlanCreated() {
        setupMultipleOrdersToOnePlanStubs(ACUTE_PRESCRIPTION_EXTENSION);
        var ehrExtract = unmarshallEhrExtract(
            "ehrExtract_MultipleSupplyPrescribeInFulfilmentOfSingleAcuteSupplyAuthorise.xml"
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
            () -> assertThat(planMedicationRequests).as("Plans").hasSize(2),
            () -> assertThat(orderMedicationRequests).as("Orders").hasSize(2)
        );
    }

    @Test
    void When_MultipleOrdersAreBasedOnOneAcutePlan_Expect_TheGeneratedPlanHasIdAndIdentityUpdatedToGeneratedId() {
        setupMultipleOrdersToOnePlanStubs(ACUTE_PRESCRIPTION_EXTENSION);
        var ehrExtract = unmarshallEhrExtract(
            "ehrExtract_MultipleSupplyPrescribeInFulfilmentOfSingleAcuteSupplyAuthorise.xml"
        );

        var resources = medicationRequestMapper
            .mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(), PRACTISE_CODE
            );

        var generatedPlan = getMedicationRequestById(resources, GENERATED_PLAN_ID);

        assertAll(
            () -> assertThat(generatedPlan.getId())
                .isEqualTo(GENERATED_PLAN_ID),
            () -> assertThat(generatedPlan.getIdentifierFirstRep().getValue())
                .isEqualTo(GENERATED_PLAN_ID)
        );
    }

    @Test
    void When_MultipleOrdersAreBasedOnOneAcutePlan_Expect_TheEarliestOrderReferencesTheOriginalPlan() {
        setupMultipleOrdersToOnePlanStubs(ACUTE_PRESCRIPTION_EXTENSION);
        var ehrExtract = unmarshallEhrExtract(
            "ehrExtract_MultipleSupplyPrescribeInFulfilmentOfSingleAcuteSupplyAuthorise.xml"
        );

        var resources = medicationRequestMapper
            .mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(), PRACTISE_CODE
            );

        var earliestOrder = getMedicationRequestById(resources, EARLIEST_ORDER_ID);

        assertAll(
            () -> assertThat(earliestOrder.getBasedOn().getFirst().getReferenceElement().getIdPart())
                .isEqualTo(INITIAL_PLAN_ID),
            () -> assertThat(earliestOrder.getBasedOn().getFirst().getReferenceElement().getResourceType())
                .isEqualTo(ResourceType.MedicationRequest.name())
        );
    }

    @Test
    void When_MultipleOrdersAreBasedOnOneAcutePlan_Expect_TheLatestOrderReferencesTheGeneratedPlan() {
        setupMultipleOrdersToOnePlanStubs(ACUTE_PRESCRIPTION_EXTENSION);
        var ehrExtract = unmarshallEhrExtract(
            "ehrExtract_MultipleSupplyPrescribeInFulfilmentOfSingleAcuteSupplyAuthorise.xml"
        );

        var resources = medicationRequestMapper
            .mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(), PRACTISE_CODE);

        var latestOrder = getMedicationRequestById(resources, LATEST_ORDER_ID);
        var generatedPlan = getMedicationRequestById(resources, GENERATED_PLAN_ID);

        assertAll(
            () -> assertThat(latestOrder.getBasedOn().getFirst().getReferenceElement().getIdPart())
                .isEqualTo(generatedPlan.getId()),
            () -> assertThat(latestOrder.getBasedOn().getFirst().getReferenceElement().getResourceType())
                .isEqualTo(ResourceType.MedicationRequest.name())
        );
    }

    @Test
    void When_MultipleOrdersAreBasedOnOneAcutePlan_Expect_TheLatestOrderPriorPrescriptionReferencesTheOriginalPlan() {
        setupMultipleOrdersToOnePlanStubs(ACUTE_PRESCRIPTION_EXTENSION);
        var ehrExtract = unmarshallEhrExtract(
            "ehrExtract_MultipleSupplyPrescribeInFulfilmentOfSingleAcuteSupplyAuthorise.xml"
        );

        var resources = medicationRequestMapper
            .mapResources(ehrExtract, (Patient) new Patient().setId(PATIENT_ID), List.of(), PRACTISE_CODE);

        var generatedPlanReferenceElement = getMedicationRequestById(resources, GENERATED_PLAN_ID)
            .getPriorPrescription()
            .getReferenceElement();

        assertAll(
            () -> assertThat(generatedPlanReferenceElement.getResourceType())
                .isEqualTo(ResourceType.MedicationRequest.name()),
            () -> assertThat(generatedPlanReferenceElement.getIdPart())
                .isEqualTo(INITIAL_PLAN_ID)
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

    private MedicationRequest buildMedicationRequestOrder(String id, String validityPeriodStartDate) {
        return (MedicationRequest) new MedicationRequest()
            .setIntent(MedicationRequestIntent.ORDER)
            .addBasedOn(REFERENCE_TO_PLAN)
            .setDispenseRequest(
                new MedicationRequestDispenseRequestComponent()
                    .setValidityPeriod(
                        new Period().setStartElement(DateFormatUtil.parseToDateTimeType(validityPeriodStartDate))
                    )
            )
            .setId(id);
    }

    private MedicationRequest buildMedicationRequestPlan(Extension extension) {
        return (MedicationRequest) new MedicationRequest()
            .setIntent(MedicationRequestIntent.PLAN)
            .addIdentifier(
                new Identifier().setValue(INITIAL_PLAN_ID)
            )
            .setExtension(List.of(extension))
            .setId(INITIAL_PLAN_ID);
    }

    private static @NotNull MedicationRequest getMedicationRequestById(
        List<DomainResource> resources,
        String id
    ) {
        return resources.stream()
            .filter(MedicationRequest.class::isInstance)
            .map(MedicationRequest.class::cast)
            .filter(medicationRequest -> id.equals(medicationRequest.getId()))
            .findFirst()
            .orElseThrow();
    }

    private void setupMultipleOrdersToOnePlanStubs(Extension planExtension) {
        when(
            medicationRequestPlanMapper.mapToPlanMedicationRequest(
                any(RCMRMT030101UKEhrExtract.class),
                any(RCMRMT030101UKEhrComposition.class),
                any(RCMRMT030101UKMedicationStatement.class),
                any(RCMRMT030101UKAuthorise.class),
                any(String.class)
            )
        ).thenReturn(buildMedicationRequestPlan(planExtension));

        when(
            medicationRequestOrderMapper.mapToOrderMedicationRequest(
                any(RCMRMT030101UKEhrExtract.class),
                any(RCMRMT030101UKEhrComposition.class),
                any(RCMRMT030101UKMedicationStatement.class),
                any(RCMRMT030101UKPrescribe.class),
                any(String.class)
            )
        ).thenReturn(buildMedicationRequestOrder(
            LATEST_ORDER_ID, "20240102"),
            buildMedicationRequestOrder(EARLIEST_ORDER_ID, "20240101")
        );

        lenient().when(idGeneratorService.generateUuid()).thenReturn(GENERATED_PLAN_ID);
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