package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKComponent3 {
    RCMRMT030101UKEhrComposition getEhrComposition();

    void setEhrComposition(RCMRMT030101UKEhrComposition value);

    boolean hasEhrComposition();

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
