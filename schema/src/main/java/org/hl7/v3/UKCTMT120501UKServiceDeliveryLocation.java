package org.hl7.v3;

import java.util.List;

public interface UKCTMT120501UKServiceDeliveryLocation {
    CV getCode();

    void setCode(CV value);

    UKCTMT120501UKPlace getLocation();

    void setLocation(UKCTMT120501UKPlace value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
