package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKPersonalRelationship {
    CE getCode();

    void setCode(CE value);

    RCMRMT030101UKPerson getRelationshipHolder();

    void setRelationshipHolder(RCMRMT030101UKPerson value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
