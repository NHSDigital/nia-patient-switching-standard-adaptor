package uk.nhs.adaptors.pss.translator.mapper;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04LinkSet;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class ConditionMapperTest {
    private static final String LINKSET_RESOURCES_BASE = "xml/Linkset/";
    private static final String PATIENT_ID = "9014e659-f87e-43a2-b8c3-36fb91cc4225";
    private static final String ENCOUNTER_ID = "77efedb3-e44e-43ff-9f81-23431a36df1b";
    private static final String ASSERTER_ID = "9b8d9627-3e92-44c2-84eb-f37e4906c47e";
    private static final String OBSERVATION_ID = "9f25604b-d2e8-402a-aae2-c78879c3e647";
    private static final String LINKSET_ID = "DCC26FC9-4D1C-11E3-A2DD-010000000161";
    private static final String CODING_DISPLAY = "THIS IS A TEST";
    private static final String PRACTISE_CODE = "TEST123";
    private static final Date EHR_EXTRACT_AVAILABILITY = new Date(2022, Calendar.JANUARY, 1);
    private static final DateTimeType EHR_EXTRACT_AVAILABILITY_DATETIME = new DateTimeType(EHR_EXTRACT_AVAILABILITY);

    private static final int RELATED_CLINICAL_CONTENT_COUNT = 3;
    private static final int NOTES_COUNT = 4;

    private static final String ACTUAL_PROBLEM_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-ActualProblem-1";
    private static final String PROBLEM_SIGNIFICANCE_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-ProblemSignificance-1";
    private static final String RELATED_CLINICAL_CONTENT_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-RelatedClinicalContent-1";

    @Mock
    private CodeableConceptMapper codeableConceptMapper;
    @Mock
    private DateTimeMapper dateTimeMapper;

    @InjectMocks
    private ConditionMapper conditionMapper;

    @BeforeEach
    public void setUp() {
        var codeableConcept = new CodeableConcept();
        var coding = new Coding();
        coding.setDisplay(CODING_DISPLAY);
        codeableConcept.addCoding(coding);

        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);
    }

    @Test
    public void When_MappingAllValidData_Expect_AppropriateOutput() {
        when(dateTimeMapper.mapDateTime(any())).thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);
        var linkset = unmarshallLinkset("linkset_valid.xml");
        var observationStatement = unmarshallObservationStatement("observationStatement_1.xml");
        var ehrComposition = new RCMRMT030101UK04EhrComposition();
        var component = new RCMRMT030101UK04Component4();

        component.setLinkSet(linkset);
        ehrComposition.setComponent(List.of(component));

        var params = ConditionMapper.ConditionMapperParameters.builder()
            .ehrComposition(ehrComposition)
            .linkedObservationStatement(Optional.of(observationStatement))
            .ehrExtractAvailabilityTime(EHR_EXTRACT_AVAILABILITY)
            .actualProblem(Optional.of(createObservationWithId()))
            .relatedClinicalContent(List.of(
                createObservationWithId(),
                createImmunizationWithId(),
                createReferralRequestWithId()
            ))
            .patientId(PATIENT_ID)
            .encounterId(Optional.of(ENCOUNTER_ID))
            .asserterId(ASSERTER_ID)
            .practiseCode(PRACTISE_CODE)
            .build();

        var result = conditionMapper.mapToCondition(params);

        assertGeneratedComponentsAreCorrect(result);
        assertThat(result.getId()).isEqualTo(LINKSET_ID);

        assertThat(result.getExtensionsByUrl(ACTUAL_PROBLEM_URL).size()).isEqualTo(1);
        assertThat(result.getExtensionsByUrl(PROBLEM_SIGNIFICANCE_URL).size()).isEqualTo(1);
        assertThat(result.getExtensionsByUrl(RELATED_CLINICAL_CONTENT_URL).size()).isEqualTo(RELATED_CLINICAL_CONTENT_COUNT);

        assertThat(result.getClinicalStatus().getDisplay()).isEqualTo("Active");
        assertThat(result.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY);

        assertThat(result.getSubject().getReferenceElement().getIdPart()).isEqualTo(PATIENT_ID);
        assertThat(result.getAsserter().getReferenceElement().getIdPart()).isEqualTo(ASSERTER_ID);
        assertThat(result.getContext().getReferenceElement().getIdPart()).isEqualTo(ENCOUNTER_ID);

        assertThat(result.getOnsetDateTimeType()).isEqualTo(EHR_EXTRACT_AVAILABILITY_DATETIME);
        assertThat(result.getAbatementDateTimeType()).isEqualTo(EHR_EXTRACT_AVAILABILITY_DATETIME);
        assertThat(result.getAssertedDate()).isEqualTo(EHR_EXTRACT_AVAILABILITY);

        assertThat(result.getNote().size()).isEqualTo(NOTES_COUNT);
    }

    @Test
    public void When_MappingMinimalValidData_Expect_AppropriateOutput() {
        when(dateTimeMapper.mapDateTime(any())).thenReturn(EHR_EXTRACT_AVAILABILITY_DATETIME);
        var linkset = unmarshallLinkset("linkset_valid.xml");
        var observationStatement = unmarshallObservationStatement("observationStatement_1.xml");
        var ehrComposition = new RCMRMT030101UK04EhrComposition();
        var component = new RCMRMT030101UK04Component4();

        component.setLinkSet(linkset);
        ehrComposition.setComponent(List.of(component));

        var params = ConditionMapper.ConditionMapperParameters.builder()
            .ehrComposition(ehrComposition)
            .linkedObservationStatement(Optional.empty())
            .ehrExtractAvailabilityTime(EHR_EXTRACT_AVAILABILITY)
            .actualProblem(Optional.empty())
            .relatedClinicalContent(List.of())
            .patientId(PATIENT_ID)
            .encounterId(Optional.empty())
            .asserterId(ASSERTER_ID)
            .practiseCode(PRACTISE_CODE)
            .build();

        var result = conditionMapper.mapToCondition(params);

        assertGeneratedComponentsAreCorrect(result);
        assertThat(result.getId()).isEqualTo(LINKSET_ID);

        assertThat(result.getExtensionsByUrl(ACTUAL_PROBLEM_URL).size()).isEqualTo(0);
        assertThat(result.getExtensionsByUrl(PROBLEM_SIGNIFICANCE_URL).size()).isEqualTo(1);
        assertThat(result.getExtensionsByUrl(RELATED_CLINICAL_CONTENT_URL).size()).isEqualTo(0);

        assertThat(result.getClinicalStatus().getDisplay()).isEqualTo("Active");
        assertThat(result.getCode().getCodingFirstRep().getDisplay()).isEqualTo(CODING_DISPLAY);

        assertThat(result.getSubject().getReferenceElement().getIdPart()).isEqualTo(PATIENT_ID);
        assertThat(result.getAsserter().getReferenceElement().getIdPart()).isEqualTo(ASSERTER_ID);
        assertThat(result.getContext().getReferenceElement()).isNotNull();

        assertThat(result.getNote().size()).isEqualTo(1);
    }

    @Test
    public void When_MappingLinksetWithNoDates_Expect_AppropriateOutput() {
        var linkset = unmarshallLinkset("linkset_no_dates.xml");
        var observationStatement = unmarshallObservationStatement("observationStatement_1.xml");
        var ehrComposition = new RCMRMT030101UK04EhrComposition();
        var component = new RCMRMT030101UK04Component4();

        component.setLinkSet(linkset);
        ehrComposition.setComponent(List.of(component));

        var params = ConditionMapper.ConditionMapperParameters.builder()
            .ehrComposition(ehrComposition)
            .linkedObservationStatement(Optional.empty())
            .ehrExtractAvailabilityTime(EHR_EXTRACT_AVAILABILITY)
            .actualProblem(Optional.empty())
            .relatedClinicalContent(List.of())
            .patientId(PATIENT_ID)
            .encounterId(Optional.empty())
            .asserterId(ASSERTER_ID)
            .practiseCode(PRACTISE_CODE)
            .build();

        var result = conditionMapper.mapToCondition(params);

        assertGeneratedComponentsAreCorrect(result);
        assertThat(result.getId()).isEqualTo(LINKSET_ID);

        assertThat(result.getClinicalStatus().getDisplay()).isEqualTo("Inactive");

        assertThat(result.getAbatementDateTimeType()).isNull();
        assertThat(result.getAssertedDate()).isEqualTo(EHR_EXTRACT_AVAILABILITY);
    }

    private void assertGeneratedComponentsAreCorrect(Condition condition) {
        assertThat(condition.getMeta().getProfile().get(0)).isNotNull();
        assertThat(condition.getIdentifierFirstRep().getValue()).isEqualTo(LINKSET_ID);
        assertThat(condition.getCategoryFirstRep().getCodingFirstRep().getDisplay()).isEqualTo("Problem List Item");
    }

    @SneakyThrows
    private RCMRMT030101UK04LinkSet unmarshallLinkset(String filename) {
        return unmarshallFile(getFile("classpath:" + LINKSET_RESOURCES_BASE + filename), RCMRMT030101UK04LinkSet.class);
    }

    @SneakyThrows
    private RCMRMT030101UK04ObservationStatement unmarshallObservationStatement(String filename) {
        return unmarshallFile(getFile("classpath:" + LINKSET_RESOURCES_BASE + filename), RCMRMT030101UK04ObservationStatement.class);
    }

    private Observation createObservationWithId() {
        var observation = new Observation();
        observation.setId(OBSERVATION_ID);
        return observation;
    }

    private Immunization createImmunizationWithId() {
        var immunization = new Immunization();
        immunization.setId(OBSERVATION_ID);
        return immunization;
    }

    private ReferralRequest createReferralRequestWithId() {
        var referralRequest = new ReferralRequest();
        referralRequest.setId(OBSERVATION_ID);
        return referralRequest;
    }
}
