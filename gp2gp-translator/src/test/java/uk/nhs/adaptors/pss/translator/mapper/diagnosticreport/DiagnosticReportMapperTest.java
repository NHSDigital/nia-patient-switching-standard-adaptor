package uk.nhs.adaptors.pss.translator.mapper.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertAll;


import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus.UNKNOWN;

@ExtendWith(MockitoExtension.class)
public class DiagnosticReportMapperTest {

    private static final String DIAGNOSTIC_REPORT_META_SUFFIX = "DiagnosticReport-1";
    private static final String PRACTICE_CODE = "TEST_PRACTICE_CODE";
    private static final String DIAGNOSTIC_REPORT_ID = "DIAGNOSTIC_REPORT_ID";
    private static final String NARRATIVE_STATEMENT_ID = "NARRATIVE_STATEMENT_ID_1";
    private static final String NARRATIVE_STATEMENT_ID_2 = "NARRATIVE_STATEMENT_ID_2";
    private static final String NARRATIVE_STATEMENT_2_TEXT = "TEST COMMENT";
    private static final String COMPOUND_STATEMENT_CHILD_ID = "COMPOUND_STATEMENT_CHILD_ID";
    private static final String ENCOUNTER_ID = "EHR_COMPOSITION_ID_1";
    private static final String NEW_OBSERVATION_ID = "NEW_OBSERVATION_ID";
    private static final InstantType ISSUED_ELEMENT = parseToInstantType("20100225154100");
    private static final Patient PATIENT = (Patient) new Patient().setId("PATIENT_TEST_ID");
    private static final String CONCLUSION_FIELD_TEXT = "TEXT_OF_DIRECT_COMPOUND_STATEMENT_CHILD_NARRATIVE_STATEMENT_1\n"
        + "TEXT_OF_DIRECT_COMPOUND_STATEMENT_CHILD_NARRATIVE_STATEMENT_2";

    private static final String NARRATIVE_STATEMENT_COMMENT_BLOCK = """
        CommentType:LABORATORY RESULT COMMENT(E141)
        CommentDate:20220308170025
        TEXT_OF_DIRECT_COMPOUND_STATEMENT_CHILD_NARRATIVE_STATEMENT""";

    private static final String NARRATIVE_STATEMENT_COMMENT_BLOCK_2 = """
        CommentType:UNKNOWN TYPE
        CommentDate:20220308170025
        TEST COMMENT
        """;

    private static final String EHR_EXTRACT_TEMPLATE = """
                <EhrExtract xmlns="urn:hl7-org:v3" classCode="EXTRACT" moodCode="EVN">
                    <component typeCode="COMP">
                        <ehrFolder classCode="FOLDER" moodCode="EVN">
                            <component typeCode="COMP">
                                <ehrComposition classCode="COMPOSITION" moodCode="EVN">
                                    <id root="EHR_COMPOSITION_ID_1" />
                                    <component typeCode="COMP">
                                        {DiagnosticReportCompoundStatement}
                                    </component>
                                </ehrComposition>
                            </component>
                        </ehrFolder>
                    </component>
                </EhrExtract>
                """;
    @Mock
    private IdGeneratorService idGeneratorService;

    @InjectMocks
    private DiagnosticReportMapper diagnosticReportMapper;


