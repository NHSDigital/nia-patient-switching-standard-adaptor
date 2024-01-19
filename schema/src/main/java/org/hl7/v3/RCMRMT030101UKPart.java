package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKPart {

    RCCTMT120101UK01Agent getAgent();

    void setAgent(RCCTMT120101UK01Agent value);

    RCCTMT120101UK01AgentSDS getAgentSDS();

    void setAgentSDS(RCCTMT120101UK01AgentSDS value);

    String getType();

    void setType(String value);

    String getTypeCode();

    void setTypeCode(String value);

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
