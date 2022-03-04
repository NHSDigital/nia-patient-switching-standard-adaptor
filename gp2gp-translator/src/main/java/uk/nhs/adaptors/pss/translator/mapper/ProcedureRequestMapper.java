package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.II;
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.hl7.v3.TS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementUtil;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProcedureRequestMapper {
    private static final String META_PROFILE = "ProcedureRequest-1";

    private final CodeableConceptMapper codeableConceptMapper;

    public List<ProcedureRequest> mapProcedureRequests(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {
        return ehrExtract.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .map(RCMRMT030101UK04EhrComposition::getComponent)
            .flatMap(List::stream)
            .flatMap(this::extractAllPlanStatements)
            .filter(Objects::nonNull)
            .map(planStatement -> mapToProcedureRequest(ehrExtract, planStatement, patient, encounters, practiseCode))
            .toList();
    }

    private Stream<RCMRMT030101UK04PlanStatement> extractAllPlanStatements(RCMRMT030101UK04Component4 component4) {
        return Stream.concat(
            Stream.of(component4.getPlanStatement()),
            component4.hasCompoundStatement()
                ? CompoundStatementUtil.extractResourcesFromCompound(component4.getCompoundStatement(),
                    RCMRMT030101UK04Component02::hasPlanStatement, RCMRMT030101UK04Component02::getPlanStatement)
                .stream()
                .map(RCMRMT030101UK04PlanStatement.class::cast)
                : Stream.empty()
        );
    }

    public ProcedureRequest mapToProcedureRequest(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04PlanStatement planStatement,
        Patient patient, List<Encounter> encounters, String practiseCode) {
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
        procedureRequest.getIdentifier().add(buildIdentifier(id, practiseCode));
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