    @Test
    public void When_DiagnosticReportWithNoReferenceIsMapped_Expect_DiagnosticReportIsCorrectlyMapped() {
        var inputXml = buildEhrExtractStringFromDiagnosticReportXml(
                """
                <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                    <id root="DIAGNOSTIC_REPORT_ID"/>
                    <code code="16488004" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                    <availabilityTime value="20100225154100"/>
                </CompoundStatement>
                """);

        var ehrExtract = unmarshallEhrExtractFromXmlString(inputXml);

        var diagnosticReports = diagnosticReportMapper.mapResources(ehrExtract, PATIENT, List.of(), PRACTICE_CODE);
        var diagnosticReport = diagnosticReports.get(0);

        assertAll(
                () -> assertThat(diagnosticReport.getId())
                        .isEqualTo(DIAGNOSTIC_REPORT_ID),
                () -> assertThat(diagnosticReport.getMeta().getProfile().get(0).getValue())
                        .contains(DIAGNOSTIC_REPORT_META_SUFFIX),
                () -> assertThat(diagnosticReport.getIdentifierFirstRep().getSystem())
                        .contains(PRACTICE_CODE),
                () -> assertThat(diagnosticReport.getIdentifierFirstRep().getValue())
                        .isEqualTo(DIAGNOSTIC_REPORT_ID),
                () -> assertThat(diagnosticReport.getSubject().getResource().getIdElement().getValue())
                        .isEqualTo(PATIENT.getId()),
                () -> assertThat(diagnosticReport.getIssuedElement().getValueAsString())
                        .isEqualTo(ISSUED_ELEMENT.getValueAsString()),
                () -> assertThat(diagnosticReport.hasContext())
                        .isFalse(),
                () -> assertThat(diagnosticReport.getSpecimen())
                        .isEmpty(),
                () -> assertThat(diagnosticReport.getResult())
                        .isEmpty()
        );
    }

    @Test
    public void When_DiagnosticReportContainsIdExtensionWithPMIPOid_Expect_MappedExtensionsShouldContainPMIPOidAsUrn() {
        var inputXml = buildEhrExtractStringFromDiagnosticReportXml(
                """
                <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                    <id root="DIAGNOSTIC_REPORT_ID"/>
                    <id extension="TEST_PMIP_EXTENSION_VALUE" root="2.16.840.1.113883.2.1.4.5.5" />
                    <code code="16488004" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                    <availabilityTime value="20100225154100"/>
                </CompoundStatement>
                """);
        var ehrExtract = unmarshallEhrExtractFromXmlString(inputXml);

        var diagnosticReports = diagnosticReportMapper.mapResources(ehrExtract, PATIENT, List.of(), PRACTICE_CODE);
        var actualPMIPExtension = diagnosticReports.get(0).getIdentifier().get(1);

        assertAll(
                () -> assertThat(actualPMIPExtension.getSystem())
                        .isEqualTo("urn:oid:2.16.840.1.113883.2.1.4.5.5"),
                () -> assertThat(actualPMIPExtension.getValue())
                        .isEqualTo("TEST_PMIP_EXTENSION_VALUE")
        );
    }

    @Test
    public void When_DiagnosticReportWithSpecimenIsMapped_Expect_DiagnosticReportContainsMappedSpecimen() {
        var inputXml = buildEhrExtractStringFromDiagnosticReportXml(
                """
                <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                    <id root="COMPOUND_STATEMENT_ID_1"/>
                    <code code="16488004" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                    <component typeCode="COMP" contextConductionInd="true">
                        <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                            <id root="COMPOUND_STATEMENT_CHILD_ID_1"/>
                            <code code="123038009" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                            <specimen typeCode="SPC" />
                        </CompoundStatement>
                    </component>
                </CompoundStatement>
                """);
        var ehrExtract = unmarshallEhrExtractFromXmlString(inputXml);

        var diagnosticReports = diagnosticReportMapper.mapResources(ehrExtract, PATIENT, List.of(), PRACTICE_CODE);
        var diagnosticReport = diagnosticReports.get(0);

        assertAll(
                () -> assertThat(diagnosticReport.getSpecimen())
                        .isNotEmpty(),
                () -> assertThat(diagnosticReport.getSpecimenFirstRep().getReference())
                        .contains(COMPOUND_STATEMENT_CHILD_ID)
        );
    }

