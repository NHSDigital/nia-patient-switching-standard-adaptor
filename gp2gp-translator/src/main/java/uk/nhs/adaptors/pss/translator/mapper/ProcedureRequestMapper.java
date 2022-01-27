package uk.nhs.adaptors.pss.translator.mapper;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestStatus;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CV;
import org.hl7.v3.ED;
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.hl7.v3.TS;

public class ProcedureRequestMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProcedureRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String PRIORITY_PREFIX = "Priority: ";
    private static final String ACTION_DATE_PREFIX = "Action Date: ";

    public ProcedureRequest mapToProcedureRequest(RCMRMT030101UK04PlanStatement planStatement) {
        var id = planStatement.getId().getRoot();
        var identifier = getIdentifier(id);
        var note = getNote(planStatement);
        var reasonCode = new CodeableConceptMapper().mapToCodeableConcept(planStatement.getCode());
        var authoredOn = getAuthoredOn(planStatement.getAvailabilityTime());

        /**
         * TODO: Implement future referencing
         * - subject: references a global patient resource for the transaction
         * - context: references an encounter resource if it has been generated from the ehrComposition
         */

        return createProcedureRequest(id, identifier, note, reasonCode, authoredOn);
    }

    private Identifier getIdentifier(String id) {
        Identifier identifier = new Identifier()
                .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL
                .setValue(id);
        return identifier;
    }

    private Annotation getNote(RCMRMT030101UK04PlanStatement planStatement) {
        var priority = getPriority(planStatement.getPriorityCode());
        var actionDate = getActionDate(planStatement.getEffectiveTime());
        var text = getText(planStatement.getText()); // TODO: Add value to ED class

        var note = new Annotation();
        note.setText(priority + actionDate + text);

        return note;
    }

    private String getPriority(CV priorityCode) {
        if (priorityCode != null) {
            var originalText = priorityCode.getOriginalText();
            var displayName = priorityCode.getDisplayName();

            if (StringUtils.isNotEmpty(originalText)) {
                return PRIORITY_PREFIX + originalText + StringUtils.LF;
            } else if (StringUtils.isNotEmpty(displayName)) {
                return PRIORITY_PREFIX + displayName + StringUtils.LF;
            }
        }
        return StringUtils.EMPTY;
    }

    private String getActionDate(IVLTS effectiveTime) {
        if (effectiveTime != null && effectiveTime.getCenter() != null) {
            return ACTION_DATE_PREFIX + effectiveTime.getCenter().getValue() + StringUtils.LF; // TODO convert to Instant
        }

        return StringUtils.EMPTY;
    }

    private String getText(ED text) {
        return StringUtils.EMPTY;
    }

    private String getAuthoredOn(TS availabilityTime) {
        if (availabilityTime != null) {
            return availabilityTime.getValue();
        }

        return null;
    }

    private ProcedureRequest createProcedureRequest(String id, Identifier identifier, Annotation note, CodeableConcept reasonCode,
                                                    String authoredOn) {
        var procedureRequest = new ProcedureRequest();

        procedureRequest
                .setStatus(ProcedureRequestStatus.UNKNOWN)
                .setIntent(ProcedureRequestIntent.ORDER)
//                .setAuthoredOn(authoredOn) needs DateUtil story for String -> Date
                .setId(id);
        procedureRequest.getMeta().getProfile().add(new UriType(META_PROFILE));
        procedureRequest.getIdentifier().add(identifier);
        procedureRequest.getNote().add(note);
        procedureRequest.getReasonCode().add(reasonCode);

        return procedureRequest;
    }
}
