package org.hl7.v3.deprecated;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKComponent2 {
    RCMRMT030101UKAuthorise getEhrSupplyAuthorise();

    void setEhrSupplyAuthorise(RCMRMT030101UKAuthorise value);

    boolean hasEhrSupplyAuthorise();

    RCMRMT030101UKDiscontinue getEhrSupplyDiscontinue();

    void setEhrSupplyDiscontinue(RCMRMT030101UKDiscontinue value);

    boolean hasEhrSupplyDiscontinue();

    RCMRMT030101UKDispense getEhrSupplyDispense();

    void setEhrSupplyDispense(RCMRMT030101UKDispense value);

    RCMRMT030101UKPrescribe getEhrSupplyPrescribe();

    void setEhrSupplyPrescribe(RCMRMT030101UKPrescribe value);

    boolean hasEhrSupplyPrescribe();

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