    @Test
    public void When_DiagnosticReportContainsUnknownCommentType_Expect_ResultReferenceIsCorrectlyMapped() {
        var inputXml = buildEhrExtractStringFromDiagnosticReportXml(
                """
                <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                    <id root="COMPOUND_STATEMENT_ID_1"/>
                    <code code="16488004" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                    <component typeCode="COMP" contextConductionInd="true">
                        <NarrativeStatement classCode="OBS" moodCode="EVN">
                            <id root="NARRATIVE_STATEMENT_ID_1"/>
                            <text mediaType="text/x-h7uk-pmip">
                                CommentType:LABORATORY RESULT COMMENT(E141)
                            </text>
                        </NarrativeStatement>
                    </component>
                    <component typeCode="COMP" contextConductionInd="true">
                        <NarrativeStatement classCode="OBS" moodCode="EVN">
                            <id root="NARRATIVE_STATEMENT_ID_2"/>
                            <text mediaType="text/x-h7uk-pmip">
                                CommentType:UNKNOWN TYPE
                            </text>
                        </NarrativeStatement>
                    </component>
                </CompoundStatement>
                """);
        var ehrExtract = unmarshallEhrExtractFromXmlString(inputXml);

        var diagnosticReports = diagnosticReportMapper.mapResources(ehrExtract, PATIENT, List.of(), PRACTICE_CODE);
        var diagnosticReport = diagnosticReports.get(0);

        assertAll(
                () -> assertThat(diagnosticReport.getResult())
                        .hasSize(1),
                () -> assertThat(diagnosticReport.getResultFirstRep().getReference())
                        .contains(NARRATIVE_STATEMENT_ID_2)
        );
    }

    @Test
    public void When_DiagnosticReportContainsUserCommentType_Expect_ResultReferenceIsCorrectlyMapped() {
        var inputXml = buildEhrExtractStringFromDiagnosticReportXml(
                """
                <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                    <id root="COMPOUND_STATEMENT_ID_1"/>
                    <code code="16488004" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                    <component typeCode="COMP" contextConductionInd="true">
                        <NarrativeStatement classCode="OBS" moodCode="EVN">
                            <id root="NARRATIVE_STATEMENT_ID_1"/>
                            <text mediaType="text/x-h7uk-pmip">
                                CommentType:LABORATORY RESULT COMMENT(E141)
                            </text>
                        </NarrativeStatement>
                    </component>
                    <component typeCode="COMP" contextConductionInd="true">
                        <NarrativeStatement classCode="OBS" moodCode="EVN">
                            <id root="NARRATIVE_STATEMENT_ID_2"/>
                            <text mediaType="text/x-h7uk-pmip">
                                CommentType:USER COMMENT
                            </text>
                        </NarrativeStatement>
                    </component>
                </CompoundStatement>
                """);
        var ehrExtract = unmarshallEhrExtractFromXmlString(inputXml);

        var diagnosticReports = diagnosticReportMapper.mapResources(ehrExtract, PATIENT, List.of(), PRACTICE_CODE);
        var diagnosticReport = diagnosticReports.get(0);

        assertAll(
                () -> assertThat(diagnosticReport.getResult())
                        .hasSize(1),
                () -> assertThat(diagnosticReport.getResultFirstRep().getReference())
                        .contains(NARRATIVE_STATEMENT_ID_2)
        );
    }

    @Test
    public void When_DiagnosticReportContainsChildObservationComment_Expect_ObservationCommentIsCorrectlyCreated() {
        var inputXml = buildEhrExtractStringFromDiagnosticReportXml(
                """
                <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                    <id root="COMPOUND_STATEMENT_ID_1"/>
                    <code code="16488004" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                    <component typeCode="COMP" contextConductionInd="true">
                        <NarrativeStatement classCode="OBS" moodCode="EVN">
                            <id root="NARRATIVE_STATEMENT_ID_1"/>
                            <text mediaType="text/x-h7uk-pmip">
                                CommentType:LABORATORY RESULT COMMENT(E141)
                                CommentDate:20220308170025
                                TEXT_OF_DIRECT_COMPOUND_STATEMENT_CHILD_NARRATIVE_STATEMENT
                            </text>
                        </NarrativeStatement>
                    </component>
                    <component typeCode="COMP" contextConductionInd="true">
                        <NarrativeStatement classCode="OBS" moodCode="EVN">
                            <id root="NARRATIVE_STATEMENT_ID_2"/>
                            <text mediaType="text/x-h7uk-pmip">
                                CommentType:UNKNOWN TYPE
                                CommentDate:20220308170025
                                TEST COMMENT
                            </text>
                        </NarrativeStatement>
                    </component>
                </CompoundStatement>
                """);

        var ehrExtract = unmarshallEhrExtractFromXmlString(inputXml);
        List<Observation> observationComments = createObservationCommentList();

        diagnosticReportMapper.handleChildObservationComments(ehrExtract, observationComments);

        assertAll(
                () -> assertThat(observationComments)
                        .hasSize(1),
                () -> assertThat(observationComments.get(0).hasEffective())
                        .isFalse(),
                () -> assertThat(observationComments.get(0).getComment())
                        .isEqualTo(NARRATIVE_STATEMENT_2_TEXT)
        );
    }

