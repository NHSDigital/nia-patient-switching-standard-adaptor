package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKReversalOf {
    RCMRMT030101UKMedicationRef getPriorMedicationRef();

    void setPriorMedicationRef(RCMRMT030101UKMedicationRef value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
