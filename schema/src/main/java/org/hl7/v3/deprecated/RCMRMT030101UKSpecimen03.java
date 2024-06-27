package org.hl7.v3.deprecated;

import java.util.List;

@Deprecated
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
