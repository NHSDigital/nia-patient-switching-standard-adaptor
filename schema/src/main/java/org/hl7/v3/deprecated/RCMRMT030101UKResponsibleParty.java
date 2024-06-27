package org.hl7.v3.deprecated;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKResponsibleParty {

    RCMRMT030101UKAgentDirectory getAgentDirectory();

    void setAgentDirectory(RCMRMT030101UKAgentDirectory value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
