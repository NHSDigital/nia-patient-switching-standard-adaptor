package uk.nhs.adaptors.pss.translator.util;

import org.hl7.v3.CD;

public interface iImmunizationChecker {
    boolean isImmunization(String code);
}
