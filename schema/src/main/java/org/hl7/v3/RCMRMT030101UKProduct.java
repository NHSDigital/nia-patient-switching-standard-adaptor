package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKProduct {
    RCMRMT030101UKManufacturedProduct getManufacturedProduct();

    void setManufacturedProduct(RCMRMT030101UKManufacturedProduct value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
