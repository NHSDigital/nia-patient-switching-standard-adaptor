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

    /**
     * The Observation resources generated for each of these components will
     * need to be refernced from DiagnosticReport.results and
     * each Observation will include a reference to the Specimen resource
     */

    public void handleSpecimenChildComponents(RCMRMT030101UK04EhrExtract ehrExtract, List<Observation> observations,
        List<Observation> observationComments, List<DiagnosticReport> diagnosticReports) {
        diagnosticReports.forEach(diagnosticReport ->
            getCompoundStatementByDRId(ehrExtract, diagnosticReport.getId()).ifPresent(parentCompoundStatement ->
                getSpecimenCompoundStatements(parentCompoundStatement).forEach(specimenCompoundStatement -> {
                    /**
                     * Handling CASE 1: Plain ObservationStatement
                     */
                    getObservationStatementsInCompound(specimenCompoundStatement).forEach(
                        specimenObservationStatement -> getObservationById(observations, specimenObservationStatement.getId().getRoot())
                            .ifPresent(observation -> handleObservationStatement(
                                observation, specimenCompoundStatement, diagnosticReport)
                            )
                    );

                    /**
                     * Handling CASE 2: Cluster CompoundStatement -> ObservationStatement and NarrativeStatement inside
                     */
                    getCompoundStatementsInSpecimenCompound(specimenCompoundStatement, CLUSTER_CLASSCODE).forEach(
                        specimenChildCompoundStatement -> handleClusterCompoundStatement(
                            specimenChildCompoundStatement, observations, observationComments, diagnosticReport
                        )
                    );

                    /**
                     * Handling CASE 3: Battery CompoundStatement -> Cluster and ObservationStatement inside
                     */
                    getCompoundStatementsInSpecimenCompound(specimenCompoundStatement, BATTERY_CLASSCODE).forEach(
                        specimenChildCompoundStatement -> handleBatteryCompoundStatement(
                            specimenChildCompoundStatement, observations, observationComments, diagnosticReport
                        )
                    );
                })));
    }

    private void handleObservationStatement(Observation observation, RCMRMT030101UK04CompoundStatement parentCompoundStatement,
        DiagnosticReport diagnosticReport) {
        /**
         * Case 1 - ObservationStatement component
         * This is the simplest of cases - a single ObservationStatement component
         * This should be mapped through a variant of the general ObservationStatement -> Observation transform.
         * FIND OBSERVATION BY ID, CREATE THE REFERENCE TO THE DIAGNOSTICREPORT IT ORIGINALLY COMES FROM
         * need to be referenced from DiagnosticReport.results and
         * each Observation will include a reference to the Specimen resource
         */
        final Reference specimenReference = new Reference(new IdType(
            ResourceType.Specimen.name(),
            parentCompoundStatement.getId().get(0).getRoot())
        );
        observation.setSpecimen(specimenReference);

        if (!containsReference(diagnosticReport.getResult(), observation.getId())) {
            diagnosticReport.addResult(new Reference(observation));
        }
    }

    private void handleNarrativeStatements(RCMRMT030101UK04CompoundStatement clusterCompoundStatement,
        List<Observation> observationComments, Observation observation) {
        /**
         * Case 2 - CompoundStatement classCode="CLUSTER"
         * A CompoundStatement with classCode CLUSTER is the mechanism for grouping result text and
         * any user filing comments in the form of NarrativeStatement with the ObservationStatement carrying the result
         *
         * The single (there will always be only one) ObservationStatement contained within the CompoundStatement is transformed
         using the same approach as Case 1
         *
         * Every NarrativeStatement within the CompoundStatement is processed in order
         *
         * If the CommentType header is USER COMMENT, then an Observation (Comment) is generated using the NarrativeSatatement to
         Observation (Comment)
         * map with the GP2GP pathology header (CommentType and blank line) stripped. The generated Observation (Comment) resource
         * is referenced by the result Observation generated from the ObservationStatement ( related linkaged of type 'has-member' )
         * and in turn the generated Observation (Comment) references the result observation via a related linkage of type
         'derived-from'
         *
         * All other NarrativeStatement within the CompoundStatement are processed into the result Observation comment field - new
         line seperated
         */

        getNarrativeStatementsInCompound(clusterCompoundStatement).forEach(clusterChildNarrativeStatement -> {
            if (clusterChildNarrativeStatement.getText().contains(USER_COMMENT_HEADER)) {
                getObservationById(observationComments, clusterChildNarrativeStatement.getId().getRoot())
                    .ifPresent(observationComment -> {
                        observationComment.setComment(getLastLine(observationComment.getComment()));

                        if (observation != null) {
                            if (!containsRelatedComponent(observationComment, observation.getId())) {
                                observationComment.addRelated(new ObservationRelatedComponent(new Reference(observation)));
                                //ObservationComment - derived-from
                            }

                            if (!containsRelatedComponent(observation, observationComment.getId())) {
                                observation.addRelated(new ObservationRelatedComponent(new Reference(observationComment)));
                                //Observation -has-member
                            }
                        }
                    });
            } else {
                if (observation != null) {
                    observation.setComment(addLine(observation.getComment(), clusterChildNarrativeStatement.getText()));
                }
            }
        });
    }

    private void handleClusterCompoundStatement(RCMRMT030101UK04CompoundStatement clusterCompoundStatement,
        List<Observation> observations, List<Observation> observationComments, DiagnosticReport diagnosticReport) {
        clusterCompoundStatement.getComponent().stream()
            .filter(RCMRMT030101UK04Component02::hasObservationStatement)
            .map(RCMRMT030101UK04Component02::getObservationStatement)
            .forEach(observationStatement -> {
                var observationOpt = getObservationById(observations, observationStatement.getId().getRoot());
                observationOpt.ifPresent(observation -> handleObservationStatement(
                    observation, clusterCompoundStatement, diagnosticReport)
                );
                handleNarrativeStatements(clusterCompoundStatement, observationComments, observationOpt.orElse(null));
            });
    }

    private void handleBatteryCompoundStatement(RCMRMT030101UK04CompoundStatement batteryCompoundStatement,
        List<Observation> observations, List<Observation> observationComments, DiagnosticReport diagnosticReport) {
        /**
         * Case 3 - CompoundStatement classCode="BATTERY"
         * For the Battery we want to generate a Panel/Battery header Observation resource using the mapping specified below
         *
         * For every CompoundStatement CLUSTER and ObservationStatement within the BATTERY CompoundStatement we process as Case 2
         and Case 1
         * with the difference that we use mutual related linkages between the Panel/Battery header Observation and the result
         Observations to link results
         * to the battery/panel header. These take the same form as described for user comment observations i.e panel battery has
         related (has-member)
         * and the result has related (derived-from)
         *
         * Every NarrativeStatement component as a direct child of the BATTERY CompoundStatement is processed.
         * USER COMMENT NarrativeStatement are processed in an identical manner to case 2 i.e. output as Observation (Comment) but
         linked
         * to the panel/battery Observation via .related. Other NarrativeStatement hace their text appended to the panel/battery
         header Observation Comment
         */

        batteryCompoundStatement.getComponent().stream()
            .filter(RCMRMT030101UK04Component02::hasCompoundStatement)
            .map(RCMRMT030101UK04Component02::getCompoundStatement)
            .filter(compoundStatement -> CLUSTER_CLASSCODE.equals(compoundStatement.getClassCode().get(0)))
            .forEach(compoundStatement -> handleClusterCompoundStatement(
                compoundStatement, observations, observationComments, diagnosticReport
            ));

        batteryCompoundStatement.getComponent().stream()
            .filter(RCMRMT030101UK04Component02::hasObservationStatement)
            .map(RCMRMT030101UK04Component02::getCompoundStatement)
            .forEach(observationStatement -> getObservationById(observations, observationStatement.getId().get(0).getRoot())
                .ifPresent(observation -> handleObservationStatement(observation, batteryCompoundStatement, diagnosticReport)));

        batteryCompoundStatement.getComponent().stream()
            .filter(RCMRMT030101UK04Component02::hasObservationStatement)
            .map(RCMRMT030101UK04Component02::getObservationStatement)
            .forEach(observationStatement -> {
                var observationOpt = getObservationById(observations, observationStatement.getId().getRoot());
                handleNarrativeStatements(batteryCompoundStatement, observationComments, observationOpt.orElse(null));
            });
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

    /**
     * Applies to Observations and Observation Comments Lists
     */
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
