package uk.nhs.adaptors.pss.translator.mapper.diagnosticreport;

import static org.hl7.fhir.dstu3.model.ResourceType.Specimen;

import static uk.nhs.adaptors.pss.translator.util.TextUtil.addLine;
import static uk.nhs.adaptors.pss.translator.util.TextUtil.getLastLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationRelatedComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationRelationshipType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.mapper.diagnosticreport.SpecimenBatteryMapper.SpecimenBatteryParameters;
import uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpecimenCompoundsMapper {

    private static final String BATTERY_CLASSCODE = "BATTERY";
    private static final String CLUSTER_CLASSCODE = "CLUSTER";
    private static final String USER_COMMENT_HEADER = "USER COMMENT";

    private final SpecimenBatteryMapper batteryMapper;

    public List<Observation> handleSpecimenChildComponents(RCMRMT030101UK04EhrExtract ehrExtract, List<Observation> observations,
        List<Observation> observationComments, List<DiagnosticReport> diagnosticReports,
        Patient patient, List<Encounter> encounters, String practiseCode) {
        final List<Observation> batteryObservations = new ArrayList<>();
        diagnosticReports.forEach(diagnosticReport ->
            getCompoundStatementByDRId(ehrExtract, diagnosticReport.getId()).ifPresent(parentCompoundStatement ->
                getSpecimenCompoundStatements(parentCompoundStatement).forEach(specimenCompoundStatement -> {
                    getObservationStatementsInCompound(specimenCompoundStatement).forEach(
                        specimenObservationStatement -> getObservationById(observations, specimenObservationStatement.getId().getRoot())
                            .ifPresent(observation -> handleObservationStatement(specimenCompoundStatement, observation, diagnosticReport))
                    );
                    getCompoundStatementsInSpecimenCompound(specimenCompoundStatement, CLUSTER_CLASSCODE).forEach(
                        clusterCompoundStatement -> handleClusterCompoundStatement(
                            specimenCompoundStatement, clusterCompoundStatement, observations, observationComments, diagnosticReport
                        )
                    );
                    getCompoundStatementsInSpecimenCompound(specimenCompoundStatement, BATTERY_CLASSCODE).forEach(
                        batteryCompoundStatement -> {
                            handleBatteryCompoundStatement(
                                specimenCompoundStatement, batteryCompoundStatement, observations, observationComments, diagnosticReport
                            );

                            final SpecimenBatteryParameters batteryParameters = SpecimenBatteryParameters.builder()
                                .ehrExtract(ehrExtract)
                                .batteryCompoundStatement(batteryCompoundStatement)
                                .specimenCompoundStatement(specimenCompoundStatement)
                                .ehrComposition(getCurrentEhrComposition(ehrExtract, parentCompoundStatement))
                                .diagnosticReport(diagnosticReport)
                                .patient(patient)
                                .encounters(encounters)
                                .practiseCode(practiseCode)
                                .build();

                            batteryObservations.add(batteryMapper.mapBatteryObservation(batteryParameters));
                        }
                    );
                })));
        return batteryObservations;
    }

    private void handleObservationStatement(RCMRMT030101UK04CompoundStatement specimenCompoundStatement,
        Observation observation, DiagnosticReport diagnosticReport) {
        final Reference specimenReference = new Reference(new IdType(
            Specimen.name(),
            specimenCompoundStatement.getId().get(0).getRoot()
        ));
        observation.setSpecimen(specimenReference);
        observation.addCategory(createCategory());

        if (!containsReference(diagnosticReport.getResult(), observation.getId())) {
            diagnosticReport.addResult(new Reference(observation));
        }
    }

    private void handleNarrativeStatements(RCMRMT030101UK04CompoundStatement compoundStatement,
        List<Observation> observationComments, Observation observation) {
        getNarrativeStatementsInCompound(compoundStatement).forEach(childNarrativeStatement -> {
            if (childNarrativeStatement.getText().contains(USER_COMMENT_HEADER)) {
                getObservationById(observationComments, childNarrativeStatement.getId().getRoot())
                    .ifPresent(observationComment -> {
                        observationComment.setEffective(null);
                        observationComment.setComment(getLastLine(observationComment.getComment()));
                        createRelationship(observation, observationComment);
                    });
            } else if (observation != null) {
                observation.setComment(addLine(observation.getComment(), getLastLine(childNarrativeStatement.getText())));
            }
        });
    }

    private void createRelationship(Observation observation, Observation observationComment) {
        if (observation != null) {
            if (!containsRelatedComponent(observationComment, observation.getId())) {
                observationComment.addRelated(new ObservationRelatedComponent(new Reference(observation))
                    .setType(ObservationRelationshipType.DERIVEDFROM)
                );
            }

            if (!containsRelatedComponent(observation, observationComment.getId())) {
                observation.addRelated(new ObservationRelatedComponent(new Reference(observationComment))
                    .setType(ObservationRelationshipType.HASMEMBER));
            }
        }
    }

    private void handleClusterCompoundStatement(RCMRMT030101UK04CompoundStatement specimenCompoundStatement,
        RCMRMT030101UK04CompoundStatement clusterCompoundStatement,
        List<Observation> observations, List<Observation> observationComments, DiagnosticReport diagnosticReport) {
        clusterCompoundStatement.getComponent().stream()
            .filter(RCMRMT030101UK04Component02::hasObservationStatement)
            .map(RCMRMT030101UK04Component02::getObservationStatement)
            .forEach(observationStatement -> {
                var observationOpt = getObservationById(observations, observationStatement.getId().getRoot());
                observationOpt.ifPresent(observation -> handleObservationStatement(
                    specimenCompoundStatement, observation, diagnosticReport)
                );
                handleNarrativeStatements(clusterCompoundStatement, observationComments, observationOpt.orElse(null));
            });
    }

    private void handleBatteryCompoundStatement(RCMRMT030101UK04CompoundStatement specimenCompoundStatement,
        RCMRMT030101UK04CompoundStatement batteryCompoundStatement,
        List<Observation> observations, List<Observation> observationComments, DiagnosticReport diagnosticReport) {
        batteryCompoundStatement.getComponent().stream()
            .filter(RCMRMT030101UK04Component02::hasCompoundStatement)
            .map(RCMRMT030101UK04Component02::getCompoundStatement)
            .filter(compoundStatement -> CLUSTER_CLASSCODE.equals(compoundStatement.getClassCode().get(0)))
            .forEach(compoundStatement -> handleClusterCompoundStatement(
                specimenCompoundStatement, compoundStatement, observations, observationComments, diagnosticReport
            ));

        batteryCompoundStatement.getComponent().stream()
            .filter(RCMRMT030101UK04Component02::hasCompoundStatement)
            .map(RCMRMT030101UK04Component02::getCompoundStatement)
            .forEach(compoundStatement -> compoundStatement.getComponent().stream()
                .filter(RCMRMT030101UK04Component02::hasObservationStatement)
                .map(RCMRMT030101UK04Component02::getObservationStatement)
                .findFirst()
                .flatMap(observationStatement -> getObservationById(observations, observationStatement.getId().getRoot()))
                .ifPresent(observation -> handleObservationStatement(
                    specimenCompoundStatement, observation, diagnosticReport)
                ));

        batteryCompoundStatement.getComponent().stream()
            .filter(RCMRMT030101UK04Component02::hasObservationStatement)
            .map(RCMRMT030101UK04Component02::getObservationStatement)
            .forEach(observationStatement ->
                getObservationById(observations, observationStatement.getId().getRoot()).ifPresent(observation -> {
                    handleObservationStatement(specimenCompoundStatement, observation, diagnosticReport);
                    handleNarrativeStatements(batteryCompoundStatement, observationComments, observation);
                }));
    }

    private Optional<RCMRMT030101UK04CompoundStatement> getCompoundStatementByDRId(RCMRMT030101UK04EhrExtract ehrExtract, String id) {
        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent()
            .stream()
            .flatMap(component3 -> component3.getEhrComposition().getComponent().stream())
            .flatMap(CompoundStatementResourceExtractors::extractAllCompoundStatements)
            .filter(Objects::nonNull)
            .filter(compoundStatement -> id.equals(compoundStatement.getId().get(0).getRoot()))
            .findFirst();
    }

    private Optional<Observation> getObservationById(List<Observation> observations, String id) {
        return observations.stream()
            .filter(e -> id.equals(e.getId()))
            .findFirst();
    }

    private boolean containsReference(List<Reference> references, String id) {
        if (!references.isEmpty()) {
            return references.stream()
                .map(reference -> {
                    if (reference.hasReference()) {
                        return reference.getReference();
                    }
                    if (reference.getResource() != null) {
                        return reference.getResource().getIdElement().getValue();
                    }
                    return StringUtils.EMPTY;
                }).anyMatch(referenceId -> referenceId.contains(id));
        }
        return false;
    }

    private boolean containsRelatedComponent(Observation observation, String id) {
        if (!observation.getRelated().isEmpty()) {
            return observation.getRelated().stream()
                .map(relatedComponent -> relatedComponent.getTarget().getResource())
                .filter(Objects::nonNull)
                .anyMatch(resource -> id.equals(resource.getIdElement().getValue()));
        }
        return false;
    }

    private CodeableConcept createCategory() {
        var codeableConcept = new CodeableConcept();
        codeableConcept
            .getCodingFirstRep()
            .setCode("laboratory")
            .setSystem("http://hl7.org/fhir/observation-category")
            .setDisplay("Laboratory");
        return codeableConcept;
    }

    private List<RCMRMT030101UK04CompoundStatement> getSpecimenCompoundStatements(
        RCMRMT030101UK04CompoundStatement parentCompoundStatement) {
        return parentCompoundStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component02::hasCompoundStatement)
            .map(RCMRMT030101UK04Component02::getCompoundStatement)
            .toList();
    }

    private List<RCMRMT030101UK04ObservationStatement> getObservationStatementsInCompound(
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component02::hasObservationStatement)
            .map(RCMRMT030101UK04Component02::getObservationStatement)
            .toList();
    }

    private List<RCMRMT030101UK04NarrativeStatement> getNarrativeStatementsInCompound(
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component02::hasNarrativeStatement)
            .map(RCMRMT030101UK04Component02::getNarrativeStatement)
            .toList();
    }

    private List<RCMRMT030101UK04CompoundStatement> getCompoundStatementsInSpecimenCompound(
        RCMRMT030101UK04CompoundStatement specimenCompoundStatement, String classCode) {
        return specimenCompoundStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component02::hasCompoundStatement)
            .map(RCMRMT030101UK04Component02::getCompoundStatement)
            .filter(compoundStatement -> classCode.equals(compoundStatement.getClassCode().get(0)))
            .toList();
    }

    private RCMRMT030101UK04EhrComposition getCurrentEhrComposition(RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04CompoundStatement parentCompoundStatement) {
        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component3::hasEhrComposition)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(e -> e.getComponent()
                .stream()
                .flatMap(CompoundStatementResourceExtractors::extractAllCompoundStatements)
                .anyMatch(parentCompoundStatement::equals)
            ).findFirst().get();
    }
}