    @Test
    public void When_MappingDiagnosticReport_Expect_DiagnosticReportContainsContextReferenceToParentEhrComposition() {
        var inputXml = buildEhrExtractStringFromDiagnosticReportXml(
                """
                <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                    <id root="COMPOUND_STATEMENT_ID_1"/>
                    <code code="16488004" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                    <component typeCode="COMP" contextConductionInd="true">
                        <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                            <id root="COMPOUND_STATEMENT_CHILD_ID_1"/>
                            <code code="123038009" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                        </CompoundStatement>
                    </component>
                </CompoundStatement>
                """);
        var ehrExtract = unmarshallEhrExtractFromXmlString(inputXml);

        var diagnosticReports = diagnosticReportMapper.mapResources(
                ehrExtract,
                PATIENT,
                createEncounterList(),
                PRACTICE_CODE);
        var diagnosticReport = diagnosticReports.get(0);

        assertAll(
                () -> assertThat(diagnosticReport.hasContext())
                        .isTrue(),
                () -> assertThat(diagnosticReport.getContext().getResource())
                        .isNotNull(),
                () -> assertThat(diagnosticReport.getContext().getResource().getIdElement().getValue())
                        .isEqualTo(ENCOUNTER_ID)
        );
    }

    @Test
    public void When_DiagnosticReportIsMapped_Expect_LaboratoryResultCommentsAreMappedToConclusionText() {
        var inputXml = buildEhrExtractStringFromDiagnosticReportXml(
                """
                <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                    <id root="COMPOUND_STATEMENT_ID_1"/>
                    <code code="16488004" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                    <component typeCode="COMP" contextConductionInd="true">
                        <NarrativeStatement classCode="OBS" moodCode="EVN">
                            <id root="NARRATIVE_STATEMENT_ID_1"/>
                            <text mediaType="text/x-h7uk-pmip">CommentType:LABORATORY RESULT COMMENT(E141)
CommentDate:20220308170025
TEXT_OF_DIRECT_COMPOUND_STATEMENT_CHILD_NARRATIVE_STATEMENT_1
                            </text>
                        </NarrativeStatement>
                    </component>
                    <component typeCode="COMP" contextConductionInd="true">
                        <NarrativeStatement classCode="OBS" moodCode="EVN">
                            <id root="NARRATIVE_STATEMENT_ID_2"/>
                            <text mediaType="text/x-h7uk-pmip">CommentType:LABORATORY RESULT COMMENT(E141)
CommentDate:20220308170025
TEXT_OF_DIRECT_COMPOUND_STATEMENT_CHILD_NARRATIVE_STATEMENT_2
                            </text>
                        </NarrativeStatement>
                    </component>
                    <component typeCode="COMP" contextConductionInd="true">
                        <NarrativeStatement classCode="OBS" moodCode="EVN">
                            <id root="NARRATIVE_STATEMENT_ID_2"/>
                            <text mediaType="text/x-h7uk-pmip">CommentType:UNKNOWN TYPE
CommentDate:20220308170025
TEST COMMENT
                             </text>
                        </NarrativeStatement>
                    </component>
                </CompoundStatement>
                """);
        var ehrExtract = unmarshallEhrExtractFromXmlString(inputXml);

        var diagnosticReports = diagnosticReportMapper.mapResources(
                ehrExtract,
                PATIENT,
                createEncounterList(),
                PRACTICE_CODE);
        var diagnosticReport = diagnosticReports.get(0);

        assertAll(
                () -> assertThat(diagnosticReport.hasConclusion())
                        .isTrue(),
                () -> assertThat(diagnosticReport.getConclusion())
                        .isEqualTo(CONCLUSION_FIELD_TEXT)
        );
    }

