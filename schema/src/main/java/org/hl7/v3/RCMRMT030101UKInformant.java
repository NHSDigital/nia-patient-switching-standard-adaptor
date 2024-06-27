package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKInformant {

    RCMRMT030101UKInformantRole getInformantRole();

    void setInformantRole(RCMRMT030101UKInformantRole value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getContextControlCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
