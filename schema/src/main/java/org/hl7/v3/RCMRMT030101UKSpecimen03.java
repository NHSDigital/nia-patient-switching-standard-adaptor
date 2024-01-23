package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKSpecimen03 {
    RCMRMT030101UKSpecimenRole getSpecimenRole();

    void setSpecimenRole(RCMRMT030101UKSpecimenRole value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
