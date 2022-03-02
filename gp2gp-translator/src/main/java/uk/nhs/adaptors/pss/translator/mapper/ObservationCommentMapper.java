package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus.FINAL;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;
import uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil;

@Service
@AllArgsConstructor
public class ObservationCommentMapper {

    private static final String META_URL = "Observation-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String CODING_SYSTEM = "http://snomed.info/sct";
    private static final String CODING_CODE = "37331000000100";
    private static final String CODING_DISPLAY = "Comment note";

    public List<Observation> mapObservations(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters) {

        var narrativeStatements =  getNarrativeStatements(ehrExtract);

        return narrativeStatements
            .stream()
            .map(narrativeStatement -> {
                var narrativeStatementId = narrativeStatement.getId();
                var observation = new Observation();
                observation.setId(narrativeStatement.getId().getRoot());
                observation.setMeta(generateMeta(META_URL));
                observation.setStatus(FINAL);
                observation.setSubject(new Reference(patient));
                observation.setIssuedElement(createIssued(ehrExtract, narrativeStatement.getId()));
                observation.setCode(createCodeableConcept());
                observation.setEffective(
                    DateFormatUtil.parseToDateTimeType(narrativeStatement.getAvailabilityTime().getValue())
                );

                observation.setPerformer(
                    Collections.singletonList(createPerformer(ehrExtract, narrativeStatement))
                );

                observation.setIdentifier(
                    Collections.singletonList(createIdentifier(narrativeStatementId.getRoot()))
                );

                setObservationComment(observation, narrativeStatement.getText());

                // Context may not always be mapped
                setObservationContext(observation, ehrExtract, narrativeStatementId, encounters);

                return observation;
            }).toList();
    }

    private void setObservationContext(Observation observation, RCMRMT030101UK04EhrExtract ehrExtract,
        II narrativeStatementId, List<Encounter> encounters) {
        var composition =
            EhrResourceExtractorUtil.extractEhrCompositionForNarrativeStatement(ehrExtract, narrativeStatementId);

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

    private Meta createMeta() {
        var meta = new Meta();
        meta.setProfile(Collections.singletonList(new UriType(META_URL)));

        return meta;
    }

    private Identifier createIdentifier(String narrativeStatementId) {
        var identifier = new Identifier();
        identifier.setSystem(IDENTIFIER_SYSTEM);
        identifier.setValue(narrativeStatementId);

        return identifier;
    }

    private InstantType createIssued(RCMRMT030101UK04EhrExtract ehrExtract, II narrativeStatementId) {
        RCMRMT030101UK04EhrComposition composition =
            EhrResourceExtractorUtil.extractEhrCompositionForNarrativeStatement(ehrExtract, narrativeStatementId);

        if (composition.getAuthor().getTime().getNullFlavor() == null) {
            return DateFormatUtil.parseToInstantType(composition.getAuthor().getTime().getValue());
        }

        return DateFormatUtil.parseToInstantType(ehrExtract.getAvailabilityTime().getValue());
    }

    private Reference createPerformer(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        RCMRMT030101UK04EhrComposition composition =
            EhrResourceExtractorUtil.extractEhrCompositionForNarrativeStatement(ehrExtract, narrativeStatement.getId());

        return ParticipantReferenceUtil.getParticipantReference(narrativeStatement.getParticipant(), composition);
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
            .filter(narrativeStatement -> !ResourceFilterUtil.hasReferredToExternalDocument(narrativeStatement))
            .toList();
    }
}
