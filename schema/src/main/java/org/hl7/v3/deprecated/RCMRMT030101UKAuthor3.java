package org.hl7.v3.deprecated;

import org.hl7.v3.CV;
import org.hl7.v3.ED;
import org.hl7.v3.TS;
import org.hl7.v3.UKCTMT120501UKAgentOrgSDS;

import java.util.List;

@Deprecated
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
