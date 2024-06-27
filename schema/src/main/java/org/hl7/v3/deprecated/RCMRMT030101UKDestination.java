package org.hl7.v3.deprecated;

import org.hl7.v3.UKCTMT120501UKAgentOrgSDS;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKDestination {
    UKCTMT120501UKAgentOrgSDS getAgentOrgSDS();

    void setAgentOrgSDS(UKCTMT120501UKAgentOrgSDS value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
