package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.TextUtil.addLine;
import static uk.nhs.adaptors.pss.translator.util.TextUtil.getLastLine;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationRelatedComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpecimenCompoundsMapper {

    private static final String BATTERY_CLASSCODE = "BATTERY";
    private static final String CLUSTER_CLASSCODE = "CLUSTER";
    private static final String USER_COMMENT_HEADER = "USER COMMENT";

    public void handleSpecimenChildComponents(RCMRMT030101UK04EhrExtract ehrExtract, List<Observation> observations,
        List<Observation> observationComments, List<DiagnosticReport> diagnosticReports) {
        diagnosticReports.forEach(diagnosticReport ->
            getCompoundStatementByDRId(ehrExtract, diagnosticReport.getId()).ifPresent(parentCompoundStatement ->
                getSpecimenCompoundStatements(parentCompoundStatement).forEach(specimenCompoundStatement -> {

                    getObservationStatementsInCompound(specimenCompoundStatement).forEach(
                        specimenObservationStatement -> getObservationById(observations, specimenObservationStatement.getId().getRoot())
                            .ifPresent(observation -> handleObservationStatement(specimenCompoundStatement, observation, diagnosticReport))
                    );
                    getCompoundStatementsInSpecimenCompound(specimenCompoundStatement, CLUSTER_CLASSCODE).forEach(
                        specimenChildCompoundStatement -> handleClusterCompoundStatement(
                            specimenCompoundStatement, specimenChildCompoundStatement, observations, observationComments, diagnosticReport
                        )
                    );
                    getCompoundStatementsInSpecimenCompound(specimenCompoundStatement, BATTERY_CLASSCODE).forEach(
                        specimenChildCompoundStatement -> handleBatteryCompoundStatement(
                            specimenCompoundStatement, specimenChildCompoundStatement, observations, observationComments, diagnosticReport
                        )
                    );
                })));
    }

    private void handleObservationStatement(RCMRMT030101UK04CompoundStatement specimenCompoundStatement,
        Observation observation, DiagnosticReport diagnosticReport) {

        final Reference specimenReference = new Reference(new IdType(
            ResourceType.Specimen.name(),
            specimenCompoundStatement.getId().get(0).getRoot()
        ));
        observation.setSpecimen(specimenReference);

        if (!containsReference(diagnosticReport.getResult(), observation.getId())) {
            diagnosticReport.addResult(new Reference(observation));
        }
    }

    private void handleNarrativeStatements(RCMRMT030101UK04CompoundStatement clusterCompoundStatement,
        List<Observation> observationComments, Observation observation) {
        getNarrativeStatementsInCompound(clusterCompoundStatement).forEach(clusterChildNarrativeStatement -> {
            if (clusterChildNarrativeStatement.getText().contains(USER_COMMENT_HEADER)) {
                getObservationById(observationComments, clusterChildNarrativeStatement.getId().getRoot())
                    .ifPresent(observationComment -> {
                        observationComment.setComment(getLastLine(observationComment.getComment()));

                        if (observation != null) {
                            if (!containsRelatedComponent(observationComment, observation.getId())) {
                                observationComment.addRelated(new ObservationRelatedComponent(new Reference(observation)));
                            }

                            if (!containsRelatedComponent(observation, observationComment.getId())) {
                                observation.addRelated(new ObservationRelatedComponent(new Reference(observationComment)));
                            }
                        }
                    });
            } else {
                if (observation != null) {
                    observation.setComment(addLine(observation.getComment(), getLastLine(clusterChildNarrativeStatement.getText())));
                }
            }
        });
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
            .flatMap(e -> e.getEhrComposition().getComponent().stream())
            .map(RCMRMT030101UK04Component4::getCompoundStatement)
            .filter(Objects::nonNull)
            .filter(e -> id.equals(e.getId().get(0).getRoot()))
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
}
