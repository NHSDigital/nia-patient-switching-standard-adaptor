package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKAuthor3 {

    TS getTime();

    boolean hasTime();

    void setTime(TS value);

    CV getSignatureCode();

    void setSignatureCode(CV value);

    ED getSignatureText();

    void setSignatureText(ED value);

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
