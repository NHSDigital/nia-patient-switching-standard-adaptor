package uk.nhs.adaptors.pss.translator.mapper.medication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementStatus.COMPLETED;
import static org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementStatus.STOPPED;
import static org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementTaken.UNK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UKComponent;
import org.hl7.v3.RCMRMT030101UKComponent2;
import org.hl7.v3.RCMRMT030101UKComponent3;
import org.hl7.v3.RCMRMT030101UKComponent4;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKEhrFolder;
import org.hl7.v3.RCMRMT030101UKMedicationStatement;
import org.hl7.v3.RCMRMT030101UKAuthorise;
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
        var ehrExtract = unmarshallEhrExtract("ehrExtract3.xml");
        var medicationStatement = unmarshallMedicationStatement("medicationStatementAuthoriseAllOptionals_MedicationStatement.xml");
        var authorise = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UKComponent2::getEhrSupplyAuthorise)
            .findFirst();

        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));

        assertThat(authorise.isPresent()).isTrue();
        var medicationStatement1 = medicationStatementMapper.mapToMedicationStatement(
            ehrExtract, medicationStatement, authorise.get(), PRACTISE_CODE, new DateTimeType());

        var lastIssuedDate = medicationStatement1.getExtensionsByUrl(
            "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationStatementLastIssueDate-1");
        assertThat(lastIssuedDate).hasSize(1);
        var dateTime = (DateTimeType) lastIssuedDate.get(0).getValue();
        assertThat(dateTime.getValue()).isEqualTo(DateFormatUtil.parseToDateTimeType("20060428").getValue());

        var prescribingAgency = medicationStatement1
            .getExtensionsByUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescribingAgency-1");
        assertThat(prescribingAgency).hasSize(1);
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
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UKComponent2::getEhrSupplyAuthorise)
            .findFirst();

        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));

        assertThat(authorise.isPresent()).isTrue();
        var medicationStatement1 = medicationStatementMapper.mapToMedicationStatement(
            new RCMRMT030101UKEhrExtract(), medicationStatement, authorise.get(), PRACTISE_CODE, new DateTimeType());

        var lastIssuedDate = medicationStatement1.getExtensionsByUrl(
            "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationStatementLastIssueDate-1");
        assertThat(lastIssuedDate).isEmpty();

        var prescribingAgency = medicationStatement1
            .getExtensionsByUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescribingAgency-1");
        assertThat(prescribingAgency).hasSize(1);
        assertThat(medicationStatement1.getBasedOnFirstRep().getReferenceElement().getIdPart()).isEqualTo(TEST_ID);
        assertThat(medicationStatement1.getStatus()).isEqualTo(ACTIVE);
        assertThat(medicationStatement1.getMedicationReference().getReferenceElement().getIdPart()).isEqualTo(MEDICATION_ID);
        assertThat(medicationStatement1.getTaken()).isEqualTo(UNK);
        assertThat(medicationStatement1.getDosageFirstRep().getText()).isEqualTo(TAKE_ONE_DAILY);
    }

    @Test
    public void When_MapToMedicationStatement_WithDiscontinue_WithAvailabilityTime_Expect_PeriodEndMappedAndStatusStopped() {
        var expectedStartDate = "2010-01-14";
        var expectedEndDate = "2010-04-26";

        var result =
            mapMedicationStatementFromEhrFile("ehrExtract_discontinue.xml", new DateTimeType());
        var effectivePeriod = result.getEffectivePeriod();

        assertThat(effectivePeriod.getStartElement().toHumanDisplay()).isEqualTo(expectedStartDate);
        assertThat(effectivePeriod.getEndElement().toHumanDisplay()).isEqualTo(expectedEndDate);
        assertThat(result.getStatus()).isEqualTo(STOPPED);
    }

    @Test
    public void When_MapToMedicationStatement_WithDiscontinue_WithMissingAvailabilityTime_Expect_PeriodEndMappedAndStatusCompleted() {
        var expectedStartDate = "2010-01-14";

        var result =
            mapMedicationStatementFromEhrFile("ehrExtract_discontinueMissingAvailabilityTime.xml", new DateTimeType());
        var effectivePeriod = result.getEffectivePeriod();

        assertThat(effectivePeriod.getStartElement().toHumanDisplay()).isEqualTo(expectedStartDate);
        assertThat(effectivePeriod.getEndElement().toHumanDisplay()).isEqualTo(expectedStartDate);
        assertThat(result.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void When_MapToMedicationStatement_WithCompletedStatus_WithAuthoriseEffectiveTimeHigh_Expect_PeriodEndMapped() {
        var expectedStartDate = "2010-04-27";
        var expectedEndDate = "2010-06-27";

        var result =
            mapMedicationStatementFromEhrFile("ehrExtract_authorise_effectiveTimeHigh.xml", new DateTimeType());
        var effectivePeriod = result.getEffectivePeriod();

        assertThat(effectivePeriod.getStartElement().toHumanDisplay()).isEqualTo(expectedStartDate);
        assertThat(effectivePeriod.getEndElement().toHumanDisplay()).isEqualTo(expectedEndDate);
        assertThat(result.getStatus()).isEqualTo(COMPLETED);

    }

    @Test
    public void When_MapToMedicationStatement_WithCompletedStatus_WithStatementEffectiveTimeHigh_Expect_PeriodEndMapped() {
        var expectedStartDate = "2010-01-14";
        var expectedEndDate = "2010-06-26";

        var result = mapMedicationStatementFromEhrFile("ehrExtract_effectiveTimeHigh.xml", new DateTimeType());
        var effectivePeriod = result.getEffectivePeriod();

        assertThat(effectivePeriod.getStartElement().toHumanDisplay()).isEqualTo(expectedStartDate);
        assertThat(effectivePeriod.getEndElement().toHumanDisplay()).isEqualTo(expectedEndDate);
        assertThat(result.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void When_MapToMedicationStatement_WithCompletedStatus_WithNoValidTimes_Expect_StartAndEndTimesEqualAuthoredOn() {
        var authoredOn = new DateTimeType("2023-01-27");

        var result = mapMedicationStatementFromEhrFile("ehrExtract_noValidTimes.xml", authoredOn);
        var effectivePeriod = result.getEffectivePeriod();

        assertThat(effectivePeriod.getStartElement()).isEqualTo(authoredOn);
        assertThat(effectivePeriod.getEndElement()).isEqualTo(authoredOn);
        assertThat(result.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void When_MapToMedicationStatement_WithDiscontinue_WithNoValidTimes_Expect_StartAndEndTimesEqualAuthoredOn() {
        var authoredOn = new DateTimeType("2023-01-27");

        var result =
            mapMedicationStatementFromEhrFile("ehrExtract_discontinue_noValidTimes.xml", authoredOn);
        var effectivePeriod = result.getEffectivePeriod();

        assertThat(effectivePeriod.getStartElement()).isEqualTo(authoredOn);
        assertThat(effectivePeriod.getEndElement()).isEqualTo(authoredOn);
        assertThat(result.getStatus()).isEqualTo(COMPLETED);
    }

    @Test
    public void When_MapToMedicationStatement_WithActiveStatement_Expect_StartDateIsNotMappedToEndDate() {
        var authoredOn = new DateTimeType("2023-01-27");
        var expectedStartDate = "2010-01-14";

        var result = mapMedicationStatementFromEhrFile("ehrExtract4.xml", authoredOn);
        var effectivePeriod = result.getEffectivePeriod();

        assertThat(result.getStatus()).isEqualTo(ACTIVE);
        assertThat(effectivePeriod.getStartElement().toHumanDisplay()).isEqualTo(expectedStartDate);
        assertThat(effectivePeriod.hasEndElement()).isFalse();
    }

    @SneakyThrows
    private RCMRMT030101UKMedicationStatement unmarshallMedicationStatement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_MEDICATION_STATEMENT + fileName),
            RCMRMT030101UKMedicationStatement.class);
    }

    @SneakyThrows
    private RCMRMT030101UKEhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_MEDICATION_STATEMENT + fileName), RCMRMT030101UKEhrExtract.class);
    }


    private MedicationStatement mapMedicationStatementFromEhrFile(String filename, DateTimeType authoredOn) {
        var ehrExtract = unmarshallEhrExtract(filename);
        var medicationStatement = extractMedicationStatement(ehrExtract);
        assertThat(medicationStatement.isPresent()).isTrue();

        var authorise = extractAuthorise(medicationStatement.orElseThrow());
        assertThat(authorise.isPresent()).isTrue();

        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));

        return medicationStatementMapper.mapToMedicationStatement(
            ehrExtract, medicationStatement.orElseThrow(), authorise.orElseThrow(), PRACTISE_CODE, authoredOn);
    }

    private Optional<RCMRMT030101UKMedicationStatement> extractMedicationStatement(RCMRMT030101UKEhrExtract ehrExtract) {
        return ehrExtract
            .getComponent()
            .stream()
            .map(RCMRMT030101UKComponent::getEhrFolder)
            .map(RCMRMT030101UKEhrFolder::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UKComponent3::getEhrComposition)
            .map(RCMRMT030101UKEhrComposition::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UKComponent4::getMedicationStatement)
            .findFirst();
    }

    private Optional<RCMRMT030101UKAuthorise> extractAuthorise(RCMRMT030101UKMedicationStatement medicationStatement) {

        return medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UKComponent2::getEhrSupplyAuthorise)
            .findFirst();
    }
}
