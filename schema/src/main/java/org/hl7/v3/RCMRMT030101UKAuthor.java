package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKAuthor {

    TS getTime();

    void setTime(TS value);

    boolean hasTime();

    CV getSignatureCode();

    void setSignatureCode(CV value);

    ED getSignatureText();

    void setSignatureText(ED value);

    RCMRMT030101UKAgentRef getAgentRef();

    void setAgentRef(RCMRMT030101UKAgentRef value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getContextControlCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
