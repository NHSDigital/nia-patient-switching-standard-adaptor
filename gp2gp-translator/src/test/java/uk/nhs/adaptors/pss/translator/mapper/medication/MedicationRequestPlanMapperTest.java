package uk.nhs.adaptors.pss.translator.mapper.medication;

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
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component2;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
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
    private static final String MEDICATION_ID = "MEDICATION_ID";
    private static final String TEST_ID = "TEST_ID";
    private static final String TAKE_ONE_DAILY = "One To Be Taken Each Day";
    private static final String AVAILABILITY_TIME = "20060426";

    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int SIX = 6;
    private static final int TWENTY_EIGHT = 28;

    @Mock
    private MedicationMapper medicationMapper;

    @InjectMocks
    private MedicationRequestPlanMapper medicationRequestPlanMapper;

    @Test
    public void When_MappingAuthoriseResourceWithAllOptionals_Expect_AllFieldsToBeMappedCorrectly() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract2.xml");
        var medicationStatement = extractMedicationStatement(ehrExtract);
        assertThat(medicationStatement.isPresent()).isTrue();
        var supplyAuthorise = medicationStatement.get()
            .getComponent()
            .stream()
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .findFirst();
        assertThat(supplyAuthorise.isPresent()).isTrue();

        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));

        var medicationRequest
            = medicationRequestPlanMapper.mapToPlanMedicationRequest(ehrExtract, medicationStatement.get(), supplyAuthorise.get());

        var repeatInformation = medicationRequest
            .getExtensionsByUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationRepeatInformation-1");
        assertThat(repeatInformation.size()).isEqualTo(ONE);
        assertRepeatInformation(repeatInformation.get(0));

        var statusReason = medicationRequest
            .getExtensionsByUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationStatusReason-1");
        assertThat(statusReason.size()).isEqualTo(ONE);
        assertStatusReasonInformation(statusReason.get(0));

        var prescriptionType = medicationRequest
            .getExtensionsByUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1");
        assertThat(prescriptionType.size()).isEqualTo(ONE);
        var codeableConcept = (CodeableConcept) prescriptionType.get(0).getValue();
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("Repeat");

        assertThat(medicationRequest.getStatus()).isEqualTo(ACTIVE);
        assertThat(medicationRequest.getIntent()).isEqualTo(PLAN);
        assertThat(medicationRequest.getMedicationReference().getReferenceElement().getIdPart()).isEqualTo(MEDICATION_ID);
        assertThat(medicationRequest.getNote().size()).isEqualTo(TWO);

        assertThat(medicationRequest.getDosageInstructionFirstRep().getText()).isEqualTo(TAKE_ONE_DAILY);
        assertThat(medicationRequest.getDispenseRequest().getQuantity().getValue().intValue()).isEqualTo(TWENTY_EIGHT);
        assertThat(medicationRequest.getDispenseRequest().getValidityPeriod().hasStart()).isTrue();
        assertThat(medicationRequest.getDispenseRequest().getValidityPeriod().hasEnd()).isTrue();
        assertThat(medicationRequest.getPriorPrescription().getReferenceElement().getIdPart()).isEqualTo(TEST_ID);
    }

    private void assertStatusReasonInformation(Extension extension) {
        var changeDate = extension.getExtensionsByUrl("statusChangeDate").get(0);
        var changeDateValue = (DateTimeType) changeDate.getValue();
        assertThat(changeDateValue.getValue()).isEqualTo(DateFormatUtil.parseToDateTimeType(AVAILABILITY_TIME).getValue());

        var statusReason = extension.getExtensionsByUrl("statusReason").get(0);
        assertThat(statusReason.hasValue()).isTrue();
    }

    private void assertRepeatInformation(Extension extension) {
        var repeatsAllowed = extension.getExtensionsByUrl("numberOfRepeatPrescriptionsAllowed");
        assertThat(repeatsAllowed.size()).isEqualTo(ONE);
        assertThat(((UnsignedIntType) repeatsAllowed.get(0).getValue()).getValue()).isEqualTo(new UnsignedIntType(SIX).getValue());

        var repeatsIssued = extension.getExtensionsByUrl("numberOfRepeatPrescriptionsIssued");
        assertThat(repeatsIssued.size()).isEqualTo(ONE);
        assertThat(((UnsignedIntType) repeatsIssued.get(0).getValue()).getValue()).isEqualTo(new UnsignedIntType(ONE).getValue());
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
