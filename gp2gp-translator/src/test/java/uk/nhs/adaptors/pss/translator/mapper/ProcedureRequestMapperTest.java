package uk.nhs.adaptors.pss.translator.mapper;

import static java.util.UUID.randomUUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestStatus;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@ExtendWith(MockitoExtension.class)
public class ProcedureRequestMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/ProcedureRequest/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProcedureRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String CODING_DISPLAY = "Ischaemic heart disease";
    private static final String ENCOUNTER_ID = "62A39454-299F-432E-993E-5A6232B4E099";
    private static final List<Encounter> ENCOUNTERS = getEncounterList();
    private static final Patient SUBJECT = createPatient();

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @InjectMocks
    private ProcedureRequestMapper procedureRequestMapper;

    private static Patient createPatient() {
        Patient patient = new Patient();
        patient.setId(randomUUID().toString());
        return patient;
    }

    private static List<Encounter> getEncounterList() {
        var encounter = new Encounter();
        encounter.setId(ENCOUNTER_ID);
        return List.of(encounter);
    }

    @Test
    public void mapProcedureRequestWithValidData() {
        var ehrExtract = unmarshallCodeElement("full_valid_data_example.xml");
        var planStatement = getPlanStatement(ehrExtract);
        setUpCodeableConceptMock();

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement, SUBJECT, ENCOUNTERS);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getNoteFirstRep().getText()).isEqualTo(planStatement.getText());
        assertThat(procedureRequest.getOccurrenceDateTimeType().getValue()).isEqualTo(
            DateFormatUtil.parseToDateTimeType(planStatement.getEffectiveTime().getCenter().getValue()).getValue());
        assertThat(procedureRequest.getAuthoredOn()).isEqualTo(
            DateFormatUtil.parseToDateTimeType(planStatement.getAvailabilityTime().getValue()).getValue());
        assertThat(procedureRequest.getReasonCodeFirstRep().getCodingFirstRep().getDisplay()).isEqualTo(
            planStatement.getCode().getDisplayName());
        assertThat(procedureRequest.getRequester().getAgent().getReference())
            .isEqualTo("Practitioner/8D1610C2-5E48-4ED5-882B-5A4A172AFA35");
        assertThat(procedureRequest.getContext().getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_ID);
    }

    @Test
    public void mapProcedureRequestWithNoOptionalFields() {
        var ehrExtract = unmarshallCodeElement("no_optional_data_example.xml");
        var planStatement = getPlanStatement(ehrExtract);

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement, SUBJECT, ENCOUNTERS);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getOccurrence()).isNull();
        assertThat(procedureRequest.getAuthoredOn()).isNull();
        assertThat(procedureRequest.getNoteFirstRep()).isNull();
    }

    @Test
    public void mapProcedureRequestWithNoReferencedEncounter() {
        var ehrExtract = unmarshallCodeElement("no_referenced_encounter_example.xml");
        var planStatement = getPlanStatement(ehrExtract);

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement, SUBJECT, ENCOUNTERS);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getContext().getResource()).isNull();
        assertThat(procedureRequest.getOccurrence()).isNull();
        assertThat(procedureRequest.getAuthoredOn()).isNull();
        assertThat(procedureRequest.getNoteFirstRep()).isNull();
    }

    @Test
    public void mapProcedureRequestWithPrfParticipant() {
        var ehrExtract = unmarshallCodeElement("prf_participant_example.xml");
        var planStatement = getPlanStatement(ehrExtract);
        setUpCodeableConceptMock();

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement, SUBJECT, ENCOUNTERS);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getReasonCodeFirstRep().getCodingFirstRep().getDisplay()).isEqualTo(
            planStatement.getCode().getDisplayName());
        assertThat(procedureRequest.getRequester().getAgent().getReference())
            .isEqualTo("Practitioner/9C1610C2-5E48-4ED5-882B-5A4A172AFA35");
        assertThat(procedureRequest.getContext().getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_ID);
    }

    @Test
    public void mapProcedureRequestWithParticipant2() {
        var ehrExtract = unmarshallCodeElement("participant2_example.xml");
        var planStatement = getPlanStatement(ehrExtract);
        setUpCodeableConceptMock();

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement, SUBJECT, ENCOUNTERS);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getReasonCodeFirstRep().getCodingFirstRep().getDisplay()).isEqualTo(
            planStatement.getCode().getDisplayName());
        assertThat(procedureRequest.getRequester().getAgent().getReference())
            .isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(procedureRequest.getContext().getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_ID);
    }

    @Test
    public void mapProcedureRequestWithEhrCompositionAvailabilityTime() {
        var ehrExtract = unmarshallCodeElement("ehr_composition_availability_time_example.xml");
        var ehrComposition = getEhrComposition(ehrExtract);
        var planStatement = getPlanStatement(ehrExtract);
        setUpCodeableConceptMock();

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement, SUBJECT, ENCOUNTERS);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getAuthoredOn()).isEqualTo(
            DateFormatUtil.parseToDateTimeType(ehrComposition.getAvailabilityTime().getValue()).getValue());
        assertThat(procedureRequest.getReasonCodeFirstRep().getCodingFirstRep().getDisplay()).isEqualTo(
            planStatement.getCode().getDisplayName());
        assertThat(procedureRequest.getContext().getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_ID);
    }

    @Test
    public void mapProcedureRequestWithEhrExtractAvailabilityTime() {
        var ehrExtract = unmarshallCodeElement("ehr_extract_availability_time_example.xml");
        var planStatement = getPlanStatement(ehrExtract);
        setUpCodeableConceptMock();

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement, SUBJECT, ENCOUNTERS);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getAuthoredOn()).isEqualTo(
            DateFormatUtil.parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue()).getValue());
        assertThat(procedureRequest.getReasonCodeFirstRep().getCodingFirstRep().getDisplay()).isEqualTo(
            planStatement.getCode().getDisplayName());
        assertThat(procedureRequest.getContext().getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_ID);
    }

    private void assertFixedValues(RCMRMT030101UK04PlanStatement planStatement, ProcedureRequest procedureRequest) {
        assertThat(procedureRequest.getId()).isEqualTo(planStatement.getId().getRoot());
        assertThat(procedureRequest.getIntent()).isEqualTo(ProcedureRequestIntent.PLAN);
        assertThat(procedureRequest.getStatus()).isEqualTo(ProcedureRequestStatus.ACTIVE);
        assertThat(procedureRequest.getIdentifierFirstRep().getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(procedureRequest.getIdentifierFirstRep().getValue()).isEqualTo(planStatement.getId().getRoot());
        assertThat(procedureRequest.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(procedureRequest.getSubject().getResource().getIdElement().getValue()).isEqualTo(SUBJECT.getId());
    }

    private RCMRMT030101UK04PlanStatement getPlanStatement(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().get(0)
            .getEhrFolder().getComponent().get(0)
            .getEhrComposition().getComponent().get(0)
            .getPlanStatement();
    }

    private RCMRMT030101UK04EhrComposition getEhrComposition(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().get(0)
            .getEhrFolder().getComponent().get(0)
            .getEhrComposition();
    }

    private void setUpCodeableConceptMock() {
        var codeableConcept = new CodeableConcept();
        var coding = new Coding();
        coding.setDisplay(CODING_DISPLAY);
        codeableConcept.addCoding(coding);
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
