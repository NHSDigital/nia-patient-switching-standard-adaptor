package uk.nhs.adaptors.pss.translator.util;

import org.hl7.v3.RCMRMT030101UKObservationStatement;

public interface ImmunizationChecker {
    boolean isImmunization(RCMRMT030101UKObservationStatement observationStatement);
}
