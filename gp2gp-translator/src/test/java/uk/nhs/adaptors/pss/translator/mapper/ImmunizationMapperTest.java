package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.DatabaseImmunizationChecker;

@ExtendWith(MockitoExtension.class)
public class ImmunizationMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Immunization/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Immunization-1";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/TESTPRACTISECODE";
    private static final String OBSERVATION_ROOT_ID = "82A39454-299F-432E-993E-5A6232B4E099";
    private static final String OBSERVATION_TEXT = "Primary Source: true Location: EMIS Test Practice Location Manufacturer: "
        + "another company Batch: past2003 Expiration: 2003-01-17 Site: Right arm GMS : Not GMS";
    private static final String CODING_DISPLAY = "Ischaemic heart disease";
    private static final int THREE = 3;
    private static final String PATIENT_ID = "9A5D5A78-1F63-434C-9637-1D7E7843341B";
    private static final String ENCOUNTER_ID = "62A39454-299F-432E-993E-5A6232B4E099";

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @Mock
    private DatabaseImmunizationChecker immunizationChecker;

    @InjectMocks
    private ImmunizationMapper immunizationMapper;

    @BeforeEach
    public void setup() {
        setUpCodeableConceptMock();
    }

    @Test
    public void mapObservationToImmunizationWithValidData() {
        var ehrExtract = unmarshallEhrExtract("full_valid_immunization.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertFullValidData(immunization, immunizationList);
    }

    @Test
    public void mapObservationToImmunizationWithMissingValues() {
        var ehrExtract = unmarshallEhrExtract("immunization_with_missing_optional_values.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertMissingData(immunization, immunizationList);
    }

    @Test
    public void mapObservationToImmunizationWhenEhrCompositionIdIsNotPresentOnEncounterList() {
        var ehrExtract = unmarshallEhrExtract("immunization_with_ehr_composition_id_not_matching_encounter_id.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertThat(immunization.getEncounter().getReference()).isNull();
    }

    @Test
    public void mapObservationToImmunizationWithMultipleObservationStatements() {
        var ehrExtract = unmarshallEhrExtract("full_valid_immunization_with_multiple_observation_statements.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertThat(immunizationList).hasSize(THREE);
        assertThat(immunization.getId()).isEqualTo(OBSERVATION_ROOT_ID);
        assertThat(immunization.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThatIdentifierIsValid(immunization.getIdentifierFirstRep(), immunization.getId());
    }

    @Test
    public void mapObservationToImmunizationWithMultipleObservationStatementsAndCodingDisplay() {
        final String VACCINE_PROCEDURE_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-VaccinationProcedure-1";
        var ehrExtract = unmarshallEhrExtract("full_valid_immunization_with_multiple_observation_statements.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertThat(immunizationList).hasSize(THREE);
        assertThat(immunization.getId()).isEqualTo(OBSERVATION_ROOT_ID);
        assertThat(immunization.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertEquals(VACCINE_PROCEDURE_URL, immunization.getExtension().get(0).getUrl());
        assertEquals(CODING_DISPLAY, ((CodeableConcept) immunization.getExtension().get(0).getValue()).getCoding().get(0).getDisplay());

        assertThatIdentifierIsValid(immunization.getIdentifierFirstRep(), immunization.getId());
    }

    @Test
    public void mapObservationToImmunizationWithEffectiveTimeCenter() {
        var ehrExtract = unmarshallEhrExtract("immunization_with_only_center_effective_time.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertImmunizationWithHighEffectiveTimeCenter(immunization);
    }

    @Test
    public void mapObservationToImmunizationWithEffectiveTimeLow() {
        var ehrExtract = unmarshallEhrExtract("immunization_with_only_low_effective_time.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertImmunizationWithEffectiveTimeLow(immunization);
    }

    @Test
    public void mapObservationToImmunizationWithHighAndLowEffectiveTime() {
        var ehrExtract = unmarshallEhrExtract("immunization_with_high_and_low_effective_time.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertImmunizationWithHighAndLowEffectiveTime(immunization);
    }

    @Test
    public void mapObservationToImmunizationWithHighEffectiveTime() {
        var ehrExtract = unmarshallEhrExtract("immunization_with_only_high_effective_time.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertImmunizationWithHighEffectiveTime(immunization);
    }

    @Test
    public void mapObservationToImmunizationWithUNKVaccineCode() {
        var ehrExtract = unmarshallEhrExtract("immunization_with_only_high_effective_time.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertImmunizationWithDefaultVaccineCode(immunization);
    }

    private void assertImmunizationWithHighAndLowEffectiveTime(Immunization immunization) {
        assertThat(immunization.getDateElement().getValue()).isEqualTo(
            DateFormatUtil.parseToDateTimeType("20110118114100000").getValue());
        assertThat(immunization.getNote().get(0).getText()).isEqualTo(OBSERVATION_TEXT);
        assertThat(immunization.getNote().get(1).getText()).isEqualTo("End Date: 2010-01-18T11:41:00+00:00");
    }

    private void assertImmunizationWithEffectiveTimeLow(Immunization immunization) {
        assertThat(immunization.getDateElement().getValue()).isEqualTo(
            DateFormatUtil.parseToDateTimeType("20100118114100000").getValue());
        assertThat(immunization.getNote().get(0).getText()).isEqualTo(OBSERVATION_TEXT);
    }

    private void assertImmunizationWithHighEffectiveTime(Immunization immunization) {
        assertThat(immunization.getDate()).isNull();
        assertThat(immunization.getNote().get(0).getText()).isEqualTo(OBSERVATION_TEXT);
        assertThat(immunization.getNote().get(1).getText()).isEqualTo("End Date: 2010-01-18T11:41:00+00:00");
    }

    private void assertImmunizationWithDefaultVaccineCode(Immunization immunization) {
        assertThat(immunization.getVaccineCode()).isNotNull();
        assertThat(immunization.getVaccineCode().getCoding().get(0).getCode()).isEqualTo("UNK");
        assertThat(immunization.getVaccineCode().getCoding().get(0).getSystem()).isEqualTo("http://hl7.org/fhir/v3/NullFlavor");
    }

    private void assertImmunizationWithHighEffectiveTimeCenter(Immunization immunization) {
        assertThat(immunization.getDateElement().getValue()).isEqualTo(
            DateFormatUtil.parseToDateTimeType("20100118114100000").getValue());
        assertThat(immunization.getNote().get(0).getText()).isEqualTo(OBSERVATION_TEXT);
    }

    private void assertFullValidData(Immunization immunization, List<Immunization> immunizationList) {
        assertThat(immunizationList).hasSize(1);
        assertThat(immunization.getId()).isEqualTo(OBSERVATION_ROOT_ID);
        assertThat(immunization.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThatIdentifierIsValid(immunization.getIdentifierFirstRep(), immunization.getId());
        assertThat(immunization.getStatus()).isEqualTo(Immunization.ImmunizationStatus.COMPLETED);
        assertTrue(immunization.getPrimarySource());
        assertThat(immunization.getDateElement().getValue()).isEqualTo(
            DateFormatUtil.parseToDateTimeType("20100118114100000").getValue());
        assertThat(immunization.getNote().get(0).getText()).isEqualTo(OBSERVATION_TEXT);
        assertThat(immunization.getNote().get(1).getText()).isEqualTo("End Date: 2010-01-18T11:41:00+00:00");
        assertThat(immunization.getPatient().getResource().getIdElement().getValue()).isEqualTo(PATIENT_ID);
        assertThat(immunization.getEncounter().getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_ID);
        assertThat(immunization.getPractitioner().get(0).getActor().getReference()).isEqualTo("Practitioner/9C1610C2-5E48-4ED5-882B"
            + "-5A4A172AFA35");
    }

    private void assertMissingData(Immunization immunization, List<Immunization> immunizationList) {
        assertEquals(1, immunizationList.size());
        assertThat(immunization.getId()).isEqualTo(OBSERVATION_ROOT_ID);
        assertThat(immunization.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThatIdentifierIsValid(immunization.getIdentifierFirstRep(), immunization.getId());
        assertThat(immunization.getStatus()).isEqualTo(Immunization.ImmunizationStatus.COMPLETED);
        assertTrue(immunization.getPrimarySource());
        assertThat(immunization.getDate()).isNull();
        assertThat(immunization.getNote()).isEmpty();
        assertThat(immunization.getPatient().getResource().getIdElement().getValue()).isEqualTo(PATIENT_ID);
        assertThat(immunization.getEncounter().getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_ID);
    }

    private void assertThatIdentifierIsValid(Identifier identifier, String id) {
        assertThat(identifier.getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(identifier.getValue()).isEqualTo(id);
    }

    private void setUpCodeableConceptMock() {
        var codeableConcept = new CodeableConcept();
        var coding = new Coding();
        coding.setDisplay(CODING_DISPLAY);
        codeableConcept.addCoding(coding);
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);
        when(immunizationChecker.isImmunization(any())).thenReturn(true);
    }

    private Patient getPatient() {
        var patient = new Patient();
        patient.setId(PATIENT_ID);
        return patient;
    }

    private List<Encounter> getEncounterList() {
        var encounter = new Encounter();
        encounter.setId(ENCOUNTER_ID);
        return List.of(encounter);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
