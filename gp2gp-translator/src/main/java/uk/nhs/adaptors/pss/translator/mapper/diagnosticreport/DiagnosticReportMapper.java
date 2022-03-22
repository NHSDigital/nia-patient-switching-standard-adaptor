package uk.nhs.adaptors.pss.translator.mapper.diagnosticreport;

import static java.util.stream.Collectors.toCollection;

import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllCompoundStatements;
import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;
import static uk.nhs.adaptors.pss.translator.util.TextUtil.getLastLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Author;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.TS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.mapper.AbstractMapper;
import uk.nhs.adaptors.pss.translator.mapper.CodeableConceptMapper;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors;
import uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil;

@Service
public class DiagnosticReportMapper extends AbstractMapper<DiagnosticReport> {

    private static final String EXTENSION_IDENTIFIER_ROOT = "2.16.840.1.113883.2.1.4.5.5";
    private static final String META_PROFILE_URL_SUFFIX = "DiagnosticReport-1";

    @Override
    public List<DiagnosticReport> mapResources(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {
        return mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
            extractAllCompoundStatements(component)
                .filter(Objects::nonNull)
                .filter(ResourceFilterUtil::isDiagnosticReport)
                .map(compoundStatement -> {
                    DiagnosticReport diagnosticReport = createDiagnosticReport(
                        compoundStatement, patient, composition, encounters, practiseCode
                    );
                    getIssued(ehrExtract, compoundStatement, composition).ifPresent(diagnosticReport::setIssuedElement);
                    return diagnosticReport;
                }
                )).toList();
    }

    private DiagnosticReport createDiagnosticReport(RCMRMT030101UK04CompoundStatement compoundStatement, Patient patient,
        RCMRMT030101UK04EhrComposition composition, List<Encounter> encounters, String practiceCode) {
        final DiagnosticReport diagnosticReport = new DiagnosticReport();
        final String id = compoundStatement.getId().get(0).getRoot();

        diagnosticReport.setMeta(generateMeta(META_PROFILE_URL_SUFFIX));
        diagnosticReport.setId(id);
        diagnosticReport.addIdentifier(buildIdentifier(id, practiceCode));
        diagnosticReport.setCode(createCode());
        diagnosticReport.setStatus(DiagnosticReportStatus.UNKNOWN);
        diagnosticReport.setSubject(new Reference(patient));
        diagnosticReport.setSpecimen(getSpecimenReferences(compoundStatement));
        createIdentifierExtension(compoundStatement.getId()).ifPresent(diagnosticReport::addIdentifier);
        buildContext(composition, encounters).ifPresent(diagnosticReport::setContext);
        setResultReferences(compoundStatement, diagnosticReport);

        return diagnosticReport;
    }

    public void mapChildObservationComments(RCMRMT030101UK04EhrExtract ehrExtract, List<Observation> observationComments) {
        getCompositionsContainingClusterCompoundStatement(ehrExtract)
            .stream()
            .flatMap(ehrComposition -> ehrComposition.getComponent().stream())
            .filter(RCMRMT030101UK04Component4::hasCompoundStatement)
            .map(RCMRMT030101UK04Component4::getCompoundStatement)
            .flatMap(compoundStatement -> compoundStatement.getComponent().stream())
            .filter(RCMRMT030101UK04Component02::hasNarrativeStatement)
            .map(RCMRMT030101UK04Component02::getNarrativeStatement)
            .map(narrativeStatement -> getObservationCommentById(observationComments, narrativeStatement.getId().getRoot()))
            .flatMap(Optional::stream)
            .forEach(observationComment -> {
                observationComment.setEffective(null);
                observationComment.setComment(getLastLine(observationComment.getComment()));
            });
    }

