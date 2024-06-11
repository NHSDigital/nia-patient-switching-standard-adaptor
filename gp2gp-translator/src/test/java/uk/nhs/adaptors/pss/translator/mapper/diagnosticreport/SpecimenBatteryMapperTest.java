package uk.nhs.adaptors.pss.translator.mapper.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToDateTimeType;
import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationRelationshipType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;

import org.hl7.v3.RCMRMT030101UKCompoundStatement;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.mapper.CodeableConceptMapper;
import uk.nhs.adaptors.pss.translator.mapper.diagnosticreport.SpecimenBatteryMapper.SpecimenBatteryParameters;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;
import static uk.nhs.adaptors.common.util.CodeableConceptUtils.createCodeableConcept;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

@ExtendWith(MockitoExtension.class)
public class SpecimenBatteryMapperTest {

    public static final String EHR_EXTRACT_WRAPPER = """
        <EhrExtract xmlns="urn:hl7-org:v3" classCode="EXTRACT" moodCode="EVN">
            <component>
                <ehrFolder>
                    <component>
                        {{ehrComposition}}
                    </component>
                </ehrFolder>
            </component>
        </EhrExtract>
        """;
    private static final String RESOURCES_BASE = "xml/SpecimenBattery/";

    private static final String BATTERY_CLASSCODE = "BATTERY";
    private static final String PRACTISE_CODE = "TEST_PRACTISE_CODE";
    private static final String OBSERVATION_ID = "SPECIMEN_CHILD_BATTERY_COMPOUND_STATEMENT_ID_1";
    private static final String OBSERVATION_STATEMENT_ID_1 = "BATTERY_DIRECT_CHILD_OBSERVATION_STATEMENT";
    private static final String OBSERVATION_STATEMENT_ID_2 = "OBSERVATION_STATEMENT_ID";
    private static final String ENCOUNTER_ID = "ENCOUNTER_ID";
    private static final String PATIENT_ID = "TEST_PATIENT_ID";
    private static final String SPECIMEN_ID = "TEST_SPECIMEN_ID_1";
    private static final String META_PROFILE_SUFFIX = "Observation-1";
    private static final String EXPECTED_COMMENT = "Looks like Covid";
    private static final Patient PATIENT = (Patient) new Patient().setId(PATIENT_ID);
    private static final DateTimeType OBSERVATION_EFFECTIVE = parseToDateTimeType("20100223000000");
    private static final String CODING_DISPLAY_MOCK = "Test Display";
    private static final String SNOMED_SYSTEM = "http://snomed.info/sct";
    private static final CodeableConcept CODEABLE_CONCEPT = createCodeableConcept(null, SNOMED_SYSTEM, CODING_DISPLAY_MOCK);
    private final List<Encounter> encounters = generateEncounters();

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @InjectMocks
    private SpecimenBatteryMapper specimenBatteryMapper;

