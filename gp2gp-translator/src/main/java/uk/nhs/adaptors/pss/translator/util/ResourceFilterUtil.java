package uk.nhs.adaptors.pss.translator.util;

import java.util.List;

import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;

public class ResourceFilterUtil {
    private static final List<String> ALLERGY_CODES = List.of("SN53.00", "14L..00");
    private static final String IMMUNIZATION_SNOMED_CODE = "2.16.840.1.113883.2.1.3.2.3.15";
    private static final String ALLERGY_SNOMED_CODE = "2.16.840.1.113883.2.1.6.2";
    private static final String PATHOLOGY_CODE = "16488004";
    private static final String BATTERY_VALUE = "BATTERY";
    private static final String CLUSTER_VALUE = "CLUSTER";

    public static boolean hasReferredToExternalDocument(RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        return narrativeStatement.getReference()
            .stream()
            .anyMatch(reference -> reference.getReferredToExternalDocument() != null);
    }

    public static boolean isImmunization(RCMRMT030101UK04ObservationStatement observationStatement) {
        // TODO: Implement filtering with snomed DB (NIAD-1947)
        return observationStatement != null && IMMUNIZATION_SNOMED_CODE.equals(observationStatement.getCode().getCodeSystem());
    }

    public static boolean isBloodPressure(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null && BATTERY_VALUE.equals(compoundStatement.getClassCode().get(0))
            && BloodPressureValidatorUtil.containsValidBloodPressureTriple(compoundStatement);
    }

    public static boolean isAllergyIntolerance(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null
            && ALLERGY_CODES.contains(compoundStatement.getCode().getCode())
            && ALLERGY_SNOMED_CODE.equals(compoundStatement.getCode().getCodeSystem())
            && compoundStatement.getComponent().size() == 1
            && compoundStatement.getComponent().get(0).hasObservationStatement();
    }

    public static boolean isDiagnosticReport(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null
            && CLUSTER_VALUE.equals(compoundStatement.getClassCode().get(0))
            && PATHOLOGY_CODE.equals(compoundStatement.getCode().getCode());
    }

    public static boolean isTemplate(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null
            && !isBloodPressure(compoundStatement)
            && !isDiagnosticReport(compoundStatement)
            && List.of(BATTERY_VALUE, CLUSTER_VALUE).contains(compoundStatement.getClassCode().get(0));
    }
}
