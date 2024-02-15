package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKInFulfillmentOf02 {

    RCMRMT030101UKMedicationRef getPriorMedicationRef();

    void setPriorMedicationRef(RCMRMT030101UKMedicationRef value);

    boolean hasPriorMedicationRef();

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
