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
import org.hl7.v3.RCMRMT030101UK04Participant;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.hl7.v3.TS;
import uk.nhs.adaptors.pss.translator.utils.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.utils.EhrResourceExtractorUtil;

import java.util.Date;
import java.util.List;

public class ProcedureRequestMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProcedureRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String PPRF_PERFORMER = "PPRF";
    private static final String PRF_PERFORMER = "PRF";

    public ProcedureRequest mapToProcedureRequest(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04PlanStatement planStatement) {

        /**
         * TODO: Implement future referencing
         * - subject: references a global patient resource for the transaction
         * - context: references an encounter resource if it has been generated from the ehrComposition
         */

        var id = planStatement.getId().getRoot();
        var identifier = getIdentifier(id);
        var note = getNote(planStatement.getText());
        var reasonCode = new CodeableConceptMapper().mapToCodeableConcept(planStatement.getCode());
        var authoredOn = getAuthoredOn(planStatement.getAvailabilityTime(), ehrExtract, planStatement.getId());
        var occurrence = getOccurrenceDate(planStatement.getEffectiveTime());
        var agentReference = getAgentReference(planStatement.getParticipant(),ehrExtract, planStatement.getId());

        return createProcedureRequest(id, identifier, note, reasonCode, authoredOn, occurrence, agentReference);
    }

    private Identifier getIdentifier(String id) {
        Identifier identifier = new Identifier()
                .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL
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
            } else if (ehrExtract.getAvailabilityTime() != null){
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

    private Reference getAgentReference(List<RCMRMT030101UK04Participant> participantList, RCMRMT030101UK04EhrExtract ehrExtract, II planStatementID) {
        Reference reference = new Reference();
        var nonNullFlavorParticipants = participantList.stream().filter(participant -> isNotNullFlavour(participant));

        // filter for PPRF Participants
        var pprfParticipants = nonNullFlavorParticipants.filter(participant -> hasPprfTypeCode(participant)).findFirst();
        if (pprfParticipants.isPresent()) {
            return reference.setReference(pprfParticipants.get().getAgentRef().getId().getRoot());
        }

        // filter for PRF Participants
        var prfParticipants = nonNullFlavorParticipants.filter(participant -> hasPrfTypeCode(participant)).findFirst();
        if (prfParticipants.isPresent()) {
            return reference.setReference(prfParticipants.get().getAgentRef().getId().getRoot());
        }

        // filter for EhrComposition Participant2
        var ehrComposition = EhrResourceExtractorUtil.extractEhrCompositionForPlanStatement(ehrExtract, planStatementID);

        // TODO: Return 'Unknown User' Practitioner
        return reference;
    }

    private boolean hasPprfTypeCode(RCMRMT030101UK04Participant participant) {
        return participant.getTypeCode().get(0).equals(PPRF_PERFORMER);
    }

    private boolean hasPrfTypeCode(RCMRMT030101UK04Participant participant) {
        return participant.getTypeCode().get(0).equals(PRF_PERFORMER);
    }

    private boolean isNotNullFlavour(RCMRMT030101UK04Participant participant) {
        return participant.getNullFlavor() != null;
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
