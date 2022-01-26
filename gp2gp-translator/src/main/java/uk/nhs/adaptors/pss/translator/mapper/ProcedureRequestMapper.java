package uk.nhs.adaptors.pss.translator.mapper;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestStatus;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CV;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;

public class ProcedureRequestMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProcedureRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String PRIORITY_PREFIX = "Priority: ";

    public ProcedureRequest mapToProcedureRequest(RCMRMT030101UK04PlanStatement planStatement) {
        var id = planStatement.getId().getRoot();
        var identifier = getIdentifier(id);
        var note = new Annotation();
        if (planStatement.getPriorityCode() != null) {
            note = getPriority(planStatement.getPriorityCode());
        }

        return createProcedureRequest(id, identifier);
    }

    private Annotation getPriority(CV priorityCode) {
        var note = new Annotation();
        var originalText = priorityCode.getOriginalText();
        var displayName = priorityCode.getDisplayName();

        if (StringUtils.isNotEmpty(originalText)) {
            return note.setText(PRIORITY_PREFIX + originalText);
        } else if (StringUtils.isNotEmpty(displayName)) {
            return note.setText(PRIORITY_PREFIX + displayName);
        }

        return null;
    }

    private Identifier getIdentifier(String id) {
        Identifier identifier = new Identifier()
                .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL
                .setValue(id);
        return identifier;
    }

    private ProcedureRequest createProcedureRequest(String id, Identifier identifier) {
        var procedureRequest = new ProcedureRequest();

        procedureRequest
                .setStatus(ProcedureRequestStatus.UNKNOWN)
                .setIntent(ProcedureRequestIntent.ORDER)
                .setId(id);
        procedureRequest.getMeta().getProfile().add(new UriType(META_PROFILE));
        procedureRequest.getIdentifier().add(identifier);

        return procedureRequest;
    }
}
