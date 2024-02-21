package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllPlanStatements;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKPlanStatement;
import org.hl7.v3.TS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProcedureRequestMapper extends AbstractMapper<ProcedureRequest> {
    private static final String META_PROFILE = "ProcedureRequest-1";

    private final CodeableConceptMapper codeableConceptMapper;

    public List<ProcedureRequest> mapResources(RCMRMT030101UKEhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
                                               String practiseCode) {

        return mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
            extractAllPlanStatements(component)
                .filter(Objects::nonNull)
                .map(planStatement -> mapToProcedureRequest(composition, planStatement, patient, encounters, practiseCode)))
            .map(ProcedureRequest.class::cast)
            .toList();
    }

    public ProcedureRequest mapToProcedureRequest(
            RCMRMT030101UKEhrComposition ehrComposition,
            RCMRMT030101UKPlanStatement planStatement,
            Patient patient,
            List<Encounter> encounters,
            String practiseCode
    ) {

        var id = planStatement.getId().getRoot();
        var procedureRequest = new ProcedureRequest();
        procedureRequest
            .setStatus(ProcedureRequestStatus.ACTIVE)
            .setIntent(ProcedureRequestIntent.PLAN)
            .setAuthoredOnElement(getAuthoredOn(planStatement.getAvailabilityTime(), ehrComposition))
            .setOccurrence(getOccurrenceDate(planStatement.getEffectiveTime()))
            .setSubject(new Reference(patient))
            .setMeta(generateMeta(META_PROFILE))
            .setId(id);
        procedureRequest.getIdentifier().add(buildIdentifier(id, practiseCode));
        procedureRequest.getNote().add(getNote(planStatement.getText()));
        procedureRequest.setCode(codeableConceptMapper.mapToCodeableConcept(planStatement.getCode()));
        procedureRequest.getRequester().setAgent(ParticipantReferenceUtil.getParticipantReference(planStatement.getParticipant(),
            ehrComposition));

        setProcedureRequestContext(procedureRequest, ehrComposition, encounters);

        if (procedureRequest.getCode() == null) {
            return procedureRequest;
        }

        DegradedCodeableConcepts.addDegradedEntryIfRequired(
                procedureRequest.getCode(),
                DegradedCodeableConcepts.DEGRADED_PLAN);

        return procedureRequest;
    }

    private void setProcedureRequestContext(ProcedureRequest procedureRequest, RCMRMT030101UKEhrComposition ehrComposition,
                                            List<Encounter> encounters) {

        encounters
            .stream()
            .filter(encounter -> encounter.getId().equals(ehrComposition.getId().getRoot()))
            .findFirst()
            .map(Reference::new)
            .ifPresent(procedureRequest::setContext);
    }

    private Annotation getNote(String text) {
        if (StringUtils.isNotEmpty(text)) {
            var note = new Annotation();
            return note.setText(text);
        }

        return null;
    }

    private DateTimeType getAuthoredOn(TS availabilityTime, RCMRMT030101UKEhrComposition ehrComposition) {

        if (availabilityTime != null && availabilityTime.hasValue()) {
            return DateFormatUtil.parseToDateTimeType(availabilityTime.getValue());
        } else if (ehrComposition.getAvailabilityTime() != null && ehrComposition.getAvailabilityTime().hasValue()) {
            return DateFormatUtil.parseToDateTimeType(ehrComposition.getAvailabilityTime().getValue());
        } else if (ehrComposition.hasAuthor() && ehrComposition.getAuthor().hasTime() && ehrComposition.getAuthor().getTime().hasValue()) {
            return DateFormatUtil.parseToDateTimeType(ehrComposition.getAuthor().getTime().getValue());
        }

        return null;
    }

    private DateTimeType getOccurrenceDate(IVLTS effectiveTime) {
        if (effectiveTime != null && effectiveTime.hasCenter() && effectiveTime.getCenter().hasValue()) {
            return DateFormatUtil.parseToDateTimeType(effectiveTime.getCenter().getValue());
        }

        return null;
    }
}
