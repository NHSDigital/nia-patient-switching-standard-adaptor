package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKConditionNamed {
    RCMRMT030101UKStatementRef getNamedStatementRef();

    void setNamedStatementRef(RCMRMT030101UKStatementRef value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    boolean isInversionInd();

    void setInversionInd(Boolean value);

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
