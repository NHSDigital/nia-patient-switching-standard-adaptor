package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKReplacementOf2 {

    RCMRMT030101UKCompositionRef getPriorCompositionRef();

    void setPriorCompositionRef(RCMRMT030101UKCompositionRef value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
