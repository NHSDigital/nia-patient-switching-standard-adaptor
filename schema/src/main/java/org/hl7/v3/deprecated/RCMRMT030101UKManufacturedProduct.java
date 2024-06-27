package org.hl7.v3.deprecated;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKManufacturedProduct {
    RCMRMT030101UKMaterial getManufacturedMaterial();

    void setManufacturedMaterial(RCMRMT030101UKMaterial value);

    boolean hasManufacturedMaterial();

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
