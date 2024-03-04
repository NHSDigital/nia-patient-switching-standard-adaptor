package org.hl7.v3;

import java.util.List;

public interface UKCTMT120501UKOrganizationSDS {
    II getId();

    void setId(II value);

    List<UKCTMT120501UKServiceDeliveryLocation> getServiceDeliveryLocation();

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getDeterminerCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
