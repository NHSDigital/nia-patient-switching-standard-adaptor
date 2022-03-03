package uk.nhs.adaptors.pss.translator.mapper.medication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;
import static org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementTaken.UNK;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UK04Component2;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@ExtendWith(MockitoExtension.class)
public class MedicationStatementMapperTest {

    private static final String XML_RESOURCES_MEDICATION_STATEMENT = "xml/MedicationStatement/";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String TEST_ID = "TEST_ID";
    private static final String MEDICATION_ID = "MEDICATION_ID";
    private static final String TAKE_ONE_DAILY = "TAKE ONE DAILY";

    @Mock
    private MedicationMapper medicationMapper;
    @InjectMocks
    private MedicationStatementMapper medicationStatementMapper;

    @Test
    public void When_MappingPrescribeResourceWithNoOptionals_Expect_AllFieldsToBeMappedCorrectly() {
        var medicationStatement = unmarshallMedicationStatement("medicationStatementAuthoriseAllOptionals_MedicationStatement.xml");
        var authorise = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .findFirst();

        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));

        assertThat(authorise.isPresent()).isTrue();
        var medicationStatement1 = medicationStatementMapper.mapToMedicationStatement(medicationStatement, authorise.get(), PRACTISE_CODE);

        var lastIssuedDate = medicationStatement1.getExtensionsByUrl(
            "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationStatementLastIssueDate-1");
        assertThat(lastIssuedDate.size()).isEqualTo(1);
        var dateTime = (DateTimeType) lastIssuedDate.get(0).getValue();
        assertThat(dateTime.getValue()).isEqualTo(DateFormatUtil.parseToDateTimeType("20060428").getValue());

        var prescribingAgency = medicationStatement1
            .getExtensionsByUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescribingAgency-1");
        assertThat(prescribingAgency.size()).isEqualTo(1);
        assertThat(medicationStatement1.getBasedOnFirstRep().getReferenceElement().getIdPart()).isEqualTo(TEST_ID);
        assertThat(medicationStatement1.getStatus()).isEqualTo(ACTIVE);
        assertThat(medicationStatement1.getMedicationReference().getReferenceElement().getIdPart()).isEqualTo(MEDICATION_ID);
        assertThat(medicationStatement1.getTaken()).isEqualTo(UNK);
        assertThat(medicationStatement1.getDosageFirstRep().getText()).isEqualTo(TAKE_ONE_DAILY);
    }

    @Test
    public void When_MappingPrescribeResourceWithNoLastIssueDate_Expect_AllFieldsToBeMappedCorrectly() {
        var medicationStatement = unmarshallMedicationStatement("medicationStatementAuthoriseNoOptionals_MedicationStatement.xml");
        var authorise = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .findFirst();

        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));

        assertThat(authorise.isPresent()).isTrue();
        var medicationStatement1 = medicationStatementMapper.mapToMedicationStatement(medicationStatement, authorise.get(), PRACTISE_CODE);

        var lastIssuedDate = medicationStatement1.getExtensionsByUrl(
            "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationStatementLastIssueDate-1");
        assertThat(lastIssuedDate.size()).isEqualTo(0);

        var prescribingAgency = medicationStatement1
            .getExtensionsByUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescribingAgency-1");
        assertThat(prescribingAgency.size()).isEqualTo(1);
        assertThat(medicationStatement1.getBasedOnFirstRep().getReferenceElement().getIdPart()).isEqualTo(TEST_ID);
        assertThat(medicationStatement1.getStatus()).isEqualTo(ACTIVE);
        assertThat(medicationStatement1.getMedicationReference().getReferenceElement().getIdPart()).isEqualTo(MEDICATION_ID);
        assertThat(medicationStatement1.getTaken()).isEqualTo(UNK);
        assertThat(medicationStatement1.getDosageFirstRep().getText()).isEqualTo(TAKE_ONE_DAILY);
    }

    @SneakyThrows
    private RCMRMT030101UK04MedicationStatement unmarshallMedicationStatement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_MEDICATION_STATEMENT + fileName),
            RCMRMT030101UK04MedicationStatement.class);
    }
}
