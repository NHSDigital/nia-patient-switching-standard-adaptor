package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKAgentDirectory {

    List<RCMRMT030101UKPart> getPart();

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