    @Test void When_MappingObservationWithEffectiveTimeInBatteryCompoundStatement_Expect_IssuedUsesThisValue() {
        final var ehrCompositionXml = """
            <ehrComposition>
                <id root="ENCOUNTER_ID"/>
                <component>
                    <CompoundStatement classCode="CLUSTER">
                        <id root="DR_TEST_ID"/>
                        <component>
                            <CompoundStatement classCode="CLUSTER">
                                <id root="TEST_SPECIMEN_ID_1"/>
                                <component typeCode="COMP" contextConductionInd="true">
                                    <CompoundStatement classCode="BATTERY" moodCode="EVN">
                                        <id root="SPECIMEN_CHILD_BATTERY_COMPOUND_STATEMENT_ID_1"/>
                                        <availabilityTime value="20100225154300"/>
                                    </CompoundStatement>
                                </component>
                            </CompoundStatement>
                        </component>
                    </CompoundStatement>
                </component>
            </ehrComposition>
            """;

        final var ehrExtract = unmarshallEhrExtractFromEhrCompositionXml(ehrCompositionXml);
        final var batteryCompoundStatement = getBatteryCompoundStatements(ehrExtract);
        final var batteryParameters = getSpecimenBatteryParameters(
            ehrExtract,
            batteryCompoundStatement,
            getObservations(),
            getObservationComments()
        );

        final Observation observation = specimenBatteryMapper.mapBatteryObservation(batteryParameters);

        assertThat(observation.getIssuedElement().asStringValue())
            .isEqualTo(parseToInstantType("20100225154300").asStringValue());
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtractFromEhrCompositionXml(String ehrCompositionXml) {
        var ehrExtractXml = EHR_EXTRACT_WRAPPER.replace("{{ehrComposition}}", ehrCompositionXml);
        return unmarshallString(ehrExtractXml, RCMRMT030101UK04EhrExtract.class);
    }

    @Test void When_MappingObservationWithEffectiveTimeInDiagnosticReport_Expect_IssuedUsesThisValue() {
        final var ehrCompositionXml = """
            <ehrComposition>
                <id root="ENCOUNTER_ID"/>
                <component>
                    <CompoundStatement classCode="CLUSTER">
                        <id root="DR_TEST_ID"/>
                        <availabilityTime value="20100225154200"/>
                        <component>
                            <CompoundStatement classCode="CLUSTER">
                                <id root="TEST_SPECIMEN_ID_1"/>
                                <component typeCode="COMP" contextConductionInd="true">
                                    <CompoundStatement classCode="BATTERY" moodCode="EVN">
                                        <id root="SPECIMEN_CHILD_BATTERY_COMPOUND_STATEMENT_ID_1"/>
                                    </CompoundStatement>
                                </component>
                            </CompoundStatement>
                        </component>
                    </CompoundStatement>
                </component>
            </ehrComposition>
            """;

        final var ehrExtract = unmarshallEhrExtractFromEhrCompositionXml(ehrCompositionXml);
        final var batteryCompoundStatement = getBatteryCompoundStatements(ehrExtract);
        final var batteryParameters = getSpecimenBatteryParameters(
            ehrExtract,
            batteryCompoundStatement,
            getObservations(),
            getObservationComments()
        );

        final Observation observation = specimenBatteryMapper.mapBatteryObservation(batteryParameters);

        assertThat(observation.getIssuedElement().asStringValue())
            .isEqualTo(parseToInstantType("20100225154200").asStringValue());
    }

    @Test void When_MappingObservationOnlyEhrCompositionAuthorTime_Expect_IssuedUsesThisValue() {
        final var ehrCompositionXml = """
            <ehrComposition>
                <id root="ENCOUNTER_ID"/>
                <author typeCode="AUT" contextControlCode="OP">
                    <time value="20220302105070"/>
                    <agentRef classCode="AGNT">
                        <id root="749107A2-4975-441F-8EDF-ADFF451FD12D"/>
                    </agentRef>
                </author>
                <component>
                    <CompoundStatement classCode="CLUSTER">
                        <id root="DR_TEST_ID"/>
                        <component>
                            <CompoundStatement classCode="CLUSTER">
                                <id root="TEST_SPECIMEN_ID_1"/>
                                <component typeCode="COMP" contextConductionInd="true">
                                    <CompoundStatement classCode="BATTERY" moodCode="EVN">
                                        <id root="SPECIMEN_CHILD_BATTERY_COMPOUND_STATEMENT_ID_1"/>
                                    </CompoundStatement>
                                </component>
                            </CompoundStatement>
                        </component>
                    </CompoundStatement>
                </component>
            </ehrComposition>
            """;

        final var ehrExtract = unmarshallEhrExtractFromEhrCompositionXml(ehrCompositionXml);
        final var batteryCompoundStatement = getBatteryCompoundStatements(ehrExtract);
        final var batteryParameters = getSpecimenBatteryParameters(
            ehrExtract,
            batteryCompoundStatement,
            getObservations(),
            getObservationComments()
        );

        final Observation observation = specimenBatteryMapper.mapBatteryObservation(batteryParameters);

        assertThat(observation.getIssuedElement().asStringValue())
            .isEqualTo(parseToInstantType("20220302105070").asStringValue());
    }

    @Test
    public void When_MappingObservation_Expect_ObservationCorrectlyMapped() {
        final var ehrExtract = unmarshallEhrExtract("specimen_battery_compound_statement.xml");
        final var batteryCompoundStatement = getBatteryCompoundStatements(ehrExtract);
        final var batteryParameters = getSpecimenBatteryParameters(
            ehrExtract,
            batteryCompoundStatement,
            getObservations(),
            getObservationComments()
        );

        final Observation observation = specimenBatteryMapper.mapBatteryObservation(batteryParameters);

        assertAll(
            () -> assertThat(observation.getId()).isEqualTo(OBSERVATION_ID),
            () -> assertThat(observation.getIdentifierFirstRep().getSystem()).contains(PRACTISE_CODE),
            () -> assertThat(observation.getEffectiveDateTimeType().getValueAsString())
                .isEqualTo(OBSERVATION_EFFECTIVE.getValueAsString()),
            () -> assertThat(observation.getSpecimen().hasReference()).isTrue(),
            () -> assertThat(observation.getSpecimen().getReference()).contains(SPECIMEN_ID),
            () -> assertThat(observation.getStatus()).isEqualTo(ObservationStatus.FINAL),
            () -> assertThat(observation.getMeta().getProfile().get(0).getValue()).contains(META_PROFILE_SUFFIX),
            () -> assertThat(observation.getComment()).isEqualTo(EXPECTED_COMMENT),
            () -> assertThat(observation.getContext().hasReference()).isTrue(),
            () -> assertThat(observation.getContext().getReference()).contains(ENCOUNTER_ID),
            () -> assertSubject(observation)
        );
    }

    @Test
    public void When_MappingObservation_Expect_ObservationRelationshipsSet() {
        final var ehrExtract = unmarshallEhrExtract("specimen_battery_compound_statement.xml");
        final var batteryCompoundStatement = getBatteryCompoundStatements(ehrExtract);
        final var observations = getObservations();
        final var batteryParameters = getSpecimenBatteryParameters(
            ehrExtract,
            batteryCompoundStatement,
            observations,
            getObservationComments());

        final Observation observation = specimenBatteryMapper.mapBatteryObservation(batteryParameters);

        assertAll(
            () -> assertThat(observations.get(0).getRelated())
                .isNotEmpty(),
            () -> assertThat(observations.get(0).getRelatedFirstRep().getType())
                .isEqualTo(ObservationRelationshipType.DERIVEDFROM),
            () -> assertRelated(observation)
        );
    }

    @Test
    public void When_MappingObservation_Expect_ObservationCommentsDoNotContainBatteryDirectChildNarrativeStatement() {
        final var ehrExtract = unmarshallEhrExtract("specimen_battery_compound_statement.xml");
        final var batteryCompoundStatement = getBatteryCompoundStatements(ehrExtract);
        final var observationComments = getObservationComments();
        final var batteryParameters = getSpecimenBatteryParameters(
            ehrExtract,
            batteryCompoundStatement,
            getObservations(),
            observationComments);

        specimenBatteryMapper.mapBatteryObservation(batteryParameters);

        final var observationCommentIds = observationComments.stream().map(Observation::getId).toList();

        assertAll(
            () -> assertThat(observationComments)
                .hasSize(2),
            () -> assertThat(observationCommentIds)
                .doesNotContain("BATTERY_DIRECT_CHILD_NARRATIVE_STATEMENT_ID")
        );
    }

    @Test
    public void When_MappingObservationFromBatteryCompoundStatementWithSnomedCode_Expect_CorrectlyMapped() {
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(CODEABLE_CONCEPT);

        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_battery_compound_statement.xml");
        var batteryCompoundStatement = getBatteryCompoundStatements(ehrExtract);

        var batteryParameters = getSpecimenBatteryParameters(
            ehrExtract,
            batteryCompoundStatement,
            getObservations(),
            getObservationComments());

        final Observation observation = specimenBatteryMapper.mapBatteryObservation(batteryParameters);

        assertThat(observation.getCode()).isEqualTo(CODEABLE_CONCEPT);
    }

    @Test
    public void When_MappingObservationFromBatteryCompoundStatementWithoutSnomedCode_Expect_DegradedCode() {

        var codeableConcept = createCodeableConcept("1.2.3.4.5", null, CODING_DISPLAY_MOCK);
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);

        final RCMRMT030101UK04EhrExtract ehrExtract = unmarshallEhrExtract("specimen_battery_compound_statement.xml");
        var batteryCompoundStatement = getBatteryCompoundStatements(ehrExtract);

        final List<Observation> observations = getObservations();
        final List<Observation> observationComments = getObservationComments();

        var batteryParameters = getSpecimenBatteryParameters(ehrExtract, batteryCompoundStatement, observations, observationComments);

        final Observation observation = specimenBatteryMapper.mapBatteryObservation(batteryParameters);

        assertThat(observation.getCode().getCodingFirstRep()).isEqualTo(DegradedCodeableConcepts.DEGRADED_OTHER);
    }

