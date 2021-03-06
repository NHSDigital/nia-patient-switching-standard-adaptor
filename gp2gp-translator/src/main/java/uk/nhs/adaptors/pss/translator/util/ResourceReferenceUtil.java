package uk.nhs.adaptors.pss.translator.util;

import static uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil.isAllergyIntolerance;
import static uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil.isBloodPressure;
import static uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil.isDiagnosticReport;
import static uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil.isDocumentReference;
import static uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil.isTemplate;

import java.util.List;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04LinkSet;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.hl7.v3.RCMRMT030101UK04RequestStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResourceReferenceUtil {
    private static final String MEDICATION_STATEMENT_REFERENCE = "%s-MS";
    private static final String QUESTIONNAIRE_ID = "%s-QRSP";
    private static final String OBSERVATION_REFERENCE = "Observation/%s";
    private static final String QUESTIONNAIRE_REFERENCE = "QuestionnaireResponse/%s";

    private final DatabaseImmunizationChecker immunizationChecker;

    public void extractChildReferencesFromEhrComposition(RCMRMT030101UK04EhrComposition ehrComposition,
        List<Reference> entryReferences) {

        ehrComposition.getComponent().forEach(component -> {
            addPlanStatementEntry(component.getPlanStatement(), entryReferences);
            addRequestStatementEntry(component.getRequestStatement(), entryReferences);
            addLinkSetEntry(component.getLinkSet(), entryReferences);
            addObservationStatementEntry(component.getObservationStatement(), entryReferences, null);
            addNarrativeStatementEntry(component.getNarrativeStatement(), entryReferences);
            addMedicationEntry(component.getMedicationStatement(), entryReferences);
            extractChildReferencesFromCompoundStatement(component.getCompoundStatement(), entryReferences);
        });
    }

    public void extractChildReferencesFromCompoundStatement(RCMRMT030101UK04CompoundStatement compoundStatement,
        List<Reference> entryReferences) {
        if (compoundStatement != null) {
            if (isDiagnosticReport(compoundStatement)) {
                addDiagnosticReportEntry(compoundStatement, entryReferences);
            } else {
                if (isTemplate(compoundStatement)) {
                    addTemplateEntry(compoundStatement, entryReferences);
                }

                compoundStatement.getComponent().forEach(component -> {
                    addObservationStatementEntry(
                        component.getObservationStatement(), entryReferences, compoundStatement);
                    addPlanStatementEntry(component.getPlanStatement(), entryReferences);
                    addRequestStatementEntry(component.getRequestStatement(), entryReferences);
                    addLinkSetEntry(component.getLinkSet(), entryReferences);
                    addMedicationEntry(component.getMedicationStatement(), entryReferences);

                    if (isNotIgnoredResource(compoundStatement, entryReferences)) {
                        addNarrativeStatementEntry(component.getNarrativeStatement(), entryReferences);
                    }

                    extractChildReferencesFromCompoundStatement(component.getCompoundStatement(), entryReferences);
                });
            }
        }
    }

    public void extractChildReferencesFromTemplate(RCMRMT030101UK04CompoundStatement compoundStatement,
        List<Reference> entryReferences) {
        compoundStatement.getComponent().forEach(component -> {
            addObservationStatementEntry(component.getObservationStatement(), entryReferences, compoundStatement);
            addPlanStatementEntry(component.getPlanStatement(), entryReferences);
            addRequestStatementEntry(component.getRequestStatement(), entryReferences);
            addLinkSetEntry(component.getLinkSet(), entryReferences);
            addMedicationEntry(component.getMedicationStatement(), entryReferences);
            addNarrativeStatementEntry(component.getNarrativeStatement(), entryReferences);
            extractChildReferencesFromCompoundStatement(component.getCompoundStatement(), entryReferences);
        });
    }

    private static boolean isNotIgnoredResource(RCMRMT030101UK04CompoundStatement compoundStatement, List<Reference> entryReferences) {
        var references = entryReferences.stream()
            .map(Reference::getReference)
            .toList();

        return compoundStatement == null
            || references.contains(QUESTIONNAIRE_ID.formatted(QUESTIONNAIRE_REFERENCE.formatted(
            compoundStatement.getId().get(0).getRoot())))
            || !references.contains(OBSERVATION_REFERENCE.formatted(compoundStatement.getId().get(0).getRoot()));
    }

    private static void addTemplateEntry(RCMRMT030101UK04CompoundStatement compoundStatement, List<Reference> entryReferences) {
        entryReferences.add(createResourceReference(ResourceType.QuestionnaireResponse.name(),
            QUESTIONNAIRE_ID.formatted(compoundStatement.getId().get(0).getRoot())));
        entryReferences.add(createResourceReference(ResourceType.Observation.name(),
            compoundStatement.getId().get(0).getRoot()));
    }

    private void addObservationStatementEntry(RCMRMT030101UK04ObservationStatement observationStatement,
        List<Reference> entryReferences, RCMRMT030101UK04CompoundStatement compoundStatement) {
        if (observationStatement != null && isNotIgnoredResource(compoundStatement, entryReferences)) {
            if (isBloodPressure(compoundStatement)) {
                addBloodPressureEntry(compoundStatement, entryReferences);
            } else if (isAllergyIntolerance(compoundStatement)) {
                addAllergyIntoleranceEntry(compoundStatement, entryReferences);
            } else if (observationStatement.hasCode() && immunizationChecker.isImmunization(observationStatement)) {
                addImmunizationEntry(observationStatement, entryReferences);
            } else {
                addUncategorisedObservationEntry(observationStatement, entryReferences);
            }
        }
    }

    private static void addAllergyIntoleranceEntry(RCMRMT030101UK04CompoundStatement compoundStatement,
        List<Reference> entryReferences) {
        entryReferences.add(createResourceReference(ResourceType.AllergyIntolerance.name(), compoundStatement.getId().get(0).getRoot()));
    }

    private static void addDiagnosticReportEntry(RCMRMT030101UK04CompoundStatement compoundStatement, List<Reference> entryReferences) {
        entryReferences.add(createResourceReference(ResourceType.DiagnosticReport.name(), compoundStatement.getId().get(0).getRoot()));
    }

    private static void addMedicationEntry(RCMRMT030101UK04MedicationStatement medicationStatement, List<Reference> entryReferences) {
        if (medicationStatement != null) {
            medicationStatement.getComponent().forEach(component -> {
                if (component.hasEhrSupplyAuthorise()) {
                    var id = component.getEhrSupplyAuthorise().getId().getRoot();
                    entryReferences.add(createResourceReference(ResourceType.MedicationStatement.name(),
                        MEDICATION_STATEMENT_REFERENCE.formatted(id)));
                    entryReferences.add(createResourceReference(ResourceType.MedicationRequest.name(), id));
                } else if (component.hasEhrSupplyPrescribe()) {
                    entryReferences.add(createResourceReference(ResourceType.MedicationRequest.name(),
                        component.getEhrSupplyPrescribe().getId().getRoot()));
                }
            });
        }
    }

    private static void addBloodPressureEntry(RCMRMT030101UK04CompoundStatement compoundStatement, List<Reference> entryReferences) {
        entryReferences.add(createResourceReference(ResourceType.Observation.name(), compoundStatement.getId().get(0).getRoot()));
    }

    private static void addImmunizationEntry(RCMRMT030101UK04ObservationStatement observationStatement, List<Reference> entryReferences) {
        entryReferences.add(createResourceReference(ResourceType.Immunization.name(), observationStatement.getId().getRoot()));
    }

    private static void addUncategorisedObservationEntry(RCMRMT030101UK04ObservationStatement observationStatement,
        List<Reference> entryReferences) {
        entryReferences.add(createResourceReference(ResourceType.Observation.name(), observationStatement.getId().getRoot()));
    }

    private static void addPlanStatementEntry(RCMRMT030101UK04PlanStatement planStatement, List<Reference> entryReferences) {
        if (planStatement != null) {
            entryReferences.add(createResourceReference(ResourceType.ProcedureRequest.name(), planStatement.getId().getRoot()));
        }
    }

    private static void addRequestStatementEntry(RCMRMT030101UK04RequestStatement requestStatement, List<Reference> entryReferences) {
        if (requestStatement != null) {
            entryReferences.add(createResourceReference(ResourceType.ReferralRequest.name(), requestStatement.getId().get(0).getRoot()));
        }
    }

    private static void addNarrativeStatementEntry(RCMRMT030101UK04NarrativeStatement narrativeStatement, List<Reference> entryReferences) {
        if (narrativeStatement != null) {
            if (isDocumentReference(narrativeStatement)) {
                entryReferences.add(createResourceReference(ResourceType.DocumentReference.name(),
                    narrativeStatement.getReference().get(0).getReferredToExternalDocument().getId().getRoot()));
            } else {
                entryReferences.add(createResourceReference(ResourceType.Observation.name(), narrativeStatement.getId().getRoot()));
            }
        }
    }

    private static void addLinkSetEntry(RCMRMT030101UK04LinkSet linkSet, List<Reference> entryReferences) {
        if (linkSet != null) {
            entryReferences.add(createResourceReference(ResourceType.Condition.name(), linkSet.getId().getRoot()));
        }
    }

    private static Reference createResourceReference(String resourceName, String id) {
        return new Reference(new IdType(resourceName, id));
    }
}
