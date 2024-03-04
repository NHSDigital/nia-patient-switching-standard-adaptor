package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKComponent6 {
    RCMRMT030101UKStatementRef getStatementRef();

    void setStatementRef(RCMRMT030101UKStatementRef value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
