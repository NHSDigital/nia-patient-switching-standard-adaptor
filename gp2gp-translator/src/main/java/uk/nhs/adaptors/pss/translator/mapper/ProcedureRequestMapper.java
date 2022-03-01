package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProcedureRequestMapper {
    private static final String META_PROFILE = "ProcedureRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";

    private final CodeableConceptMapper codeableConceptMapper;

    public ProcedureRequest mapToProcedureRequest(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04PlanStatement planStatement,
        Patient patient) {

        /**
         * TODO: Known future implementations to this mapper
         * - context: references an encounter resource if it has been generated from the ehrComposition (NIAD-2025)
         * - concatenate source practice org id to identifier URL (NIAD-2021)
         */

        var id = planStatement.getId().getRoot();
        var note = getNote(planStatement.getText());
        var reasonCode = codeableConceptMapper.mapToCodeableConcept(planStatement.getCode());
        var authoredOn = getAuthoredOn(planStatement.getAvailabilityTime(), ehrExtract, planStatement.getId());
        var occurrence = getOccurrenceDate(planStatement.getEffectiveTime());
        var agentReference = ParticipantReferenceUtil.getParticipantReference(planStatement.getParticipant(),
            EhrResourceExtractorUtil.extractEhrCompositionForPlanStatement(ehrExtract, planStatement.getId()));

        return createProcedureRequest(id, note, reasonCode, authoredOn, occurrence, agentReference, new Reference(patient));
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

    private ProcedureRequest createProcedureRequest(String id, Annotation note, CodeableConcept reasonCode,
        DateTimeType authoredOn, DateTimeType occurrence, Reference agentReference, Reference patientReference) {
        var procedureRequest = new ProcedureRequest();
        procedureRequest
            .setStatus(ProcedureRequestStatus.ACTIVE)
            .setIntent(ProcedureRequestIntent.PLAN)
            .setAuthoredOnElement(authoredOn)
            .setOccurrence(occurrence)
            .setId(id);
        procedureRequest.setMeta(generateMeta(META_PROFILE));
        procedureRequest.getIdentifier().add(getIdentifier(id));
        procedureRequest.getNote().add(note);
        procedureRequest.getReasonCode().add(reasonCode);
        procedureRequest.getRequester().setAgent(agentReference);
        procedureRequest.setSubject(patientReference);

        return procedureRequest;
    }
}
