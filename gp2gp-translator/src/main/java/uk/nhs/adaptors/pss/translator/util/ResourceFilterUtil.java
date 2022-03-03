package uk.nhs.adaptors.pss.translator.util;

import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;

public class ResourceFilterUtil {

    private static final String IMMUNIZATION_SNOMED_CODE = "2.16.840.1.113883.2.1.3.2.3.15";
    private static final String BATTERY_VALUE = "BATTERY";

    public static boolean hasReferredToExternalDocument(RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        return narrativeStatement.getReference()
            .stream()
            .anyMatch(reference -> reference.getReferredToExternalDocument() != null);
    }

    public static boolean hasImmunizationCode(RCMRMT030101UK04ObservationStatement observationStatement) {
        String snomedCode = observationStatement.getCode().getCodeSystem();

        return IMMUNIZATION_SNOMED_CODE.equals(snomedCode); // TODO: Implement filtering with snomed DB (NIAD-1947)
    }

    public static boolean hasBloodPressure(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null && BATTERY_VALUE.equals(compoundStatement.getClassCode().get(0))
            && BloodPressureValidatorUtil.containsValidBloodPressureTriple(compoundStatement);
    }
}
