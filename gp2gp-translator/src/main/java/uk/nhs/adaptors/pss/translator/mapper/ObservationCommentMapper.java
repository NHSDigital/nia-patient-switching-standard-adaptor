package uk.nhs.adaptors.pss.translator.mapper;

import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus.FINAL;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToDateTimeType;
import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil.extractEhrCompositionForCompoundNarrativeStatement;
import static uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil.extractEhrCompositionForNarrativeStatement;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;
import static uk.nhs.adaptors.pss.translator.util.TextUtil.getLastLine;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.TextUtil;

@Service
@AllArgsConstructor
public class ObservationCommentMapper {

    private static final String META_URL = "Observation-1";
    private static final String CODING_SYSTEM = "http://snomed.info/sct";
    private static final String CODING_CODE = "37331000000100";
    private static final String CODING_DISPLAY = "Comment note";

    public List<Observation> mapObservations(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters) {
        return getNarrativeStatements(ehrExtract).stream()
            .map(narrativeStatement -> {
                Observation observation = createObservationComment(narrativeStatement, patient);
                observation.setEffective(parseToDateTimeType(narrativeStatement.getAvailabilityTime().getValue()));
                observation.addPerformer(createPerformer(ehrExtract, narrativeStatement));
                setObservationContext(observation, ehrExtract, narrativeStatement.getId(), encounters); // Context may not always be mapped
                setObservationComment(observation, narrativeStatement.getText());
                observation.setIssuedElement(createIssued(ehrExtract, narrativeStatement.getId()));

                return observation;
            }).toList();
    }

    public List<Observation> mapDiagnosticChildObservations(List<RCMRMT030101UK04NarrativeStatement> narrativeStatements,
        RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters) {
        return narrativeStatements.stream()
            .map(narrativeStatement -> {
                Observation observation = createObservationComment(narrativeStatement, patient);
                observation.addPerformer(createDeepPerformer(ehrExtract, narrativeStatement));
                setObservationComment(observation, getLastLine(narrativeStatement.getText()));
                observation.setIssuedElement(createDeepIssued(ehrExtract, narrativeStatement.getId()));
                setDeepObservationContext(observation, ehrExtract, narrativeStatement.getId(), encounters);

                return observation;
            }).toList();
    }

    private Observation createObservationComment(RCMRMT030101UK04NarrativeStatement narrativeStatement, Patient patient) {
        var observation = new Observation();
        observation.setId(narrativeStatement.getId().getRoot());
        observation.addIdentifier(buildIdentifier(narrativeStatement.getId().getRoot(), "UNK")); //TODO: set practice code (NIAD-2021)
        observation.setMeta(generateMeta(META_URL));
        observation.setStatus(FINAL);
        observation.setSubject(new Reference(patient));
        observation.setCode(createCodeableConcept());

        return observation;
    }

    private void setObservationContext(Observation observation, RCMRMT030101UK04EhrExtract ehrExtract,
        II narrativeStatementId, List<Encounter> encounters) {
        var composition =
            extractEhrCompositionForNarrativeStatement(ehrExtract, narrativeStatementId);

        encounters
            .stream()
            .filter(encounter -> encounter.getId().equals(composition.getId().getRoot()))
            .findFirst()
            .ifPresent(encounter -> observation.setContext(new Reference(encounter)));
    }

    private void setDeepObservationContext(Observation observation, RCMRMT030101UK04EhrExtract ehrExtract,
        II narrativeStatementId, List<Encounter> encounters) {
        var composition =
            extractEhrCompositionForCompoundNarrativeStatement(ehrExtract, narrativeStatementId);

        encounters
            .stream()
            .filter(encounter -> encounter.getId().equals(composition.getId().getRoot()))
            .findFirst()
            .ifPresent(encounter -> observation.setContext(new Reference(encounter)));
    }

    private void setObservationComment(Observation observation, String text) {
        if (!text.isBlank()) {
            observation.setComment(text.trim());
        }
    }

    private InstantType createIssued(RCMRMT030101UK04EhrExtract ehrExtract, II narrativeStatementId) {
        RCMRMT030101UK04EhrComposition composition =
            extractEhrCompositionForNarrativeStatement(ehrExtract, narrativeStatementId);

        if (composition.getAuthor().getTime().getNullFlavor() == null) {
            return parseToInstantType(composition.getAuthor().getTime().getValue());
        }

        return parseToInstantType(ehrExtract.getAvailabilityTime().getValue());
    }

    private InstantType createDeepIssued(RCMRMT030101UK04EhrExtract ehrExtract, II narrativeStatementId) {
        RCMRMT030101UK04EhrComposition composition = extractEhrCompositionForCompoundNarrativeStatement(ehrExtract, narrativeStatementId);

        if (composition.getAuthor().getTime().getNullFlavor() == null) {
            return parseToInstantType(composition.getAuthor().getTime().getValue());
        }

        return parseToInstantType(ehrExtract.getAvailabilityTime().getValue());
    }

    private Reference createPerformer(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        var composition = extractEhrCompositionForNarrativeStatement(ehrExtract, narrativeStatement.getId());
        return getParticipantReference(narrativeStatement.getParticipant(), composition);
    }

    private Reference createDeepPerformer(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        var composition = extractEhrCompositionForCompoundNarrativeStatement(ehrExtract, narrativeStatement.getId());

        return getParticipantReference(narrativeStatement.getParticipant(), composition);
    }

    private CodeableConcept createCodeableConcept() {
        var codeableConcept = new CodeableConcept();
        codeableConcept.setCoding(
            Collections.singletonList(new Coding(CODING_SYSTEM, CODING_CODE, CODING_DISPLAY)));

        return codeableConcept;
    }

    private List<RCMRMT030101UK04NarrativeStatement> getNarrativeStatements(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent()
            .stream()
            .flatMap(component -> component.getEhrFolder().getComponent().stream())
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .map(RCMRMT030101UK04EhrComposition::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UK04Component4::getNarrativeStatement)
            .filter(Objects::nonNull)
            .filter(narrativeStatement -> !hasReferredToExternalDocument(narrativeStatement))
            .toList();
    }

    private boolean hasReferredToExternalDocument(RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        return narrativeStatement.getReference()
            .stream()
            .anyMatch(reference -> reference.getReferredToExternalDocument() != null);
    }
}