    @Test
    public void When_MappingWithUserCommentInBattery_Expect_DiagnosticReportToReferenceFilingComment() {
        var inputXml = buildEhrExtractStringFromDiagnosticReportXml(
                """
                <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                    <id root="C515E71B-2473-11EE-808B-AC162D1F16F0"/>
                    <code code="16488004" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                    <component typeCode="COMP" contextConductionInd="true">
                        <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                            <id root="C515E71C-2473-11EE-808B-AC162D1F16F0"/>
                            <component typeCode="COMP" contextConductionInd="true">
                                <CompoundStatement classCode="BATTERY" moodCode="EVN">
                                    <id root="C515E71D-2473-11EE-808B-AC162D1F16F0"/>
                                    <component typeCode="COMP" contextConductionInd="true">
                                        <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                                            <id root="C515E71F-2473-11EE-808B-AC162D1F16F0"/>
                                            <component typeCode="COMP" contextConductionInd="true">
                                                <NarrativeStatement classCode="OBS" moodCode="EVN">
                                                    <id root="C515E720-2473-11EE-808B-AC162D1F16F0"/>
                                                    <text mediaType="text/x-h7uk-pmip">
                                                        CommentType:AGGREGATE COMMENT SET
                                                    </text>
                                                </NarrativeStatement>
                                            </component>
                                        </CompoundStatement>
                                    </component>
                                    <component typeCode="COMP" contextConductionInd="true">
                                        <NarrativeStatement classCode="OBS" moodCode="EVN">
                                            <id root="C515E722-2473-11EE-808B-AC162D1F16F0"/>
                                            <effectiveTime value="01012024"/>
                                            <text mediaType="text/x-h7uk-pmip">
                                                CommentType:USER COMMENT
                                            </text>
                                        </NarrativeStatement>
                                    </component>
                                </CompoundStatement>
                            </component>
                        </CompoundStatement>
                    </component>
                </CompoundStatement>
                """);
        final var ehrExtract = unmarshallEhrExtractFromXmlString(inputXml);
        final var batteryLevelFilingComment = createBatteryLevelFilingComment();
        final var expectedReference = "Observation/" + NEW_OBSERVATION_ID;

        when(idGeneratorService.generateUuid()).thenReturn(NEW_OBSERVATION_ID);

        final var diagnosticReports = diagnosticReportMapper.mapResources(
                ehrExtract,
                PATIENT,
                createEncounterList(),
                PRACTICE_CODE,
                new ArrayList<>(Collections.singleton(batteryLevelFilingComment)));

        final var diagnosticReport = diagnosticReports.get(0);

        assertAll(
                () -> assertThat(diagnosticReport.getResult())
                        .hasSize(1),
                () -> assertThat(diagnosticReport.getResult().get(0).getReference())
                        .isEqualTo(expectedReference)
        );
    }

