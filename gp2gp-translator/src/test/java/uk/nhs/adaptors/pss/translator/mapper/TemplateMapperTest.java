package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.ResourceReferenceUtil;

@ExtendWith(MockitoExtension.class)
public class TemplateMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Template/";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String COMPOUND_ID = "C8B1BEAF-FB71-45D1-89DA-298148C00CE1";
    private static final String PATIENT_ID = "9A5D5A78-1F63-434C-9637-1D7E7843341B";
    private static final String ENCOUNTER_ID = "TEST_ID_MATCHING_ENCOUNTER";
    private static final String CODING_DISPLAY_MOCK = "Test Display";
    private static final String QUESTIONNAIRE_SUFFIX = "-QRSP";
    private static final String QUESTIONNAIRE_META = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC"
        + "-QuestionnaireResponse-1";
    private static final String OBSERVATION_META = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1";
    private static final String IDENTIFIER = "https://PSSAdaptor/TESTPRACTISECODE";
    private static final int THREE = 3;

    private static final CodeableConcept CODEABLE_CONCEPT = new CodeableConcept()
        .addCoding(new Coding().setDisplay(CODING_DISPLAY_MOCK));

    private static final List<Encounter> ENCOUNTER_LIST = List.of(
        (Encounter) new Encounter().setId(ENCOUNTER_ID)
    );

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @Mock
    private ResourceReferenceUtil resourceReferenceUtil;

    @InjectMocks
    private TemplateMapper templateMapper;

    @Test
    public void testMapTemplateWithAllData() {
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(CODEABLE_CONCEPT);

        var ehrExtract = unmarshallEhrExtractElement("full_valid_template.xml");
        var mappedResources = templateMapper.mapResources(ehrExtract, getPatient(), ENCOUNTER_LIST, PRACTISE_CODE);

        assertThat(mappedResources.size()).isEqualTo(2);
        var questionnaireResponse = (QuestionnaireResponse) mappedResources.get(0);
        var parentObservation = (Observation) mappedResources.get(1);

        assertQuestionnaireResponse(questionnaireResponse, ENCOUNTER_ID, "original-text", "20100113151332");
        assertParentObservation(parentObservation, ENCOUNTER_ID, "20100113151332", "3707E1F0-9011-11EC-B1E5-0800200C9A66");

        verify(resourceReferenceUtil, atLeast(1)).extractChildReferencesFromTemplate(any(), anyList());
    }

    @Test
    public void testMapTemplateWithFallbackData() {
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(CODEABLE_CONCEPT);

        var ehrExtract = unmarshallEhrExtractElement("fallback_template.xml");
        var mappedResources = templateMapper.mapResources(ehrExtract, getPatient(), ENCOUNTER_LIST, PRACTISE_CODE);

        assertThat(mappedResources.size()).isEqualTo(2);
        var questionnaireResponse = (QuestionnaireResponse) mappedResources.get(0);
        var parentObservation = (Observation) mappedResources.get(1);

        assertQuestionnaireResponse(questionnaireResponse, null, "display-text", "20200101010101");
        assertParentObservation(parentObservation, null, "20200101010101", "9007E1F0-9011-11EC-B1E5-0800200C9A66");

        assertThat(questionnaireResponse.getItem().size()).isOne();
    }

    @Test
    public void testMapNestedTemplate() {
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(CODEABLE_CONCEPT);

        var ehrExtract = unmarshallEhrExtractElement("nested_template.xml");
        var mappedResources = templateMapper.mapResources(ehrExtract, getPatient(), ENCOUNTER_LIST, PRACTISE_CODE);

        assertThat(mappedResources.size()).isEqualTo(2);
        var questionnaireResponse = (QuestionnaireResponse) mappedResources.get(0);
        var parentObservation = (Observation) mappedResources.get(1);

        assertQuestionnaireResponse(questionnaireResponse, null, "display-text", "20200101010101");
        assertParentObservation(parentObservation, null, "20200101010101", "9007E1F0-9011-11EC-B1E5-0800200C9A66");

        verify(resourceReferenceUtil, atLeast(1)).extractChildReferencesFromTemplate(any(), anyList());
    }

    @Test
    public void testNoMappableTemplates() {
        var ehrExtract = unmarshallEhrExtractElement("no_mappable_template.xml");
        var mappedResources = templateMapper.mapResources(ehrExtract, getPatient(), ENCOUNTER_LIST, PRACTISE_CODE);

        assertThat(mappedResources.size()).isZero();
    }

    private void assertQuestionnaireResponse(QuestionnaireResponse questionnaireResponse, String encounter, String linkId,
        String authored) {
        assertThat(questionnaireResponse.getId()).isEqualTo(COMPOUND_ID + QUESTIONNAIRE_SUFFIX);
        assertThat(questionnaireResponse.getMeta().getProfile().get(0).getValue()).isEqualTo(QUESTIONNAIRE_META);
        assertThat(questionnaireResponse.getIdentifier().getSystem()).isEqualTo(IDENTIFIER);
        assertThat(questionnaireResponse.getIdentifier().getValue()).isEqualTo(COMPOUND_ID);
        assertThat(questionnaireResponse.getParentFirstRep().getResource().getIdElement().getValue()).isEqualTo(COMPOUND_ID);
        assertThat(questionnaireResponse.getStatus()).isEqualTo(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        assertThat(questionnaireResponse.getSubject().getResource().getIdElement().getValue()).isEqualTo(PATIENT_ID);
        assertThat(questionnaireResponse.getItemFirstRep().getLinkId()).isEqualTo(linkId);
        assertThat(questionnaireResponse.getAuthoredElement().asStringValue()).isEqualTo(
            DateFormatUtil.parseToDateTimeType(authored).asStringValue());

        if (encounter == null) {
            assertThat(questionnaireResponse.getContext().getResource()).isNull();
        } else {
            assertThat(questionnaireResponse.getContext().getResource().getIdElement().getValue()).isEqualTo(encounter);
        }
    }

    private void assertParentObservation(Observation parentObservation, String encounter, String issued, String performer) {
        assertThat(parentObservation.getId()).isEqualTo(COMPOUND_ID);
        assertThat(parentObservation.getMeta().getProfile().get(0).getValue()).isEqualTo(OBSERVATION_META);
        assertThat(parentObservation.getIdentifierFirstRep().getSystem()).isEqualTo(IDENTIFIER);
        assertThat(parentObservation.getIdentifierFirstRep().getValue()).isEqualTo(COMPOUND_ID);
        assertThat(parentObservation.getSubject().getResource().getIdElement().getValue()).isEqualTo(PATIENT_ID);
        assertThat(parentObservation.getStatus()).isEqualTo(Observation.ObservationStatus.FINAL);
        assertThat(parentObservation.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY_MOCK);
        assertThat(parentObservation.getIssuedElement().asStringValue()).isEqualTo(
            DateFormatUtil.parseToInstantType(issued).asStringValue());
        assertThat(parentObservation.getPerformerFirstRep().getReference()).contains(performer);

        if (encounter == null) {
            assertThat(parentObservation.getContext().getResource()).isNull();
        } else {
            assertThat(parentObservation.getContext().getResource().getIdElement().getValue()).isEqualTo(encounter);
        }
    }

    private Patient getPatient() {
        var patient = new Patient();
        patient.setId(PATIENT_ID);
        return patient;
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtractElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
