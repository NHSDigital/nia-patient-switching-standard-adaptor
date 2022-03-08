package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;
import static uk.nhs.adaptors.pss.translator.util.TextUtil.getLastLine;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Specimen;
import org.hl7.fhir.dstu3.model.Specimen.SpecimenCollectionComponent;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04SpecimenRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpecimenMapper {

    private static final String SPECIMEN_META_PROFILE_SUFFIX = "Specimen-1";
    private static final String REFERENCE_PREFIX = "Specimen/";

    private final DateTimeMapper dateTimeMapper;

    public List<Specimen> mapSpecimen(RCMRMT030101UK04EhrExtract ehrExtract, List<DiagnosticReport> diagnosticReports,
        Patient patient, String practiceCode) {
        return diagnosticReports.stream()
            .flatMap(diagnosticReport -> diagnosticReport.getSpecimen().stream())
            .map(Reference::getReference)
            .map(reference -> getParentCompoundStatementByChildId(ehrExtract, reference.replace(REFERENCE_PREFIX, StringUtils.EMPTY)))
            .flatMap(Optional::stream)
            .flatMap(parentCompoundStatement -> parentCompoundStatement.getComponent().stream())
            .map(RCMRMT030101UK04Component02::getCompoundStatement)
            .filter(Objects::nonNull)
            .distinct()
            .map(childCompoundStatement -> createSpecimen(childCompoundStatement, patient, practiceCode))
            .toList();
    }

    private Specimen createSpecimen(RCMRMT030101UK04CompoundStatement specimenCompoundStatement, Patient patient, String practiceCode) {
        Specimen specimen = new Specimen();
        final String id = specimenCompoundStatement.getId().get(0).getRoot();
        specimen.setId(id);
        specimen.setMeta(generateMeta(SPECIMEN_META_PROFILE_SUFFIX));
        specimen.addIdentifier(buildIdentifier(id, practiceCode));
        specimen.setSubject(new Reference(patient));
        specimen.setNote(getNote(specimenCompoundStatement));
        getAccessionIdentifier(specimenCompoundStatement).ifPresent(specimen::setAccessionIdentifier);
        getType(specimenCompoundStatement).ifPresent(specimen::setType);
        getCollectedDateTime(specimenCompoundStatement).ifPresent(specimen::setCollection);

        return specimen;
    }

    private Optional<SpecimenCollectionComponent> getCollectedDateTime(RCMRMT030101UK04CompoundStatement specimenCompoundStatement) {
        /**
         * If CompoundStatement/specimen/specimenRole/effectiveTime/center/@value is set use it to generate the collection
         * .collectedDateTime (suitable TS to FHIR DateTime conversion).
         * otherwise no collection.collectedDateTime shoild be generated
         */
        var specimenRoleOpt = getSpecimenRole(specimenCompoundStatement);
        if (specimenRoleOpt.isPresent() && specimenRoleOpt.get().getEffectiveTime() != null) {
            var effectiveTime = specimenRoleOpt.get().getEffectiveTime();
            if (effectiveTime.hasCenter()) {
                var effective = dateTimeMapper.mapDateTime(effectiveTime.getCenter().getValue());
                return Optional.of(new SpecimenCollectionComponent().setCollected(effective));
            }
        }
        return Optional.empty();
    }

    private List<Annotation> getNote(RCMRMT030101UK04CompoundStatement specimenCompoundStatement) {
        /**
         * Find every NarrativeStatement component which is a direct child of the specimen CompoundStatement level.
         * Strip the GP2GP pathology CommentType and blank line and append to the note newline separated.
         * Usual conversion of linebreaks to newline characters
         */
        return specimenCompoundStatement.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component02::getNarrativeStatement)
            .filter(Objects::nonNull)
            .map(narrativeStatement -> getLastLine(narrativeStatement.getText()))
            .map(e -> new Annotation().setText(e))
            .toList();
    }

    private Optional<CodeableConcept> getType(RCMRMT030101UK04CompoundStatement specimenCompoundStatement) {
        var specimenRoleOpt = getSpecimenRole(specimenCompoundStatement);
        if (specimenRoleOpt.isPresent()) {
            var specimenMaterialOpt = Optional.ofNullable(specimenRoleOpt.get().getSpecimenSpecimenMaterial());
            if (specimenMaterialOpt.isPresent()) {
                if (specimenMaterialOpt.get().getDesc() != null) {
                    return Optional.of(new CodeableConcept().setText(specimenMaterialOpt.get().getDesc()));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Identifier> getAccessionIdentifier(RCMRMT030101UK04CompoundStatement specimenCompoundStatement) {
        var specimenRoleOpt = getSpecimenRole(specimenCompoundStatement);
        if (specimenRoleOpt.isPresent()) {
            var specimenIds = specimenRoleOpt.get().getId();
            if (specimenIds.size() > 1) {
                return Optional.of(new Identifier().setValue(specimenIds.get(1).getExtension()));
            }
        }
        return Optional.empty();
    }

    private Optional<RCMRMT030101UK04SpecimenRole> getSpecimenRole(RCMRMT030101UK04CompoundStatement specimenCompoundStatement) {
        return !specimenCompoundStatement.getSpecimen().isEmpty() ?
            Optional.ofNullable(specimenCompoundStatement.getSpecimen().get(0).getSpecimenRole()) : Optional.empty();
    }

    private Optional<RCMRMT030101UK04CompoundStatement> getParentCompoundStatementByChildId(RCMRMT030101UK04EhrExtract ehrExtract, String id) {
        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent().stream()
            .flatMap(e -> e.getEhrComposition().getComponent().stream())
            .map(RCMRMT030101UK04Component4::getCompoundStatement)
            .filter(Objects::nonNull)
            .filter(compoundStatement -> compoundStatement.getComponent()
                .stream()
                .map(RCMRMT030101UK04Component02::getCompoundStatement)
                .filter(Objects::nonNull)
                .anyMatch(e -> id.equals(e.getId().get(0).getRoot()))
            ).findFirst();
    }

}
