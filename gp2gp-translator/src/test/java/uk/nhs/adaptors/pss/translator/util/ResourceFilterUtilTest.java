package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.stream.Stream;

import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import lombok.SneakyThrows;

public class ResourceFilterUtilTest {

    private static final String XML_RESOURCES = "xml/ResourceFilter/";

    @Test
    public void testIsDocumentReferenceResource() {
        final RCMRMT030101UK04NarrativeStatement narrativeStatement = unmarshallNarrativeStatementElement(
            "document_reference_resource.xml");

        assertThat(ResourceFilterUtil.isDocumentReference(narrativeStatement)).isTrue();
    }

    @Test
    public void testIsNotDocumentReferenceResource() {
        final RCMRMT030101UK04NarrativeStatement narrativeStatement = unmarshallNarrativeStatementElement(
            "observation_comment_resource.xml");

        assertThat(ResourceFilterUtil.isDocumentReference(narrativeStatement)).isFalse();
    }

    @Test
    public void testIsBloodPressureResource() {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement("blood_pressure_resource.xml");

        assertThat(ResourceFilterUtil.isBloodPressure(compoundStatement)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonBloodPressureTestFiles")
    public void testIsNotBloodPressureResource(String inputXML) {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement(inputXML);

        assertThat(ResourceFilterUtil.isBloodPressure(compoundStatement)).isFalse();
    }

    private static Stream<Arguments> nonBloodPressureTestFiles() {
        return Stream.of(
            Arguments.of("allergy_intolerance_resource.xml"),
            Arguments.of("diagnostic_report_resource.xml"),
            Arguments.of("template_resource.xml"),
            Arguments.of("specimen_resource.xml")
        );
    }

    @Test
    public void testIsAllergyIntoleranceResource() {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement("allergy_intolerance_resource.xml");

        assertThat(ResourceFilterUtil.isAllergyIntolerance(compoundStatement)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonAllergyIntoleranceTestFiles")
    public void testIsNotAllergyIntoleranceResource(String inputXML) {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement(inputXML);

        assertThat(ResourceFilterUtil.isAllergyIntolerance(compoundStatement)).isFalse();
    }

    private static Stream<Arguments> nonAllergyIntoleranceTestFiles() {
        return Stream.of(
            Arguments.of("blood_pressure_resource.xml"),
            Arguments.of("diagnostic_report_resource.xml"),
            Arguments.of("template_resource.xml"),
            Arguments.of("specimen_resource.xml")
        );
    }

    @Test
    public void testIsDiagnosticReportResource() {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement("diagnostic_report_resource.xml");

        assertThat(ResourceFilterUtil.isDiagnosticReport(compoundStatement)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonDiagnosticReportTestFiles")
    public void testIsNotBloodPressure(String inputXML) {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement(inputXML);

        assertThat(ResourceFilterUtil.isDiagnosticReport(compoundStatement)).isFalse();
    }

    private static Stream<Arguments> nonDiagnosticReportTestFiles() {
        return Stream.of(
            Arguments.of("allergy_intolerance_resource.xml"),
            Arguments.of("blood_pressure_resource.xml"),
            Arguments.of("template_resource.xml"),
            Arguments.of("specimen_resource.xml")
        );
    }

    @Test
    public void testIsTemplateResource() {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement("template_resource.xml");

        assertThat(ResourceFilterUtil.isTemplate(compoundStatement)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonTemplateTestFiles")
    public void testIsNotTemplateResource(String inputXML) {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement(inputXML);

        assertThat(ResourceFilterUtil.isTemplate(compoundStatement)).isFalse();
    }

    private static Stream<Arguments> nonTemplateTestFiles() {
        return Stream.of(
            Arguments.of("allergy_intolerance_resource.xml"),
            Arguments.of("diagnostic_report_resource.xml"),
            Arguments.of("blood_pressure_resource.xml"),
            Arguments.of("specimen_resource.xml")
        );
    }

    @Test
    public void testIsSpecimenResource() {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement("specimen_resource.xml");

        assertThat(ResourceFilterUtil.isSpecimen(compoundStatement)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonSpecimenTestFiles")
    public void testIsNotSpecimenResource(String inputXML) {
        final RCMRMT030101UK04CompoundStatement compoundStatement = unmarshallCompoundStatementElement(inputXML);

        assertThat(ResourceFilterUtil.isSpecimen(compoundStatement)).isFalse();
    }

    private static Stream<Arguments> nonSpecimenTestFiles() {
        return Stream.of(
            Arguments.of("allergy_intolerance_resource.xml"),
            Arguments.of("diagnostic_report_resource.xml"),
            Arguments.of("blood_pressure_resource.xml"),
            Arguments.of("template_resource.xml")
        );
    }

    @SneakyThrows
    private RCMRMT030101UK04CompoundStatement unmarshallCompoundStatementElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES + fileName),
            RCMRMT030101UK04CompoundStatement.class);
    }

    @SneakyThrows
    private RCMRMT030101UK04ObservationStatement unmarshallObservationStatementElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES + fileName),
            RCMRMT030101UK04ObservationStatement.class);
    }

    @SneakyThrows
    private RCMRMT030101UK04NarrativeStatement unmarshallNarrativeStatementElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES + fileName),
            RCMRMT030101UK04NarrativeStatement.class);
    }
}
