package uk.nhs.adaptors.pss.translator.mapper.medication;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestIntent.ORDER;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus.COMPLETED;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UKComponent2;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKMedicationStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@ExtendWith(MockitoExtension.class)
public class MedicationRequestOrderMapperTest {

    private static final String XML_RESOURCES_MEDICATION_STATEMENT = "xml/MedicationStatement/";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String TEST_ID = "TEST_ID";
    private static final String MEDICATION_ID = "MEDICATION_ID";
    private static final String TAKE_ONE_DAILY = "TAKE ONE DAILY";
    private static final String AVAILABILITY_TIME = "20060426";

    private static final int THREE = 3;
    private static final int SEVEN = 7;

    @Mock
    private MedicationMapper medicationMapper;

    @InjectMocks
    private MedicationRequestOrderMapper medicationRequestOrderMapper;

    @Test
    public void When_MappingPrescribeResourceWithAllOptionals_Expect_AllFieldsToBeMappedCorrectly() {
        var medicationStatement = unmarshallMedicationStatement("medicationStatementPrescribeAllOptionals.xml");
        var prescribe = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyPrescribe)
            .map(RCMRMT030101UKComponent2::getEhrSupplyPrescribe)
            .findFirst();

        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));

        assertThat(prescribe).isPresent();
        var medicationRequest = medicationRequestOrderMapper.mapToOrderMedicationRequest(new RCMRMT030101UKEhrExtract(),
            medicationStatement, prescribe.get(), PRACTISE_CODE);
        assertCommonValues(medicationRequest);
        medicationRequest
            .getExtensionsByUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1")
            .forEach(extension -> assertPrescriptionType(extension, "Repeat"));
        assertThat(medicationRequest.getBasedOnFirstRep().getReferenceElement().getIdPart()).isEqualTo(TEST_ID);
        assertThat(medicationRequest.getNote()).hasSize(THREE);
        assertThat(medicationRequest.getDosageInstructionFirstRep().getText()).isEqualTo(TAKE_ONE_DAILY);
        assertThat(medicationRequest.getDispenseRequest().getQuantity().getValue().intValue()).isEqualTo(SEVEN);
        assertThat(medicationRequest.getDispenseRequest().getValidityPeriod().getStartElement().getValue())
            .isEqualTo(DateFormatUtil.parseToDateTimeType(AVAILABILITY_TIME).getValue());
    }

    @Test
    public void When_MappingPrescribeResourceWithNoOptionals_Expect_AllFieldsToBeMappedCorrectly() {
        var medicationStatement = unmarshallMedicationStatement("medicationStatementPrescribeNoOptionals.xml");
        var prescribe = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyPrescribe)
            .map(RCMRMT030101UKComponent2::getEhrSupplyPrescribe)
            .findFirst();

        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));

        assertThat(prescribe).isPresent();
        var medicationRequest = medicationRequestOrderMapper.mapToOrderMedicationRequest(new RCMRMT030101UKEhrExtract(),
            medicationStatement, prescribe.get(), PRACTISE_CODE);
        assertCommonValues(medicationRequest);

        medicationRequest
            .getExtensionsByUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1")
            .forEach(extension -> assertPrescriptionType(extension, "Repeat"));
        assertThat(medicationRequest.getBasedOnFirstRep().getReferenceElement().getIdPart()).isEqualTo(TEST_ID);
        assertThat(medicationRequest.getNote()).hasSize(1);
        assertThat(medicationRequest.getDosageInstructionFirstRep().getText()).isEqualTo(TAKE_ONE_DAILY);
        assertThat(medicationRequest.getDispenseRequest().getQuantity().getValue()).isNull();
        assertThat(medicationRequest.getDispenseRequest().getValidityPeriod().getStartElement().getValue())
            .isEqualTo(DateFormatUtil.parseToDateTimeType(AVAILABILITY_TIME).getValue());
    }

    public void assertCommonValues(MedicationRequest medicationRequest) {
        assertThat(medicationRequest.getIdentifier()).hasSize(1);
        assertThat(medicationRequest.getIntent()).isEqualTo(ORDER);
        assertThat(medicationRequest.getStatus()).isEqualTo(COMPLETED);
        assertThat(medicationRequest.getMedicationReference().getReferenceElement().getIdPart()).isEqualTo(MEDICATION_ID);
    }

    public void assertPrescriptionType(Extension extension, String expectedDisplayValue) {
        var codeableConcept = (CodeableConcept) extension.getValue();
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo(expectedDisplayValue);
    }

    @SneakyThrows
    private RCMRMT030101UKMedicationStatement unmarshallMedicationStatement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_MEDICATION_STATEMENT + fileName),
            RCMRMT030101UKMedicationStatement.class);
    }
}