    @Test
    public void When_MappingWithUserCommentInBattery_Expect_NewFilingCommentObservationCreatedWithCommentField() {
        var inputXml = buildEhrExtractStringFromDiagnosticReportXml(
                """
                <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                    <id root="C515E71B-2473-11EE-808B-AC162D1F16F0"/>
                    <code code="16488004" codeSystem="2.16.840.1.113883.2.1.3.2.4.15" />
                    <component typeCode="COMP" contextConductionInd="true">
                        <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                            <id root="C515E71C-2473-11EE-808B-AC162D1F16F0"/>
                            <component typeCode="COMP" contextConductionInd="true">
                                <CompoundStatement classCode="BATTERY" moodCode="EVN">
                                    <id root="C515E71D-2473-11EE-808B-AC162D1F16F0"/>
                                    <component typeCode="COMP" contextConductionInd="true">
                                        <CompoundStatement classCode="CLUSTER" moodCode="EVN">
                                            <id root="C515E71F-2473-11EE-808B-AC162D1F16F0"/>
                                            <component typeCode="COMP" contextConductionInd="true">
                                                <NarrativeStatement classCode="OBS" moodCode="EVN">
                                                    <id root="C515E720-2473-11EE-808B-AC162D1F16F0"/>
                                                    <text mediaType="text/x-h7uk-pmip">
                                                        CommentType:AGGREGATE COMMENT SET
                                                    </text>
                                                </NarrativeStatement>
                                            </component>
                                        </CompoundStatement>
                                    </component>
                                    <component typeCode="COMP" contextConductionInd="true">
                                        <NarrativeStatement classCode="OBS" moodCode="EVN">
                                            <id root="C515E722-2473-11EE-808B-AC162D1F16F0"/>
                                            <effectiveTime value="01012024"/>
                                            <text mediaType="text/x-h7uk-pmip">
                                                CommentType:USER COMMENT
                                            </text>
                                        </NarrativeStatement>
                                    </component>
                                </CompoundStatement>
                            </component>
                        </CompoundStatement>
                    </component>
                </CompoundStatement>
                """);
        final var ehrExtract = unmarshallEhrExtractFromXmlString(inputXml);
        final var batteryLevelFilingComment = createBatteryLevelFilingComment();
        var observationComments = new ArrayList<>(Collections.singleton(batteryLevelFilingComment));

        when(idGeneratorService.generateUuid()).thenReturn(NEW_OBSERVATION_ID);

        diagnosticReportMapper.mapResources(ehrExtract,
                PATIENT,
                createEncounterList(),
                PRACTICE_CODE,
                observationComments);

        assertAll(
                () -> assertThat(observationComments)
                        .hasSize(2),
                () -> assertThat(observationComments.get(1).getId())
                        .isEqualTo(NEW_OBSERVATION_ID),
                () -> assertThat(observationComments.get(1).getIdentifierFirstRep().getValue())
                        .isEqualTo(NEW_OBSERVATION_ID),
                () -> assertThat(observationComments.get(1).getEffectiveDateTimeType().getValueAsString())
                        .isEqualTo("2024-01-01"),
                () -> assertThat(observationComments.get(1).getComment()).isNull(),
                () -> assertThat(observationComments.get(1).getStatus()).isEqualTo(UNKNOWN)
        );
    }

    private List<Observation> createObservationCommentList() {
        return new ArrayList<>(Arrays.asList(
                (Observation) new Observation()
                        .setEffective(new DateTimeType())
                        .setComment(NARRATIVE_STATEMENT_COMMENT_BLOCK)
                        .setId(NARRATIVE_STATEMENT_ID),
                (Observation) new Observation()
                        .setEffective(new DateTimeType())
                        .setComment(NARRATIVE_STATEMENT_COMMENT_BLOCK_2)
                        .setId(NARRATIVE_STATEMENT_ID_2)
        ));
    }

    private Observation createBatteryLevelFilingComment() {
        var identifier = new Identifier();
        identifier.setSystem("https://PSSAdaptor/" + PRACTICE_CODE);
        identifier.setValue("C515E722-2473-11EE-808B-AC162D1F16F0");
        return (Observation) new Observation()
                        .setEffective(new DateTimeType())
                        .setComment("This is a comment from the doctor")
                        .setEffective(DateFormatUtil.parseToDateTimeType("20240101"))
                        .setId("C515E722-2473-11EE-808B-AC162D1F16F0");
    }

    private List<Encounter> createEncounterList() {
        return List.of((Encounter) new Encounter().setId(ENCOUNTER_ID));
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtractFromXmlString(String xmlString) {
        return unmarshallString(xmlString, RCMRMT030101UK04EhrExtract.class);
    }

    private static String buildEhrExtractStringFromDiagnosticReportXml(String diagnosticReportXml) {
        return EHR_EXTRACT_TEMPLATE.replace("{DiagnosticReportCompoundStatement}", diagnosticReportXml);
    }
}
