package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKConsumable {
    RCMRMT030101UKManufacturedProduct getManufacturedProduct();

    void setManufacturedProduct(RCMRMT030101UKManufacturedProduct value);

    boolean hasManufacturedProduct();

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
