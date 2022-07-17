package uk.nhs.adaptors.pss.translator.util;

import org.hl7.v3.RCMRMT030101UK04ObservationStatement;

public interface ImmunizationChecker {
    boolean isImmunization(RCMRMT030101UK04ObservationStatement observationStatement);
}
