package uk.nhs.adaptors.pss.translator.mapper;

import lombok.SneakyThrows;

import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestStatus;
import org.junit.jupiter.api.Test;

import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.common.testutil.XmlUnmarshallUtil.unmarshallFile;

public class ProcedureRequestMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/ProcedureRequest/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProcedureRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";

    private final ProcedureRequestMapper procedureRequestMapper = new ProcedureRequestMapper();

    @Test
    public void mapProcedureRequestWithValidData() {
        var ehrExtract = unmarshallCodeElement("full_valid_data_example.xml");
        var planStatement = getPlanStatement(ehrExtract);

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getNoteFirstRep().getText()).isEqualTo(planStatement.getText());
        assertThat(procedureRequest.getOccurrenceDateTimeType().getValue()).isEqualTo(
            DateFormatUtil.parse(planStatement.getEffectiveTime().getCenter().getValue()).getValue());
        assertThat(procedureRequest.getAuthoredOn()).isEqualTo(
            DateFormatUtil.parse(planStatement.getAvailabilityTime().getValue()).getValue());
        assertThat(procedureRequest.getReasonCodeFirstRep().getCodingFirstRep().getDisplay()).isEqualTo(
            planStatement.getCode().getDisplayName());
        assertThat(procedureRequest.getRequester().getAgent().getReference()).isEqualTo("8D1610C2-5E48-4ED5-882B-5A4A172AFA35");
    }

    @Test
    public void mapProcedureRequestWithNoOptionalFields() {
        var ehrExtract = unmarshallCodeElement("no_optional_data_example.xml");
        var planStatement = getPlanStatement(ehrExtract);

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getOccurrence()).isNull();
        assertThat(procedureRequest.getAuthoredOn()).isNull();
        assertThat(procedureRequest.getNoteFirstRep()).isNull();
    }

    @Test
    public void mapProcedureRequestWithPrfParticipant() {
        var ehrExtract = unmarshallCodeElement("prf_participant_example.xml");
        var planStatement = getPlanStatement(ehrExtract);

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getReasonCodeFirstRep().getCodingFirstRep().getDisplay()).isEqualTo(
            planStatement.getCode().getDisplayName());
        assertThat(procedureRequest.getRequester().getAgent().getReference()).isEqualTo("9C1610C2-5E48-4ED5-882B-5A4A172AFA35");
    }

    @Test
    public void mapProcedureRequestWithParticipant2() {
        var ehrExtract = unmarshallCodeElement("participant2_example.xml");
        var planStatement = getPlanStatement(ehrExtract);

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getReasonCodeFirstRep().getCodingFirstRep().getDisplay()).isEqualTo(
            planStatement.getCode().getDisplayName());
        assertThat(procedureRequest.getRequester().getAgent().getReference()).isEqualTo("2D70F602-6BB1-47E0-B2EC-39912A59787D");
    }

    @Test
    public void mapProcedureRequestWithEhrCompositionAvailabilityTime() {
        var ehrExtract = unmarshallCodeElement("ehr_composition_availability_time_example.xml");
        var ehrComposition = getEhrComposition(ehrExtract);
        var planStatement = getPlanStatement(ehrExtract);

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getAuthoredOn()).isEqualTo(
            DateFormatUtil.parse(ehrComposition.getAvailabilityTime().getValue()).getValue());
        assertThat(procedureRequest.getReasonCodeFirstRep().getCodingFirstRep().getDisplay()).isEqualTo(
            planStatement.getCode().getDisplayName());
    }

    @Test
    public void mapProcedureRequestWithEhrExtractAvailabilityTime() {
        var ehrExtract = unmarshallCodeElement("ehr_extract_availability_time_example.xml");
        var planStatement = getPlanStatement(ehrExtract);

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement);

        assertFixedValues(planStatement, procedureRequest);
        assertThat(procedureRequest.getAuthoredOn()).isEqualTo(
            DateFormatUtil.parse(ehrExtract.getAvailabilityTime().getValue()).getValue());
        assertThat(procedureRequest.getReasonCodeFirstRep().getCodingFirstRep().getDisplay()).isEqualTo(
            planStatement.getCode().getDisplayName());
    }

    private void assertFixedValues(RCMRMT030101UK04PlanStatement planStatement, ProcedureRequest procedureRequest) {
        assertThat(procedureRequest.getId()).isEqualTo(planStatement.getId().getRoot());
        assertThat(procedureRequest.getIntent()).isEqualTo(ProcedureRequestIntent.PLAN);
        assertThat(procedureRequest.getStatus()).isEqualTo(ProcedureRequestStatus.ACTIVE);
        assertThat(procedureRequest.getIdentifierFirstRep().getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(procedureRequest.getIdentifierFirstRep().getValue()).isEqualTo(planStatement.getId().getRoot());
        assertThat(procedureRequest.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
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

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}

