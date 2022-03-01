package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.II;
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.hl7.v3.TS;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

@Service
@AllArgsConstructor
public class ProcedureRequestMapper {
    private static final String META_PROFILE = "ProcedureRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";

    private CodeableConceptMapper codeableConceptMapper;

    public ProcedureRequest mapToProcedureRequest(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04PlanStatement planStatement,
        Patient patient, List<Encounter> encounters) {

        /**
         * TODO: Known future implementations to this mapper
         * - requester: fallback to a default 'Unknown User' Practitioner if none are present in requester (NIAD-2026)
         * - concatenate source practice org id to identifier URL (NIAD-2021)
         */

        var id = planStatement.getId().getRoot();
        var procedureRequest = new ProcedureRequest();
        procedureRequest
            .setStatus(ProcedureRequestStatus.ACTIVE)
            .setIntent(ProcedureRequestIntent.PLAN)
            .setAuthoredOnElement(getAuthoredOn(planStatement.getAvailabilityTime(), ehrExtract, planStatement.getId()))
            .setOccurrence(getOccurrenceDate(planStatement.getEffectiveTime()))
            .setSubject(new Reference(patient))
            .setMeta(generateMeta(META_PROFILE))
            .setId(id);
        procedureRequest.getIdentifier().add(getIdentifier(id));
        procedureRequest.getNote().add(getNote(planStatement.getText()));
        procedureRequest.getReasonCode().add(codeableConceptMapper.mapToCodeableConcept(planStatement.getCode()));
        procedureRequest.getRequester().setAgent(ParticipantReferenceUtil.getParticipantReference(planStatement.getParticipant(),
            EhrResourceExtractorUtil.extractEhrCompositionForPlanStatement(ehrExtract, planStatement.getId())));

        setProcedureRequestContext(procedureRequest, ehrExtract, planStatement.getId(), encounters);

        return procedureRequest;
    }

    private void setProcedureRequestContext(ProcedureRequest procedureRequest, RCMRMT030101UK04EhrExtract ehrExtract,
        II planStatementId, List<Encounter> encounters) {
        var ehrComposition =
            EhrResourceExtractorUtil.extractEhrCompositionForPlanStatement(ehrExtract, planStatementId);

        encounters
            .stream()
            .filter(encounter -> encounter.getId().equals(ehrComposition.getId().getRoot()))
            .findFirst()
            .ifPresent(encounter -> procedureRequest.setContext(new Reference(encounter)));
    }

    private Identifier getIdentifier(String id) {
        Identifier identifier = new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
        return identifier;
    }

    private Annotation getNote(String text) {
        if (StringUtils.isNotEmpty(text)) {
            var note = new Annotation();
            return note.setText(text);
        }

        return null;
    }

    private DateTimeType getAuthoredOn(TS availabilityTime, RCMRMT030101UK04EhrExtract ehrExtract, II planStatementID) {
        if (availabilityTime != null) {
            return DateFormatUtil.parseToDateTimeType(availabilityTime.getValue());
        } else {
            var ehrComposition = EhrResourceExtractorUtil.extractEhrCompositionForPlanStatement(ehrExtract, planStatementID);
            if (ehrComposition.getAvailabilityTime() != null) {
                return DateFormatUtil.parseToDateTimeType(ehrComposition.getAvailabilityTime().getValue());
            } else if (ehrExtract.getAvailabilityTime() != null) {
                return DateFormatUtil.parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue());
            }
        }

        return null;
    }

    private DateTimeType getOccurrenceDate(IVLTS effectiveTime) {
        if (effectiveTime != null && effectiveTime.getCenter() != null) {
            return DateFormatUtil.parseToDateTimeType(effectiveTime.getCenter().getValue());
        }

        return null;
    }
}
