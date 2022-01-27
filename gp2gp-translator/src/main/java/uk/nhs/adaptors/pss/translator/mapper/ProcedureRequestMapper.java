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
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.hl7.v3.TS;

public class ProcedureRequestMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProcedureRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";

    public ProcedureRequest mapToProcedureRequest(RCMRMT030101UK04PlanStatement planStatement) {
        var id = planStatement.getId().getRoot();
        var identifier = getIdentifier(id);
        var note = getNote(planStatement.getText());
        var reasonCode = new CodeableConceptMapper().mapToCodeableConcept(planStatement.getCode());
        var authoredOn = getAuthoredOn(planStatement.getAvailabilityTime());
        var occurrence = getOccurenceDate(planStatement.getEffectiveTime());

        /**
         * TODO: Implement future referencing
         * - subject: references a global patient resource for the transaction
         * - context: references an encounter resource if it has been generated from the ehrComposition
         */

        return createProcedureRequest(id, identifier, note, reasonCode, authoredOn, occurrence);
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
            return note.setText(text.trim()); // TODO: check how to deal with line breaks
        }

        return null;
    }

    private String getAuthoredOn(TS availabilityTime) {
        if (availabilityTime != null) {
            return availabilityTime.getValue();
        }

        return null;
    }

    private String getOccurenceDate(IVLTS effectiveTime) {
        if (effectiveTime != null && effectiveTime.getCenter() != null) {
            return effectiveTime.getCenter().getValue();
        }

        return null;
    }

    private ProcedureRequest createProcedureRequest(String id, Identifier identifier, Annotation note, CodeableConcept reasonCode,
                                                    String authoredOn, String occurrence) {
        var procedureRequest = new ProcedureRequest();
        procedureRequest
                .setStatus(ProcedureRequestStatus.ACTIVE)
                .setIntent(ProcedureRequestIntent.PLAN)
//                .setAuthoredOn(authoredOn) needs DateUtil story for String -> Date
//                .setOccurrence(occurrence) needs formatted appropriately with same story ^
                .setId(id);
        procedureRequest.getMeta().getProfile().add(new UriType(META_PROFILE));
        procedureRequest.getIdentifier().add(identifier);
        procedureRequest.getNote().add(note);
        procedureRequest.getReasonCode().add(reasonCode);

        return procedureRequest;
    }
}
