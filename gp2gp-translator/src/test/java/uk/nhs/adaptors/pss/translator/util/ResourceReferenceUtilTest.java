package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import lombok.SneakyThrows;

public class ResourceReferenceUtilTest {

    private static final String XML_RESOURCES_COMPOSITION = "xml/ResourceReference/EhrComposition/";
    private static final String XML_RESOURCES_COMPOUND = "xml/ResourceReference/CompoundStatement/";
    private static final int THREE = 3;
    private static final int FOUR = 4;

    @Test
    public void testMedicationResourcesReferencedAtEhrCompositionLevel() {
        final RCMRMT030101UK04EhrComposition ehrComposition = unmarshallEhrCompositionElement("ehr_composition_medication.xml");

        List<Reference> references = new ArrayList<>();
        ResourceReferenceUtil.extractChildReferencesFromEhrComposition(ehrComposition, references);

        assertThat(references.size()).isEqualTo(THREE);
        assertThat(references.get(0).getReference()).isEqualTo("MedicationStatement/A0A70B62-2649-4C8F-B3AB-618B8257C942-MS");
        assertThat(references.get(1).getReference()).isEqualTo("MedicationRequest/A0A70B62-2649-4C8F-B3AB-618B8257C942");
        assertThat(references.get(2).getReference()).isEqualTo("MedicationRequest/9B4B797A-D674-4362-B666-2ADC8551EEDA");
    }

    @Test
    public void testMedicationResourcesReferencedAtCompoundStatementLevel() {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement("compound_statement_medication.xml");

        List<Reference> references = new ArrayList<>();
        ResourceReferenceUtil.extractChildReferencesFromCompoundStatement(compoundStatement, references);

        assertThat(references.size()).isEqualTo(THREE);
        assertThat(references.get(0).getReference()).isEqualTo("MedicationStatement/A0A70B62-2649-4C8F-B3AB-618B8257C942-MS");
        assertThat(references.get(1).getReference()).isEqualTo("MedicationRequest/A0A70B62-2649-4C8F-B3AB-618B8257C942");
        assertThat(references.get(2).getReference()).isEqualTo("MedicationRequest/9B4B797A-D674-4362-B666-2ADC8551EEDA");
    }

    @Test
    public void testTemplateResourcesReferencedAtEhrCompositionLevel() {
        final RCMRMT030101UK04EhrComposition ehrComposition = unmarshallEhrCompositionElement("ehr_composition_template.xml");

        List<Reference> references = new ArrayList<>();
        ResourceReferenceUtil.extractChildReferencesFromEhrComposition(ehrComposition, references);

        assertThat(references.size()).isEqualTo(FOUR);
        assertThat(references.get(0).getReference()).isEqualTo("QuestionnaireResponse/7334D39A-BBB3-424A-B5D3-E841BCA39BF7-QRSP");
        assertThat(references.get(1).getReference()).isEqualTo("Observation/7334D39A-BBB3-424A-B5D3-E841BCA39BF7");
        assertThat(references.get(2).getReference()).isEqualTo("Observation/3DCC9FC9-1873-4004-9789-C4E5C52B02B9");
        assertThat(references.get(THREE).getReference()).isEqualTo("Observation/278ADD5F-2AC7-48DC-966A-0BA7C029C793");
    }

    @Test
    public void testTemplateResourcesReferencedAtCompoundStatementLevel() {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement("compound_statement_template.xml");

        List<Reference> references = new ArrayList<>();
        ResourceReferenceUtil.extractChildReferencesFromCompoundStatement(compoundStatement, references);

        assertThat(references.size()).isEqualTo(FOUR);
        assertThat(references.get(0).getReference()).isEqualTo("QuestionnaireResponse/7334D39A-BBB3-424A-B5D3-E841BCA39BF7-QRSP");
        assertThat(references.get(1).getReference()).isEqualTo("Observation/7334D39A-BBB3-424A-B5D3-E841BCA39BF7");
        assertThat(references.get(2).getReference()).isEqualTo("Observation/3DCC9FC9-1873-4004-9789-C4E5C52B02B9");
        assertThat(references.get(THREE).getReference()).isEqualTo("Observation/278ADD5F-2AC7-48DC-966A-0BA7C029C793");
    }

