package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKSequelTo {

    RCMRMT030101UKStatementRef getPriorStatementRef();

    void setPriorStatementRef(RCMRMT030101UKStatementRef value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
