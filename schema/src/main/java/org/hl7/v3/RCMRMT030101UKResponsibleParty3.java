package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKResponsibleParty3 {

    CV getModeCode();

    void setModeCode(CV value);

    RCMRMT030101UKAgentRef getAgentRef();

    void setAgentRef(RCMRMT030101UKAgentRef value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
