package uk.nhs.adaptors.pss.translator.mapper;

import static org.springframework.util.ResourceUtils.getFile;
import static org.assertj.core.api.Assertions.assertThat;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class ImmunizationMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Immunization/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Immunization-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String VACCINE_PROCEDURE_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-VaccinationProcedure-1";

    private final ImmunizationMapper immunizationMapper = new ImmunizationMapper();


    @Test
    public void mapObservationToImmunizationWithValidData() {
        var observationStatement = unmarshallCodeElement("full_valid_immunization.xml");
        immunizationMapper.mapToImmunization(observationStatement, "655D5A78-1F63-434C-9637-1D7E7843341B",
            "655D5A78-1F63-434C-9637-1D7E7843341B", new RCMRMT030101UK04EhrComposition()).forEach(
                immunization -> assertFullValidData(observationStatement, immunization)
        );
    }

    @Test
    public void mapObservationToImmunizationWithEffectiveTimeCenter() {
        var observationStatement = unmarshallCodeElement("immunization_with_only_center_effective_time.xml");
        immunizationMapper.mapToImmunization(observationStatement, "655D5A78-1F63-434C-9637-1D7E7843341B",
            "655D5A78-1F63-434C-9637-1D7E7843341B", new RCMRMT030101UK04EhrComposition()).forEach(immunization ->
            assertImmunizationWithHighEffectiveTimeCenter(immunization, observationStatement));
    }

    @Test
    public void mapObservationToImmunizationWithEffectiveTimeLow() {
        var observationStatement = unmarshallCodeElement("immunization_with_only_low_effective_time.xml");
        immunizationMapper.mapToImmunization(observationStatement, "655D5A78-1F63-434C-9637-1D7E7843341B",
            "655D5A78-1F63-434C-9637-1D7E7843341B", new RCMRMT030101UK04EhrComposition()).forEach(
                immunization -> assertImmunizationWithEffectiveTimeLow(immunization, observationStatement));
    }

    @Test
    public void mapObservationToImmunizationWithHighAndLowEffectiveTime() {
        var observationStatement = unmarshallCodeElement("immunization_with_high_and_low_effective_time.xml");
        immunizationMapper.mapToImmunization(observationStatement, "655D5A78-1F63-434C-9637-1D7E7843341B",
            "655D5A78-1F63-434C-9637-1D7E7843341B", new RCMRMT030101UK04EhrComposition()).forEach(
                immunization -> assertImmunizationWithHighAndLowEffectiveTime(immunization, observationStatement));
    }

    @Test
    public void mapObservationToImmunizationWithHighEffectiveTime() {
        var observationStatement = unmarshallCodeElement("immunization_with_only_high_effective_time.xml");
        immunizationMapper.mapToImmunization(observationStatement, "655D5A78-1F63-434C-9637-1D7E7843341B",
            "655D5A78-1F63-434C-9637-1D7E7843341B", new RCMRMT030101UK04EhrComposition()).forEach(
                immunization -> assertImmunizationWithHighEffectiveTime(immunization, observationStatement));
    }

    private void assertImmunizationWithHighAndLowEffectiveTime(Immunization immunization,
        RCMRMT030101UK04ObservationStatement observationStatement) {
        assertThat(immunization.getDate()).isEqualTo("2011-01-18T11:41:00.000");
        assertThat(immunization.getNote().get(0).getText())
            .isEqualTo(observationStatement.getPertinentInformation().get(0).getPertinentAnnotation().getText()
                + " End Date: 20100118114100");
    }

    private void assertImmunizationWithEffectiveTimeLow(Immunization immunization,
        RCMRMT030101UK04ObservationStatement observationStatement) {
        assertThat(immunization.getDate()).isEqualTo("2010-01-18T11:41:00.000");
        assertThat(immunization.getNote().get(0).getText())
            .isEqualTo(observationStatement.getPertinentInformation().get(0).getPertinentAnnotation().getText());
    }

    private void assertImmunizationWithHighEffectiveTime(Immunization immunization,
        RCMRMT030101UK04ObservationStatement observationStatement) {
        assertThat(immunization.getDate()).isNull();
        assertThat(immunization.getNote().get(0).getText())
            .isEqualTo(observationStatement.getPertinentInformation().get(0).getPertinentAnnotation().getText()
                + " End Date: 20100118114100");
    }

    private void assertImmunizationWithHighEffectiveTimeCenter(Immunization immunization,
        RCMRMT030101UK04ObservationStatement observationStatement) {
        assertThat(immunization.getDate()).isEqualTo("2010-01-18T11:41:00.000");
        assertThat(immunization.getNote().get(0).getText())
            .isEqualTo(observationStatement.getPertinentInformation().get(0).getPertinentAnnotation().getText());
    }

    private void assertFullValidData(RCMRMT030101UK04ObservationStatement observationStatement, Immunization immunization) {
        assertThat(immunization.getId()).isEqualTo(observationStatement.getId().getRoot());
        assertThat(immunization.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThatIdentifierIsValid(immunization.getIdentifierFirstRep(), immunization.getId());
        assertThat(immunization.getStatus()).isEqualTo(Immunization.ImmunizationStatus.COMPLETED);
        assertThat(immunization.getPrimarySource()).isEqualTo(false);
        assertThat(immunization.getDate()).isEqualTo("2010-01-18T11:41:00.000");
        assertThat(immunization.getNote().get(0).getText())
            .isEqualTo(observationStatement.getPertinentInformation().get(0).getPertinentAnnotation().getText()
                + " End Date: 20100118114100");
    }

    private void assertThatIdentifierIsValid(Identifier identifier, String id) {
        assertThat(identifier.getSystem()).isEqualTo(IDENTIFIER_SYSTEM); // TODO assert that source practice org id is concatenated
        assertThat(identifier.getValue()).isEqualTo(id);
    }

    @SneakyThrows
    private RCMRMT030101UK04ObservationStatement unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04ObservationStatement.class);
    }


}
