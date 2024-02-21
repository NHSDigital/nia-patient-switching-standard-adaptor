package uk.nhs.adaptors.pss.translator.mapper.diagnosticreport;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Specimen;
import org.hl7.fhir.dstu3.model.Specimen.SpecimenCollectionComponent;
import org.hl7.v3.RCMRMT030101UKComponent02;
import org.hl7.v3.RCMRMT030101UKCompoundStatement;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKSpecimenRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.adaptors.pss.translator.mapper.DateTimeMapper;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.pss.translator.util.CDUtil.extractSnomedCode;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;
import static uk.nhs.adaptors.pss.translator.util.TextUtil.extractPmipComment;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpecimenMapper {

    private static final String SPECIMEN_META_PROFILE_SUFFIX = "Specimen-1";
    private static final String REFERENCE_PREFIX = "Specimen/";
    private static final String SPECIMEN_CODE = "123038009";

    private final DateTimeMapper dateTimeMapper;

    public List<Specimen> mapSpecimen(RCMRMT030101UKEhrExtract ehrExtract, List<DiagnosticReport> diagnosticReports,
        Patient patient, String practiceCode) {

        return diagnosticReports.stream()
            .flatMap(diagnosticReport -> diagnosticReport.getSpecimen().stream())
            .map(Reference::getReference)
            .map(reference -> getParentCompoundStatementByChildId(ehrExtract, reference.replace(REFERENCE_PREFIX, StringUtils.EMPTY)))
            .flatMap(Optional::stream)
            .flatMap(parentCompoundStatement -> parentCompoundStatement.getComponent().stream())
            .map(RCMRMT030101UKComponent02::getCompoundStatement)
            .filter(Objects::nonNull)
            .distinct()
            .map(childCompoundStatement -> createSpecimen(childCompoundStatement, patient, practiceCode))
            .toList();
    }

    public List<Observation> removeSurplusObservationComments(RCMRMT030101UKEhrExtract ehrExtract,
        List<Observation> observationComments) {

        var specimenCompoundStatements = findAllSpecimenCompoundStatements(ehrExtract);

        var observationCommentIds = specimenCompoundStatements.stream()
            .flatMap(compoundStatement -> compoundStatement.getComponent().stream())
            .filter(RCMRMT030101UKComponent02::hasNarrativeStatement)
            .map(RCMRMT030101UKComponent02::getNarrativeStatement)
            .map(narrativeStatement -> narrativeStatement.getId().getRoot())
            .toList();

        var surplusObservationComments = observationComments.stream()
            .filter(observation -> observationCommentIds.contains(observation.getId()))
            .toList();

        observationComments.removeAll(surplusObservationComments);

        return observationComments;
    }

    private Specimen createSpecimen(RCMRMT030101UKCompoundStatement specimenCompoundStatement, Patient patient, String practiceCode) {

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

    private Optional<SpecimenCollectionComponent> getCollectedDateTime(RCMRMT030101UKCompoundStatement specimenCompoundStatement) {

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

    private List<Annotation> getNote(RCMRMT030101UKCompoundStatement specimenCompoundStatement) {

        var text = specimenCompoundStatement.getComponent()
            .stream()
            .map(RCMRMT030101UKComponent02::getNarrativeStatement)
            .filter(Objects::nonNull)
            .map(narrativeStatement -> extractPmipComment(narrativeStatement.getText()))
            .collect(Collectors.joining(StringUtils.LF));

        return List.of(new Annotation().setText(text));
    }

    private Optional<CodeableConcept> getType(RCMRMT030101UKCompoundStatement specimenCompoundStatement) {

        var specimenRoleOpt = getSpecimenRole(specimenCompoundStatement);

        if (specimenRoleOpt.isPresent()) {
            var specimenMaterialOpt = Optional.ofNullable(specimenRoleOpt.get().getSpecimenSpecimenMaterial());
            if (specimenMaterialOpt.isPresent() && specimenMaterialOpt.get().getDesc() != null) {
                return Optional.of(new CodeableConcept().setText(specimenMaterialOpt.get().getDesc()));
            }
        }

        return Optional.empty();
    }

    private Optional<Identifier> getAccessionIdentifier(RCMRMT030101UKCompoundStatement specimenCompoundStatement) {
        var specimenRoleOpt = getSpecimenRole(specimenCompoundStatement);
        if (specimenRoleOpt.isPresent()) {
            var specimenIds = specimenRoleOpt.get().getId();
            if (specimenIds.size() > 1) {
                return Optional.of(new Identifier().setValue(specimenIds.get(1).getExtension()));
            }
        }
        return Optional.empty();
    }

    private Optional<RCMRMT030101UKSpecimenRole> getSpecimenRole(RCMRMT030101UKCompoundStatement specimenCompoundStatement) {

        return !specimenCompoundStatement.getSpecimen().isEmpty()
            ? Optional.ofNullable(specimenCompoundStatement.getSpecimen().get(0).getSpecimenRole())
            : Optional.empty();
    }

    private Optional<RCMRMT030101UKCompoundStatement> getParentCompoundStatementByChildId(
            RCMRMT030101UKEhrExtract ehrExtract, String id) {

        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent().stream()
            .flatMap(component3 -> component3.getEhrComposition().getComponent().stream())
            .flatMap(CompoundStatementResourceExtractors::extractAllCompoundStatements)
            .filter(Objects::nonNull)
            .filter(compoundStatement -> compoundStatement.getComponent()
                .stream()
                .map(RCMRMT030101UKComponent02::getCompoundStatement)
                .filter(Objects::nonNull)
                .anyMatch(e -> id.equals(e.getId().get(0).getRoot()))
            ).findFirst();
    }

    private List<RCMRMT030101UKCompoundStatement> findAllSpecimenCompoundStatements(RCMRMT030101UKEhrExtract ehrExtract) {

        var topLevelComponents = ehrExtract.getComponent().get(0).getEhrFolder().getComponent().stream()
            .flatMap(component3 -> component3.getEhrComposition().getComponent().stream())
            .toList();

        return topLevelComponents.stream()
            .flatMap(CompoundStatementResourceExtractors::extractAllCompoundStatements)
            .filter(Objects::nonNull)
            .filter(RCMRMT030101UKCompoundStatement::hasCode)
            .filter(compoundStatement -> {
                Optional<String> code = extractSnomedCode(compoundStatement.getCode());
                return code.map(SPECIMEN_CODE::equals).orElse(false);
            })
            .toList();
    }
}
