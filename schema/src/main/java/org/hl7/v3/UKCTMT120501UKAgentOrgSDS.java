package org.hl7.v3;

import java.util.List;

public interface UKCTMT120501UKAgentOrgSDS {
    II getId();

    void setId(II value);

    CV getCode();

    void setCode(CV value);

    UKCTMT120501UKOrganizationSDS getAgentOrganizationSDS();

    void setAgentOrganizationSDS(UKCTMT120501UKOrganizationSDS value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
