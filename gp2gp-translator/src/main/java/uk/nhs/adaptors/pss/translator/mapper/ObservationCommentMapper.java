package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus.FINAL;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementUtil;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

@Service
@AllArgsConstructor
public class ObservationCommentMapper {

    private static final String META_URL = "Observation-1";
    private static final String CODING_SYSTEM = "http://snomed.info/sct";
    private static final String CODING_CODE = "37331000000100";
    private static final String CODING_DISPLAY = "Comment note";

    public List<Observation> mapObservations(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {

        return ehrExtract.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .flatMap(ehrComposition -> ehrComposition.getComponent()
                .stream()
                .flatMap(this::extractAllNarrativeStatements)
                .filter(Objects::nonNull)
                .filter(narrativeStatement -> !hasReferredToExternalDocument(narrativeStatement))
                .map(narrativeStatement -> {
                    var narrativeStatementId = narrativeStatement.getId();
                    var observation = new Observation();
                    observation.setId(narrativeStatement.getId().getRoot());
                    observation.setMeta(generateMeta(META_URL));
                    observation.setStatus(FINAL);
                    observation.setSubject(new Reference(patient));
                    observation.setIssuedElement(createIssued(ehrExtract, ehrComposition));
                    observation.setCode(createCodeableConcept());
                    observation.setEffective(
                        DateFormatUtil.parseToDateTimeType(narrativeStatement.getAvailabilityTime().getValue())
                    );

                    observation.addPerformer(createPerformer(ehrComposition, narrativeStatement));

                    observation.addIdentifier(buildIdentifier(narrativeStatementId.getRoot(), practiseCode));

                    setObservationComment(observation, narrativeStatement.getText());

                    // Context may not always be mapped
                    setObservationContext(observation, ehrComposition, encounters);
                    return observation;
                })).toList();
    }

    private void setObservationContext(Observation observation, RCMRMT030101UK04EhrComposition composition, List<Encounter> encounters) {

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

    private InstantType createIssued(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04EhrComposition composition) {
        if (composition.getAuthor().getTime().getNullFlavor() == null) {
            return DateFormatUtil.parseToInstantType(composition.getAuthor().getTime().getValue());
        }

        return DateFormatUtil.parseToInstantType(ehrExtract.getAvailabilityTime().getValue());
    }

    private Reference createPerformer(RCMRMT030101UK04EhrComposition composition, RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        return ParticipantReferenceUtil.getParticipantReference(narrativeStatement.getParticipant(), composition);
    }

    private CodeableConcept createCodeableConcept() {
        var codeableConcept = new CodeableConcept();
        codeableConcept.setCoding(
            Collections.singletonList(new Coding(CODING_SYSTEM, CODING_CODE, CODING_DISPLAY)));

        return codeableConcept;
    }

    private Stream<RCMRMT030101UK04NarrativeStatement> extractAllNarrativeStatements(RCMRMT030101UK04Component4 component4) {
        return Stream.concat(
            Stream.of(component4.getNarrativeStatement()),
            component4.hasCompoundStatement() ? CompoundStatementUtil.extractResourcesFromCompound(component4.getCompoundStatement(),
                    RCMRMT030101UK04Component02::hasNarrativeStatement, RCMRMT030101UK04Component02::getNarrativeStatement)
                .stream()
                .map(RCMRMT030101UK04NarrativeStatement.class::cast)
                : Stream.empty()
        );
    }

    private boolean hasReferredToExternalDocument(RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        return narrativeStatement.getReference()
            .stream()
            .anyMatch(reference -> reference.getReferredToExternalDocument() != null);
    }
}
