package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKPertinentInformation {
    RCMRMT030101UKMedicationDosage getPertinentMedicationDosage();

    void setPertinentMedicationDosage(RCMRMT030101UKMedicationDosage value);

    boolean hasPertinentMedicationDosage();

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
