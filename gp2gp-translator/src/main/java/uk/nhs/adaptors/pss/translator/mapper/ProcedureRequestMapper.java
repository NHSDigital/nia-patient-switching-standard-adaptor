package uk.nhs.adaptors.pss.translator.mapper;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestStatus;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.II;
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.hl7.v3.TS;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

import java.util.Date;

@AllArgsConstructor
public class ProcedureRequestMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProcedureRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";

    private CodeableConceptMapper codeableConceptMapper;

    public ProcedureRequest mapToProcedureRequest(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04PlanStatement planStatement) {

        /**
         * TODO: Known future implementations to this mapper
         * - subject: references a global patient resource for the transaction (NIAD-2024)
         * - context: references an encounter resource if it has been generated from the ehrComposition (NIAD-2025)
         * - requester: fallback to a default 'Unknown User' Practitioner if none are present in requester (NIAD-2026)
         * - concatenate source practice org id to identifier URL (NIAD-2021)
         */

        var id = planStatement.getId().getRoot();
        var identifier = getIdentifier(id);
        var note = getNote(planStatement.getText());
        var reasonCode = codeableConceptMapper.mapToCodeableConcept(planStatement.getCode());
        var authoredOn = getAuthoredOn(planStatement.getAvailabilityTime(), ehrExtract, planStatement.getId());
        var occurrence = getOccurrenceDate(planStatement.getEffectiveTime());
        var agentReference = ParticipantReferenceUtil.getParticipantReference(planStatement.getParticipant(),
            EhrResourceExtractorUtil.extractEhrCompositionForPlanStatement(ehrExtract, planStatement.getId()));

        return createProcedureRequest(id, identifier, note, reasonCode, authoredOn, occurrence, agentReference);
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

    private Date getAuthoredOn(TS availabilityTime, RCMRMT030101UK04EhrExtract ehrExtract, II planStatementID) {
        if (availabilityTime != null) {
            return DateFormatUtil.parse(availabilityTime.getValue()).getValue();
        } else {
            var ehrComposition = EhrResourceExtractorUtil.extractEhrCompositionForPlanStatement(ehrExtract, planStatementID);
            if (ehrComposition.getAvailabilityTime() != null) {
                return DateFormatUtil.parse(ehrComposition.getAvailabilityTime().getValue()).getValue();
            } else if (ehrExtract.getAvailabilityTime() != null) {
                return DateFormatUtil.parse(ehrExtract.getAvailabilityTime().getValue()).getValue();
            }
        }

        return null;
    }

    private DateTimeType getOccurrenceDate(IVLTS effectiveTime) {
        if (effectiveTime != null && effectiveTime.getCenter() != null) {
            return DateFormatUtil.parse(effectiveTime.getCenter().getValue());
        }

        return null;
    }

    private ProcedureRequest createProcedureRequest(String id, Identifier identifier, Annotation note, CodeableConcept reasonCode,
        Date authoredOn, DateTimeType occurrence, Reference agentReference) {
        var procedureRequest = new ProcedureRequest();
        procedureRequest
            .setStatus(ProcedureRequestStatus.ACTIVE)
            .setIntent(ProcedureRequestIntent.PLAN)
            .setAuthoredOn(authoredOn)
            .setOccurrence(occurrence)
            .setId(id);
        procedureRequest.getMeta().getProfile().add(new UriType(META_PROFILE));
        procedureRequest.getIdentifier().add(identifier);
        procedureRequest.getNote().add(note);
        procedureRequest.getReasonCode().add(reasonCode);
        procedureRequest.getRequester().setAgent(agentReference);

        return procedureRequest;
    }
}
