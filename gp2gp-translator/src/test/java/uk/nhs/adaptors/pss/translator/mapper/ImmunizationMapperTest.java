package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class ImmunizationMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Immunization/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Immunization-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String OBSERVATION_ROOT_ID = "82A39454-299F-432E-993E-5A6232B4E099";
    private static final String OBSERVATION_TEXT = "Primary Source: true Location: EMIS Test Practice Location Manufacturer: " +
        "another company Batch: past2003 Expiration: 2003-01-17 Site: Right arm GMS : Not GMS";

    private final ImmunizationMapper immunizationMapper = new ImmunizationMapper();

    @Test
    public void mapObservationToImmunizationWithValidData() {
        var ehrComposition = unmarshallEhrComposition("full_valid_immunization.xml");
        var observationStatement = ehrComposition.getComponent().get(0).getObservationStatement();
        List immunizationList = immunizationMapper.mapToImmunization(observationStatement, "9A5D5A78-1F63-434C-9637-1D7E7843341B",
            "655D5A78-1F63-434C-9637-1D7E7843341B", ehrComposition);
        var immunization = (Immunization) immunizationList.get(0);

        assertFullValidData(immunization);
    }

    @Test
    public void mapObservationToImmunizationWithEffectiveTimeCenter() {
        var ehrComposition = unmarshallEhrComposition("immunization_with_only_center_effective_time.xml");
        var observationStatement = ehrComposition.getComponent().get(0).getObservationStatement();
        List immunizationList = immunizationMapper.mapToImmunization(observationStatement, "9A5D5A78-1F63-434C-9637-1D7E7843341B",
            "655D5A78-1F63-434C-9637-1D7E7843341B", ehrComposition);
        var immunization = (Immunization) immunizationList.get(0);

        assertImmunizationWithHighEffectiveTimeCenter(immunization);
    }

    @Test
    public void mapObservationToImmunizationWithEffectiveTimeLow() {
        var ehrComposition = unmarshallEhrComposition("immunization_with_only_low_effective_time.xml");
        var observationStatement = ehrComposition.getComponent().get(0).getObservationStatement();
        List immunizationList = immunizationMapper.mapToImmunization(observationStatement, "9A5D5A78-1F63-434C-9637-1D7E7843341B",
            "655D5A78-1F63-434C-9637-1D7E7843341B", ehrComposition);
        var immunization = (Immunization) immunizationList.get(0);

        assertImmunizationWithEffectiveTimeLow(immunization);
    }

    @Test
    public void mapObservationToImmunizationWithHighAndLowEffectiveTime() {
        var ehrComposition = unmarshallEhrComposition("immunization_with_high_and_low_effective_time.xml");
        var observationStatement = ehrComposition.getComponent().get(0).getObservationStatement();
        List immunizationList = immunizationMapper.mapToImmunization(observationStatement, "9A5D5A78-1F63-434C-9637-1D7E7843341B",
            "655D5A78-1F63-434C-9637-1D7E7843341B", ehrComposition);
        var immunization = (Immunization) immunizationList.get(0);

        assertImmunizationWithHighAndLowEffectiveTime(immunization);
    }

    @Test
    public void mapObservationToImmunizationWithHighEffectiveTime() {
        var ehrComposition = unmarshallEhrComposition("immunization_with_only_high_effective_time.xml");
        var observationStatement = ehrComposition.getComponent().get(0).getObservationStatement();
        List immunizationList = immunizationMapper.mapToImmunization(observationStatement, "9A5D5A78-1F63-434C-9637-1D7E7843341B",
            "655D5A78-1F63-434C-9637-1D7E7843341B", ehrComposition);
        var immunization = (Immunization) immunizationList.get(0);

        assertImmunizationWithHighEffectiveTime(immunization);
    }

    private void assertImmunizationWithHighAndLowEffectiveTime(Immunization immunization) {
        assertThat(immunization.getDate()).isEqualTo("2011-01-18T11:41:00.000");
        assertThat(immunization.getNote().get(0).getText())
            .isEqualTo(OBSERVATION_TEXT + " End Date: 20100118114100");
    }

    private void assertImmunizationWithEffectiveTimeLow(Immunization immunization) {
        assertThat(immunization.getDate()).isEqualTo("2010-01-18T11:41:00.000");
        assertThat(immunization.getNote().get(0).getText())
            .isEqualTo(OBSERVATION_TEXT);
    }

    private void assertImmunizationWithHighEffectiveTime(Immunization immunization) {
        assertThat(immunization.getDate()).isNull();
        assertThat(immunization.getNote().get(0).getText())
            .isEqualTo(OBSERVATION_TEXT + " End Date: 20100118114100");
    }

    private void assertImmunizationWithHighEffectiveTimeCenter(Immunization immunization) {
        assertThat(immunization.getDate()).isEqualTo("2010-01-18T11:41:00.000");
        assertThat(immunization.getNote().get(0).getText()).isEqualTo(OBSERVATION_TEXT);
    }

    private void assertFullValidData(Immunization immunization) {
        assertThat(immunization.getId()).isEqualTo(OBSERVATION_ROOT_ID);
        assertThat(immunization.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThatIdentifierIsValid(immunization.getIdentifierFirstRep(), immunization.getId());
        assertThat(immunization.getStatus()).isEqualTo(Immunization.ImmunizationStatus.COMPLETED);
        assertThat(immunization.getPrimarySource()).isEqualTo(false);
        assertThat(immunization.getDate()).isEqualTo("2010-01-18T11:41:00.000");
        assertThat(immunization.getNote().get(0).getText())
            .isEqualTo(OBSERVATION_TEXT + " End Date: 20100118114100");
        assertThat(immunization.getPatient().getReference()).isEqualTo("Patient/9A5D5A78-1F63-434C-9637-1D7E7843341B");
        assertThat(immunization.getEncounter().getReference()).isEqualTo("Encounter/655D5A78-1F63-434C-9637-1D7E7843341B");
    }

    private void assertThatIdentifierIsValid(Identifier identifier, String id) {
        assertThat(identifier.getSystem()).isEqualTo(IDENTIFIER_SYSTEM); // TODO assert that source practice org id is concatenated
        assertThat(identifier.getValue()).isEqualTo(id);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrComposition unmarshallEhrComposition(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrComposition.class);
    }
}