    private List<Reference> getSpecimenReferences(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component02::hasCompoundStatement)
            .map(RCMRMT030101UK04Component02::getCompoundStatement)
            .filter(ResourceFilterUtil::isSpecimen)
            .map(compoundStatement1 -> new Reference(new IdType(ResourceType.Specimen.name(), compoundStatement1.getId().get(0).getRoot())))
            .toList();
    }

    private void setResultReferences(RCMRMT030101UK04CompoundStatement compoundStatement, DiagnosticReport diagnosticReport) {
        var resultReferences = compoundStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component02::hasNarrativeStatement)
            .map(RCMRMT030101UK04Component02::getNarrativeStatement)
            .map(narrativeStatement -> new Reference(new IdType(ResourceType.Observation.name(), narrativeStatement.getId().getRoot())))
            .collect(toCollection(ArrayList::new));

        if (!resultReferences.isEmpty()) {
            diagnosticReport.setResult(resultReferences);
        }
    }

    private Optional<Observation> getObservationCommentById(List<Observation> observationComments, String id) {
        return observationComments.stream()
            .filter(observationComment -> id.equals(observationComment.getId()))
            .findFirst();
    }

    private Optional<Identifier> createIdentifierExtension(List<II> id) {
        if (id.size() > 1) {
            final II idExtension = id.get(1);
            if (idExtension != null && EXTENSION_IDENTIFIER_ROOT.equals(idExtension.getRoot())) {
                return Optional.of(new Identifier()
                    .setSystem(EXTENSION_IDENTIFIER_ROOT)
                    .setValue(idExtension.getExtension()));
            }
        }
        return Optional.empty();
    }

    private Optional<Reference> buildContext(RCMRMT030101UK04EhrComposition ehrComposition, List<Encounter> encounters) {
        return encounters.stream()
            .filter(encounter -> encounter.getId().equals(ehrComposition.getId().getRoot()))
            .findFirst()
            .map(Reference::new);
    }

    private Optional<InstantType> getIssued(RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04CompoundStatement compoundStatement, RCMRMT030101UK04EhrComposition ehrComposition) {
        if (compoundStatementHasValidAvailabilityTime(compoundStatement)) {
            return Optional.of(parseToInstantType(compoundStatement.getAvailabilityTime().getValue()));
        }

        if (authorHasValidTimeValue(ehrComposition.getAuthor())) {
            return Optional.of(parseToInstantType(ehrComposition.getAuthor().getTime().getValue()));
        }

        if (availabilityTimeHasValue(ehrExtract.getAvailabilityTime())) {
            return Optional.of(parseToInstantType(ehrExtract.getAvailabilityTime().getValue()));
        }

        return Optional.empty();
    }

    private boolean compoundStatementHasValidAvailabilityTime(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null && compoundStatement.getAvailabilityTime() != null
            && compoundStatement.getAvailabilityTime().hasValue()
            && !compoundStatement.getAvailabilityTime().hasNullFlavor();
    }

    private boolean authorHasValidTimeValue(RCMRMT030101UK04Author author) {
        return author != null && author.hasTime()
            && author.getTime().hasValue()
            && !author.getTime().hasNullFlavor();
    }

    private boolean availabilityTimeHasValue(TS availabilityTime) {
        return availabilityTime != null && availabilityTime.hasValue() && !availabilityTime.hasNullFlavor();
    }

    private CodeableConcept createCode() {
        var codeableConcept = new CodeableConcept();
        codeableConcept
            .getCodingFirstRep()
            .setCode("721981007")
            .setSystem("http://snomed.info/sct")
            .setDisplay("Diagnostic studies report");
        return codeableConcept;
    }

    private List<RCMRMT030101UK04EhrComposition> getCompositionsContainingClusterCompoundStatement(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().stream()
            .flatMap(component -> component.getEhrFolder().getComponent().stream())
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> ehrComposition.getComponent()
                .stream()
                .flatMap(CompoundStatementResourceExtractors::extractAllCompoundStatements)
                .filter(Objects::nonNull)
                .anyMatch(ResourceFilterUtil::isDiagnosticReport))
            .toList();
    }
}
