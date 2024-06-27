package org.hl7.v3;

import org.hl7.v3.deprecated.RCMRMT030101UKObservationStatement;
import org.hl7.v3.deprecated.RCMRMT030101UKRequestStatement;

public interface LinkableComponent {
    RCMRMT030101UKObservationStatement getObservationStatement();
    RCMRMT030101UKRequestStatement getRequestStatement();
}