    @Test
    public void testTemplateChildResourcesReferencedAsQuestionnaireAnswers() {
        final RCMRMT030101UK04EhrComposition ehrComposition = unmarshallEhrCompositionElement("ehr_composition_template.xml");

        List<Reference> references = new ArrayList<>();
        ResourceReferenceUtil.extractChildReferencesFromTemplate(ehrComposition.getComponent().get(0).getCompoundStatement(), references);

        assertThat(references.size()).isEqualTo(2);
        assertThat(references.get(0).getReference()).isEqualTo("Observation/3DCC9FC9-1873-4004-9789-C4E5C52B02B9");
        assertThat(references.get(1).getReference()).isEqualTo("Observation/278ADD5F-2AC7-48DC-966A-0BA7C029C793");
    }

    @ParameterizedTest
    @MethodSource("ehrCompositionResourceFiles")
    public void testResourcesReferencedAtEhrCompositionLevel(String inputXML, String referenceString) {
        final RCMRMT030101UK04EhrComposition ehrComposition = unmarshallEhrCompositionElement(inputXML);

        List<Reference> references = new ArrayList<>();
        ResourceReferenceUtil.extractChildReferencesFromEhrComposition(ehrComposition, references);

        assertThat(references.size()).isOne();
        assertThat(references.get(0).getReference()).isEqualTo(referenceString);
    }

    private static Stream<Arguments> ehrCompositionResourceFiles() {
        return Stream.of(
            Arguments.of("ehr_composition_observation_comment.xml", "Observation/5E496953-065B-41F2-9577-BE8F2FBD0757"),
            Arguments.of("ehr_composition_document_reference.xml", "DocumentReference/31B75ED0-6E88-11EA-9384-E83935108FD5"),
            Arguments.of("ehr_composition_immunization.xml", "Immunization/82A39454-299F-432E-993E-5A6232B4E099"),
            Arguments.of("ehr_composition_allergy_intolerance.xml", "AllergyIntolerance/35C0BE8D-F5F9-41C4-B819-4BE66FF8ED27"),
            Arguments.of("ehr_composition_observation_uncategorised.xml", "Observation/E9396E5B-B81A-4D69-BF0F-DFB1DFE80A33"),
            Arguments.of("ehr_composition_condition.xml", "Condition/5968B6B2-8E9A-4A78-8979-C8F14F4D274B"),
            Arguments.of("ehr_composition_blood_pressure.xml", "Observation/FE739904-2AAB-4B3F-9718-84BE019FD483"),
            Arguments.of("ehr_composition_diagnostic_report.xml", "DiagnosticReport/2E135210-74C2-478A-90DC-0FC9F7B8103C")
        );
    }

    @ParameterizedTest
    @MethodSource("compoundStatementResourceFiles")
    public void testResourcesReferencedAtCompoundStatementLevel(String inputXML, String referenceString) {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement(inputXML);

        List<Reference> references = new ArrayList<>();
        ResourceReferenceUtil.extractChildReferencesFromCompoundStatement(compoundStatement, references);

        assertThat(references.size()).isOne();
        assertThat(references.get(0).getReference()).isEqualTo(referenceString);
    }

    private static Stream<Arguments> compoundStatementResourceFiles() {
        return Stream.of(
            Arguments.of("compound_statement_observation_comment.xml", "Observation/5E496953-065B-41F2-9577-BE8F2FBD0757"),
            Arguments.of("compound_statement_document_reference.xml", "DocumentReference/31B75ED0-6E88-11EA-9384-E83935108FD5"),
            Arguments.of("compound_statement_immunization.xml", "Immunization/82A39454-299F-432E-993E-5A6232B4E099"),
            Arguments.of("compound_statement_allergy_intolerance.xml", "AllergyIntolerance/35C0BE8D-F5F9-41C4-B819-4BE66FF8ED27"),
            Arguments.of("compound_statement_observation_uncategorised.xml", "Observation/E9396E5B-B81A-4D69-BF0F-DFB1DFE80A33"),
            Arguments.of("compound_statement_condition.xml", "Condition/5968B6B2-8E9A-4A78-8979-C8F14F4D274B"),
            Arguments.of("compound_statement_blood_pressure.xml", "Observation/FE739904-2AAB-4B3F-9718-84BE019FD483"),
            Arguments.of("compound_statement_diagnostic_report.xml", "DiagnosticReport/2E135210-74C2-478A-90DC-0FC9F7B8103C")
        );
    }

    @SneakyThrows
    private RCMRMT030101UK04CompoundStatement unmarshallCompoundStatementElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_COMPOUND + fileName),
            RCMRMT030101UK04CompoundStatement.class);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrComposition unmarshallEhrCompositionElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_COMPOSITION + fileName), RCMRMT030101UK04EhrComposition.class);
    }
}
