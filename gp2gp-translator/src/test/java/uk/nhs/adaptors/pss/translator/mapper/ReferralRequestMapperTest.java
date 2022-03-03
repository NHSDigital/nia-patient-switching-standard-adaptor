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
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralRequestStatus;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04RequestStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class ReferralRequestMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/RequestStatement/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ReferralRequest-1";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/TESTPRACTISECODE";
    private static final String EXAMPLE_ID = "B4303C92-4D1C-11E3-A2DD-010000000161";
    private static final String PRACTITIONER_ID = "Practitioner/58341512-03F3-4C8E-B41C-A8FCA3886BBB";
    private static final String EHR_COMPOSITION_PRACTITIONER2_ID = "Practitioner/B1AF3701-4D1C-11E3-9E6B-010000001205";
    private static final String CODING_DISPLAY = "Reason Code 1";
    private static final String ENCOUNTER_ID = "72A39454-299F-432E-993E-5A6232B4E099";
    private static final List<Encounter> ENCOUNTERS = getEncounterList();
    private static final Patient SUBJECT = createPatient();

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @InjectMocks
    private ReferralRequestMapper referralRequestMapper;

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

    @SneakyThrows
    private RCMRMT030101UK04EhrComposition unmarshallEhrCompositionElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrComposition.class);
    }

    private RCMRMT030101UK04RequestStatement getRequestStatement(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition.getComponent().get(0).getRequestStatement();
    }

    private void assertFixedValues(ReferralRequest referralRequest) {
        assertThat(referralRequest.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(referralRequest.getIdentifier().get(0).getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(referralRequest.getIdentifier().get(0).getValue()).isEqualTo(EXAMPLE_ID);
        assertThat(referralRequest.getIntent()).isEqualTo(ReferralRequest.ReferralCategory.ORDER);
        assertThat(referralRequest.getStatus()).isEqualTo(ReferralRequestStatus.UNKNOWN);
        assertThat(referralRequest.getSubject().getResource().getIdElement().getValue()).isEqualTo(SUBJECT.getId());
    }

    @Test
    public void mapReferralRequestWithValidData() {
        var ehrComposition = unmarshallEhrCompositionElement("full_valid_data_example.xml");
        var requestStatement = getRequestStatement(ehrComposition);

        var codeableConcept = new CodeableConcept();
        var coding = new Coding();
        coding.setDisplay(CODING_DISPLAY);
        codeableConcept.addCoding(coding);
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);

        ReferralRequest referralRequest = referralRequestMapper.mapToReferralRequest(ehrComposition, requestStatement, SUBJECT,
            ENCOUNTERS, PRACTISE_CODE);

        assertFixedValues(referralRequest);
        assertThat(referralRequest.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(referralRequest.getNote().get(0).getText()).isEqualTo("Priority: Routine");
        assertThat(referralRequest.getNote().get(1).getText()).isEqualTo("Action Date: 2005-04-06");
        assertThat(referralRequest.getNote().get(2).getText()).isEqualTo("Test request statement text\nNew line");
        assertThat(referralRequest.getAuthoredOn()).isEqualTo("2010-01-01T12:30:00+00:00");
        assertThat(referralRequest.getRequester().getAgent().getReference()).isEqualTo(PRACTITIONER_ID);
        assertThat(referralRequest.getRecipient().get(0).getReference()).isEqualTo("Practitioner/B8CA3710-4D1C-11E3-9E6B-010000001205");
        assertThat(referralRequest.getReasonCodeFirstRep().getCodingFirstRep().getDisplay()).isEqualTo("Reason Code 1");
        assertThat(referralRequest.getContext().getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_ID);
    }

    @Test
    public void mapReferralRequestWithNoOptionalData() {
        var ehrComposition = unmarshallEhrCompositionElement("no_optional_data_example.xml");
        var requestStatement = getRequestStatement(ehrComposition);

        ReferralRequest referralRequest = referralRequestMapper.mapToReferralRequest(ehrComposition, requestStatement, SUBJECT,
            ENCOUNTERS, PRACTISE_CODE);

        assertFixedValues(referralRequest);
        assertThat(referralRequest.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(referralRequest.getNote().size()).isZero();
        assertThat(referralRequest.getAuthoredOn()).isNull();
        assertThat(referralRequest.getRequester().getAgent().getReference()).isNull();
        assertThat(referralRequest.getRecipient().size()).isZero();
        assertThat(referralRequest.getReasonCode().size()).isZero();
    }

    @Test
    public void mapReferralRequestWithNoReferencedEncounter() {
        var ehrComposition = unmarshallEhrCompositionElement("no_referenced_encounter_example.xml");
        var requestStatement = getRequestStatement(ehrComposition);

        ReferralRequest referralRequest = referralRequestMapper.mapToReferralRequest(ehrComposition, requestStatement, SUBJECT,
            ENCOUNTERS, PRACTISE_CODE);

        assertThat(referralRequest.getContext().getResource()).isNull();
    }

    @Test
    public void mapReferralRequestWithRequesterPrfParticipant() {
        var ehrComposition = unmarshallEhrCompositionElement("requester_prf_participant_example.xml");
        var requestStatement = getRequestStatement(ehrComposition);

        ReferralRequest referralRequest = referralRequestMapper.mapToReferralRequest(ehrComposition, requestStatement, SUBJECT,
            ENCOUNTERS, PRACTISE_CODE);

        assertFixedValues(referralRequest);
        assertThat(referralRequest.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(referralRequest.getNote().size()).isZero();
        assertThat(referralRequest.getAuthoredOn()).isNull();
        assertThat(referralRequest.getRequester().getAgent().getReference()).isEqualTo(PRACTITIONER_ID);
        assertThat(referralRequest.getRecipient().size()).isZero();
        assertThat(referralRequest.getReasonCode().size()).isZero();
    }

    @Test
    public void mapReferralRequestWithRequesterEhrCompositionParticipant2() {
        var ehrComposition = unmarshallEhrCompositionElement("requester_ehr_composition_participant2_example.xml");
        var requestStatement = getRequestStatement(ehrComposition);

        ReferralRequest referralRequest = referralRequestMapper.mapToReferralRequest(ehrComposition, requestStatement, SUBJECT,
            ENCOUNTERS, PRACTISE_CODE);

        assertFixedValues(referralRequest);
        assertThat(referralRequest.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(referralRequest.getNote().size()).isZero();
        assertThat(referralRequest.getAuthoredOn()).isNull();
        assertThat(referralRequest.getRequester().getAgent().getReference()).isEqualTo(EHR_COMPOSITION_PRACTITIONER2_ID);
        assertThat(referralRequest.getRecipient().size()).isZero();
        assertThat(referralRequest.getReasonCode().size()).isZero();
    }

    @Test
    public void mapReferralRequestWithRecipientResponsiblePartyNoValidTypeCode() {
        var ehrComposition = unmarshallEhrCompositionElement("recipient_responsible_party_no_valid_type_code_example.xml");
        var requestStatement = getRequestStatement(ehrComposition);

        ReferralRequest referralRequest = referralRequestMapper.mapToReferralRequest(ehrComposition, requestStatement, SUBJECT,
            ENCOUNTERS, PRACTISE_CODE);

        assertFixedValues(referralRequest);
        assertThat(referralRequest.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(referralRequest.getNote().size()).isZero();
        assertThat(referralRequest.getAuthoredOn()).isNull();
        assertThat(referralRequest.getRequester().getAgent().getReference()).isNull();
        assertThat(referralRequest.getRecipient().size()).isZero();
        assertThat(referralRequest.getReasonCode().size()).isZero();
    }

    @Test
    public void mapReferralRequestWithPriorityNoteUsingDisplayName() {
        var ehrComposition = unmarshallEhrCompositionElement("note_priority_using_display_name_example.xml");
        var requestStatement = getRequestStatement(ehrComposition);

        ReferralRequest referralRequest = referralRequestMapper.mapToReferralRequest(ehrComposition, requestStatement, SUBJECT,
            ENCOUNTERS, PRACTISE_CODE);

        assertFixedValues(referralRequest);
        assertThat(referralRequest.getId()).isEqualTo(EXAMPLE_ID);
        assertThat(referralRequest.getNote().get(0).getText()).isEqualTo("Priority: Normal");
        assertThat(referralRequest.getAuthoredOn()).isNull();
        assertThat(referralRequest.getRequester().getAgent().getReference()).isNull();
        assertThat(referralRequest.getRecipient().size()).isZero();
        assertThat(referralRequest.getReasonCode().size()).isZero();
    }
}
