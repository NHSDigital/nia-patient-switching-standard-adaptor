package org.hl7.v3.deprecated;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKReferenceRange {
    RCMRMT030101UKInterpretationRange getReferenceInterpretationRange();

    void setReferenceInterpretationRange(RCMRMT030101UKInterpretationRange value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}