    private List<Observation> getObservationComments() {
        List<Observation> observationComments = new ArrayList<>();
        var batteryObservationComment = new Observation()
            .setComment("""
                           CommentType:SUPER COMMENT
                           CommentDate:20220321163025

                           Looks like Covid
                           """);

        batteryObservationComment.setId("BATTERY_DIRECT_CHILD_NARRATIVE_STATEMENT_ID");

        var otherClusterObservationComment = new Observation()
            .setComment("""
                CommentType:OTHER COMMENT
                CommentDate:20220321162705

                Or maybe not?
                """);

        otherClusterObservationComment.setId("OTHER_COMMENT_NARRATIVE_STATEMENT_ID");

        var userObservationComment = new Observation()
            .setComment("""
                CommentType:USER COMMENT
                CommentDate:20100223000000

                This should not be a part of Battery Observation.comment
                """);

        userObservationComment.setId("USER_COMMENT_NARRATIVE_STATEMENT_ID");

        observationComments.add(batteryObservationComment);
        observationComments.add(otherClusterObservationComment);
        observationComments.add(userObservationComment);

        return observationComments;
    }

    private void assertRelated(Observation observation) {
        assertThat(observation.getRelated()).isNotEmpty();
        assertThat(observation.getRelatedFirstRep().hasTarget()).isTrue();
        assertThat(observation.getRelatedFirstRep().getTarget().getReference()).contains(OBSERVATION_STATEMENT_ID_1);
        assertThat(observation.getRelated().get(1).getTarget().getReference()).contains(OBSERVATION_STATEMENT_ID_2);
    }

