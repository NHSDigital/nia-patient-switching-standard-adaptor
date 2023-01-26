package uk.nhs.adaptors.pss.translator.mapper.medication;

import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus.COMPLETED;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus.STOPPED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;
import static org.assertj.core.api.Assertions.assertThat;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestIntent.PLAN;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.UnsignedIntType;
import org.hl7.v3.RCMRMT030101UK04Authorise;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component2;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@ExtendWith(MockitoExtension.class)
public class MedicationRequestPlanMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/MedicationStatement/";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String MEDICATION_ID = "MEDICATION_ID";
    private static final String TEST_ID = "TEST_ID";
    private static final String TAKE_ONE_DAILY = "One To Be Taken Each Day";
    private static final String AVAILABILITY_TIME = "20060426";
    private static final String REPEATS_ISSUED_URL = "numberOfRepeatPrescriptionsIssued";
    private static final String REPEATS_ALLOWED_URL = "numberOfRepeatPrescriptionsAllowed";
    private static final String REPEATS_EXPIRY_DATE_URL = "authorisationExpiryDate";
    private static final String REPEAT_INFO_URL =
        "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationRepeatInformation-1";
    private static final String MEDICATION_STATUS_REASON_URL =
        "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationStatusReason-1";
    private static final String PRESCRIPTION_TYPE_URL =
        "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1";
    private static final String DEFAULT_STATUS_REASON = "No information available";

    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int SIX = 6;
    private static final int TWENTY_EIGHT = 28;

    @Mock
    private MedicationMapper medicationMapper;

    @InjectMocks
    private MedicationRequestPlanMapper medicationRequestPlanMapper;

    @BeforeEach
    public void setup() {
        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));
    }

    @Test
    public void When_MappingAuthoriseResourceWithAllOptionals_Expect_AllFieldsToBeMappedCorrectly() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract2.xml");
        Optional<RCMRMT030101UK04MedicationStatement> medicationStatement = extractMedicationStatement(ehrExtract);
        assertThat(medicationStatement.isPresent()).isTrue();

        Optional<RCMRMT030101UK04Authorise> supplyAuthorise = extractSupplyAuthorise(medicationStatement.get());
        assertThat(supplyAuthorise.isPresent()).isTrue();

        var medicationRequest =
            medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement.get(), supplyAuthorise.get(),
                PRACTISE_CODE);

        var repeatInformation = medicationRequest.getExtensionsByUrl(REPEAT_INFO_URL);
        assertThat(repeatInformation.size()).isEqualTo(ONE);
        assertRepeatInformation(repeatInformation.get(0));

        var statusReason = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        assertThat(statusReason.size()).isEqualTo(ONE);
        assertStatusReasonInformation(statusReason.get(0));

        var prescriptionType = medicationRequest.getExtensionsByUrl(PRESCRIPTION_TYPE_URL);
        assertThat(prescriptionType.size()).isEqualTo(ONE);

        var codeableConcept = (CodeableConcept) prescriptionType.get(0).getValue();
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("Repeat");

        assertThat(medicationRequest.getStatus()).isEqualTo(STOPPED);
        assertThat(medicationRequest.getIntent()).isEqualTo(PLAN);
        assertThat(medicationRequest.getMedicationReference().getReferenceElement().getIdPart()).isEqualTo(MEDICATION_ID);
        assertThat(medicationRequest.getNote().size()).isEqualTo(TWO);

        assertThat(medicationRequest.getDosageInstructionFirstRep().getText()).isEqualTo(TAKE_ONE_DAILY);
        assertThat(medicationRequest.getDispenseRequest().getQuantity().getValue().intValue()).isEqualTo(TWENTY_EIGHT);
        assertThat(medicationRequest.getDispenseRequest().getValidityPeriod().hasStart()).isTrue();
        assertThat(medicationRequest.getDispenseRequest().getValidityPeriod().hasEnd()).isTrue();
        assertThat(medicationRequest.getPriorPrescription().getReferenceElement().getIdPart()).isEqualTo(TEST_ID);
    }

    @Test
    public void When_MappingAuthoriseResourceWithNoEffectiveTime_Expect_NoExpiryDateExtensionAdded() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract5.xml");
        Optional<RCMRMT030101UK04MedicationStatement> medicationStatement = extractMedicationStatement(ehrExtract);
        assertThat(medicationStatement.isPresent()).isTrue();

        Optional<RCMRMT030101UK04Authorise> supplyAuthorise = extractSupplyAuthorise(medicationStatement.get());
        assertThat(supplyAuthorise.isPresent()).isTrue();

        var medicationRequest
            = medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement.get(), supplyAuthorise.get(),
            PRACTISE_CODE);

        var repeatInformation = medicationRequest.getExtensionsByUrl(REPEAT_INFO_URL);
        assertThat(repeatInformation.size()).isEqualTo(ONE);

        var expiryDate = repeatInformation.get(0).getExtensionsByUrl(REPEATS_EXPIRY_DATE_URL);
        assertThat(expiryDate.size()).isEqualTo(0);
    }

    @Test
    public void When_MappingAuthoriseResourceEffectiveTimeWithNullHighValue_Expect_NoExpiryDateExtensionAdded() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract6.xml");
        Optional<RCMRMT030101UK04MedicationStatement> medicationStatement = extractMedicationStatement(ehrExtract);
        assertThat(medicationStatement.isPresent()).isTrue();

        Optional<RCMRMT030101UK04Authorise> supplyAuthorise = extractSupplyAuthorise(medicationStatement.get());
        assertThat(supplyAuthorise.isPresent()).isTrue();

        var medicationRequest =
            medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement.get(), supplyAuthorise.get(),
                PRACTISE_CODE);

        var repeatInformation = medicationRequest.getExtensionsByUrl(REPEAT_INFO_URL);
        assertThat(repeatInformation.size()).isEqualTo(ONE);

        var expiryDate = repeatInformation.get(0).getExtensionsByUrl(REPEATS_EXPIRY_DATE_URL);
        assertThat(expiryDate.size()).isEqualTo(0);
    }

    @Test
    public void When_MappingDiscontinue_With_PertinentInformation_Expect_StatusReasonAdded() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract2.xml");
        Optional<RCMRMT030101UK04MedicationStatement> medicationStatement = extractMedicationStatement(ehrExtract);
        Optional<RCMRMT030101UK04Authorise> supplyAuthorise = extractSupplyAuthorise(medicationStatement.orElseThrow());

        var medicationRequest =
            medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement.get(), supplyAuthorise.orElseThrow(),
                PRACTISE_CODE);

        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        assertThat(statusExt.size()).isEqualTo(1);

        var statusReasonExt = statusExt.get(0).getExtensionsByUrl("statusReason");
        assertThat(statusReasonExt.size()).isEqualTo(1);

        var statusReason = (CodeableConcept) statusReasonExt.get(0).getValue();

        assertThat(statusReason.getText()).isEqualTo("Patient no longer requires these");
    }

    @Test
    public void When_MappingDiscontinue_With_MissingPertinentInformationAndCodeDisplayPresent_Expect_DefaultTextAddedAsReason() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract7.xml");
        Optional<RCMRMT030101UK04MedicationStatement> medicationStatement = extractMedicationStatement(ehrExtract);
        Optional<RCMRMT030101UK04Authorise> supplyAuthorise = extractSupplyAuthorise(medicationStatement.orElseThrow());

        var medicationRequest =
            medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement.get(), supplyAuthorise.orElseThrow(),
                PRACTISE_CODE);

        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        assertThat(statusExt.size()).isEqualTo(1);

        var statusReasonExt = statusExt.get(0).getExtensionsByUrl("statusReason");
        assertThat(statusReasonExt.size()).isEqualTo(1);

        var statusReason = (CodeableConcept) statusReasonExt.get(0).getValue();
        assertThat(statusReason.getText()).isEqualTo(DEFAULT_STATUS_REASON);
    }

    @Test
    public void When_MappingDiscontinue_With_MissingPertinentInformationAndCodeDisplay_Expect_DefaultTextAddedAsReason() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract8.xml");
        Optional<RCMRMT030101UK04MedicationStatement> medicationStatement = extractMedicationStatement(ehrExtract);
        Optional<RCMRMT030101UK04Authorise> supplyAuthorise = extractSupplyAuthorise(medicationStatement.orElseThrow());

        var medicationRequest =
            medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement.get(), supplyAuthorise.orElseThrow(),
                PRACTISE_CODE);

        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        assertThat(statusExt.size()).isEqualTo(1);

        var statusReasonExt = statusExt.get(0).getExtensionsByUrl("statusReason");
        assertThat(statusReasonExt.size()).isEqualTo(1);

        var statusReason = (CodeableConcept) statusReasonExt.get(0).getValue();
        assertThat(statusReason.getText()).isEqualTo(DEFAULT_STATUS_REASON);
    }

    @Test
    public void When_MappingDiscontinue_With_MissingPertinentInformation_Expect_DefaultTextAddedAsReason() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract9.xml");
        Optional<RCMRMT030101UK04MedicationStatement> medicationStatement = extractMedicationStatement(ehrExtract);
        Optional<RCMRMT030101UK04Authorise> supplyAuthorise = extractSupplyAuthorise(medicationStatement.orElseThrow());

        var medicationRequest =
            medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement.get(), supplyAuthorise.orElseThrow(),
                PRACTISE_CODE);

        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        assertThat(statusExt.size()).isEqualTo(1);

        var statusReasonExt = statusExt.get(0).getExtensionsByUrl("statusReason");
        assertThat(statusReasonExt.size()).isEqualTo(1);

        var statusReason = (CodeableConcept) statusReasonExt.get(0).getValue();
        assertThat(statusReason.getText()).isEqualTo(DEFAULT_STATUS_REASON);
    }

    @Test
    public void When_MappingAuthoriseResource_WithActiveStatusAndNoDiscontinue_Expect_ActiveStatusAndNoStatus() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract10.xml");
        Optional<RCMRMT030101UK04MedicationStatement> medicationStatement = extractMedicationStatement(ehrExtract);
        Optional<RCMRMT030101UK04Authorise> supplyAuthorise = extractSupplyAuthorise(medicationStatement.orElseThrow());

        var medicationRequest =
            medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement.get(), supplyAuthorise.orElseThrow(),
                PRACTISE_CODE);

        assertThat(medicationRequest.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    public void When_MappingAuthoriseResource_WithCompleteStatusAndNoDiscontinue_Expect_CompletedStatus() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract11.xml");
        Optional<RCMRMT030101UK04MedicationStatement> medicationStatement = extractMedicationStatement(ehrExtract);
        Optional<RCMRMT030101UK04Authorise> supplyAuthorise = extractSupplyAuthorise(medicationStatement.orElseThrow());

        var medicationRequest =
            medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement.get(), supplyAuthorise.orElseThrow(),
                PRACTISE_CODE);

        assertThat(medicationRequest.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void When_MappingAuthoriseResource_With_NoDiscontinue_Expect_NoStatusReasonExtension() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract11.xml");
        Optional<RCMRMT030101UK04MedicationStatement> medicationStatement = extractMedicationStatement(ehrExtract);
        Optional<RCMRMT030101UK04Authorise> supplyAuthorise = extractSupplyAuthorise(medicationStatement.orElseThrow());

        var medicationRequest =
            medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement.get(), supplyAuthorise.orElseThrow(),
                PRACTISE_CODE);

        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        assertThat(statusExt.isEmpty()).isTrue();
    }

    @Test
    public void When_MappingDiscontinue_With_UnknownDate_Expect_DiscontinueIgnored() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract12.xml");
        Optional<RCMRMT030101UK04MedicationStatement> medicationStatement = extractMedicationStatement(ehrExtract);
        Optional<RCMRMT030101UK04Authorise> supplyAuthorise = extractSupplyAuthorise(medicationStatement.orElseThrow());

        var medicationRequest =
            medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement.get(), supplyAuthorise.orElseThrow(),
                PRACTISE_CODE);

        assertThat(medicationRequest.getStatus()).isEqualTo(COMPLETED);
        var statusExt = medicationRequest.getExtensionsByUrl(MEDICATION_STATUS_REASON_URL);
        assertThat(statusExt.isEmpty()).isTrue();
    }

    private Optional<RCMRMT030101UK04Authorise> extractSupplyAuthorise(RCMRMT030101UK04MedicationStatement medicationStatement) {
        return medicationStatement
            .getComponent()
            .stream()
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .findFirst();
    }

    private void assertStatusReasonInformation(Extension extension) {
        var changeDate = extension.getExtensionsByUrl("statusChangeDate").get(0);
        var changeDateValue = (DateTimeType) changeDate.getValue();
        assertThat(changeDateValue.getValue()).isEqualTo(DateFormatUtil.parseToDateTimeType(AVAILABILITY_TIME).getValue());

        var statusReason = extension.getExtensionsByUrl("statusReason").get(0);
        assertThat(statusReason.hasValue()).isTrue();
    }

    private void assertRepeatInformation(Extension extension) {
        var repeatsAllowed = extension.getExtensionsByUrl(REPEATS_ALLOWED_URL);
        assertThat(repeatsAllowed.size()).isEqualTo(ONE);
        assertThat(((UnsignedIntType) repeatsAllowed.get(0).getValue()).getValue()).isEqualTo(new UnsignedIntType(SIX).getValue());

        var repeatsIssued = extension.getExtensionsByUrl(REPEATS_ISSUED_URL);
        assertThat(repeatsIssued.size()).isEqualTo(ONE);
        assertThat(((UnsignedIntType) repeatsIssued.get(0).getValue()).getValue()).isEqualTo(new UnsignedIntType(ONE).getValue());

        var expiryDate = extension.getExtensionsByUrl(REPEATS_EXPIRY_DATE_URL);
        assertThat(expiryDate.size()).isEqualTo(ONE);
        var date = expiryDate.get(0).getValue().toString();
        assertThat(date).isEqualTo(DateFormatUtil.parseToDateTimeType("20060427").toString());
    }

    private Optional<RCMRMT030101UK04MedicationStatement> extractMedicationStatement(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract
            .getComponent()
            .stream()
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .map(RCMRMT030101UK04EhrComposition::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UK04Component4::getMedicationStatement)
            .findFirst();
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
