package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
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
import static uk.nhs.adaptors.common.util.CodeableConceptUtils.createCodeableConcept;

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
        assertNull(immunization.getEncounter().getReference());
    }

    @Test
    public void mapObservationToImmunizationWhenEhrCompositionWithParticipantAndAuthor() {
        var ehrExtract = unmarshallEhrExtract("immunization_with_ehr_composition_with_author_and_participant.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertEquals("Practitioner/E7E7B550-09EF-BE85-C20F-34598014166C",
                                    immunization.getPractitioner().get(0).getActor().getReference());
        assertEquals("EP",
                                    immunization.getPractitioner().get(0).getRole().getText());
        assertEquals("Practitioner/9F2ABD26-1682-FDFE-1E88-19673307C67A",
                                    immunization.getPractitioner().get(1).getActor().getReference());
        assertEquals("AP",
                                    immunization.getPractitioner().get(1).getRole().getText());

    }

    @Test
    public void mapObservationToImmunizationWithMultipleObservationStatements() {
        var ehrExtract = unmarshallEhrExtract("full_valid_immunization_with_multiple_observation_statements.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertThat(immunizationList).hasSize(THREE);
        assertEquals(OBSERVATION_ROOT_ID, immunization.getId());
        assertEquals(META_PROFILE, immunization.getMeta().getProfile().get(0).getValue());
        assertThatIdentifierIsValid(immunization.getIdentifierFirstRep(), immunization.getId());
    }

    @Test
    public void mapObservationToImmunizationAndCheckCodingDisplayAndVaccineProcedureUrl() {
        final String VACCINE_PROCEDURE_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-VaccinationProcedure-1";
        var ehrExtract = unmarshallEhrExtract("full_valid_immunization_with_multiple_observation_statements.xml");
        List<Immunization> immunizationList = immunizationMapper.mapResources(ehrExtract, getPatient(), getEncounterList(), PRACTISE_CODE);

        var immunization = (Immunization) immunizationList.get(0);
        assertEquals(VACCINE_PROCEDURE_URL, immunization.getExtension().get(0).getUrl());
        assertEquals(CODING_DISPLAY, ((CodeableConcept) immunization.getExtension().get(0).getValue()).getCoding().get(0).getDisplay());
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

        assertEquals(DateFormatUtil.parseToDateTimeType("20110118114100000").getValue(), immunization.getDateElement().getValue());
        assertEquals(OBSERVATION_TEXT, immunization.getNote().get(0).getText());
        assertEquals("End Date: 2010-01-18T11:41:00+00:00", immunization.getNote().get(1).getText());
    }

    private void assertImmunizationWithEffectiveTimeLow(Immunization immunization) {
        assertEquals(DateFormatUtil.parseToDateTimeType("20100118114100000").getValue(), immunization.getDateElement().getValue());
        assertEquals(OBSERVATION_TEXT, immunization.getNote().get(0).getText());
    }

    private void assertImmunizationWithHighEffectiveTime(Immunization immunization) {
        assertNull(immunization.getDate());
        assertEquals(OBSERVATION_TEXT, immunization.getNote().get(0).getText());
        assertEquals("End Date: 2010-01-18T11:41:00+00:00", immunization.getNote().get(1).getText());
    }

    private void assertImmunizationWithDefaultVaccineCode(Immunization immunization) {
        assertNotNull(immunization.getVaccineCode());
        assertEquals("UNK", immunization.getVaccineCode().getCoding().get(0).getCode());
        assertEquals("http://hl7.org/fhir/v3/NullFlavor", immunization.getVaccineCode().getCoding().get(0).getSystem());
    }

    private void assertImmunizationWithHighEffectiveTimeCenter(Immunization immunization) {
        assertEquals(DateFormatUtil.parseToDateTimeType("20100118114100000").getValue(), immunization.getDateElement().getValue());
        assertEquals(OBSERVATION_TEXT, immunization.getNote().get(0).getText());
    }

    private void assertFullValidData(Immunization immunization, List<Immunization> immunizationList) {
        assertThat(immunizationList).hasSize(1);
        assertEquals(OBSERVATION_ROOT_ID, immunization.getId());
        assertEquals(META_PROFILE, immunization.getMeta().getProfile().get(0).getValue());
        assertThatIdentifierIsValid(immunization.getIdentifierFirstRep(), immunization.getId());
        assertEquals(Immunization.ImmunizationStatus.COMPLETED, immunization.getStatus());
        assertTrue(immunization.getPrimarySource());
        assertEquals(DateFormatUtil.parseToDateTimeType("20100118114100000").getValue(), immunization.getDateElement().getValue());
        assertEquals(OBSERVATION_TEXT, immunization.getNote().get(0).getText());
        assertEquals("End Date: 2010-01-18T11:41:00+00:00", immunization.getNote().get(1).getText());
        assertEquals(PATIENT_ID, immunization.getPatient().getResource().getIdElement().getValue());
        assertEquals(ENCOUNTER_ID, immunization.getEncounter().getResource().getIdElement().getValue());
        assertEquals("Practitioner/9C1610C2-5E48-4ED5-882B-5A4A172AFA35", immunization.getPractitioner().get(0).getActor().getReference());
    }

    private void assertMissingData(Immunization immunization, List<Immunization> immunizationList) {
        assertEquals(1, immunizationList.size());
        assertEquals(OBSERVATION_ROOT_ID, immunization.getId());
        assertEquals(META_PROFILE, immunization.getMeta().getProfile().get(0).getValue());
        assertThatIdentifierIsValid(immunization.getIdentifierFirstRep(), immunization.getId());
        assertEquals(Immunization.ImmunizationStatus.COMPLETED, immunization.getStatus());
        assertTrue(immunization.getPrimarySource());
        assertNull(immunization.getDate());
        assertThat(immunization.getNote()).isEmpty();
        assertEquals(PATIENT_ID, immunization.getPatient().getResource().getIdElement().getValue());
        assertEquals(ENCOUNTER_ID, immunization.getEncounter().getResource().getIdElement().getValue());
    }

    private void assertThatIdentifierIsValid(Identifier identifier, String id) {
        assertEquals(IDENTIFIER_SYSTEM, identifier.getSystem());
        assertEquals(id, identifier.getValue());
    }

    private void setUpCodeableConceptMock() {

        var codeableConcept = createCodeableConcept(null, null, CODING_DISPLAY);
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