    private void assertSubject(Observation observation) {
        assertThat(observation.getSubject()).isNotNull();
        assertThat(observation.getSubject().getResource()).isNotNull();
        assertThat(observation.getSubject().getResource().getIdElement().getValue()).isEqualTo(PATIENT_ID);
    }

    private SpecimenBatteryParameters getSpecimenBatteryParameters(
        RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UKCompoundStatement batteryCompoundStatement,
        List<Observation> observations,
        List<Observation> observationComments) {

        return SpecimenBatteryParameters.builder()
            .ehrExtract(ehrExtract)
            .batteryCompoundStatement(batteryCompoundStatement)
            .specimenCompoundStatement(getSpecimenCompoundStatement(ehrExtract))
            .ehrComposition(getEhrComposition(ehrExtract))
            .diagnosticReport(getDiagnosticReport(ehrExtract))
            .patient(PATIENT)
            .encounters(encounters)
            .practiseCode(PRACTISE_CODE)
            .observations(observations)
            .observationComments(observationComments)
            .build();
    }

    private RCMRMT030101UKEhrComposition getEhrComposition(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent().get(0).getEhrComposition();
    }

    private RCMRMT030101UKCompoundStatement getSpecimenCompoundStatement(RCMRMT030101UK04EhrExtract ehrExtract) {
        return getEhrComposition(ehrExtract).getComponent().get(0).getCompoundStatement()
            .getComponent().get(0).getCompoundStatement();
    }

    private DiagnosticReport getDiagnosticReport(RCMRMT030101UK04EhrExtract ehrExtract) {
        var compoundStatement = getEhrComposition(ehrExtract).getComponent().get(0).getCompoundStatement();
        var diagnosticReport = new DiagnosticReport();
        diagnosticReport.setId(compoundStatement.getId().get(0).getRoot());

        if (compoundStatement.getAvailabilityTime() != null) {
            diagnosticReport.setIssued(
                parseToDateTimeType(compoundStatement.getAvailabilityTime().getValue()).getValue()
            );
        }

        return diagnosticReport;
    }

    private List<Observation> getObservations() {
        return List.of(
            (Observation) new Observation().setId(OBSERVATION_STATEMENT_ID_1),
            (Observation) new Observation().setId(OBSERVATION_STATEMENT_ID_2)
        );
    }

    private RCMRMT030101UKCompoundStatement getBatteryCompoundStatements(RCMRMT030101UK04EhrExtract ehrExtract) {

        return getEhrComposition(ehrExtract).getComponent()
            .stream()
            .flatMap(CompoundStatementResourceExtractors::extractAllCompoundStatements)
            .filter(compoundStatement -> BATTERY_CLASSCODE.equals(compoundStatement.getClassCode().get(0)))
            .findFirst().get();
    }

    private List<Encounter> generateEncounters() {
        return List.of((Encounter) new Encounter().setId(ENCOUNTER_ID));
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String filename) {
        return unmarshallFile(getFile("classpath:" + RESOURCES_BASE + filename), RCMRMT030101UK04EhrExtract.class);
    }
}

