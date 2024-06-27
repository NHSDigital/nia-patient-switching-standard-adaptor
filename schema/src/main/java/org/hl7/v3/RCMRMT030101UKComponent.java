package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKComponent {

    RCMRMT030101UKEhrFolder getEhrFolder();

    void setEhrFolder(RCMRMT030101UKEhrFolder value);

    boolean hasEhrFolder();

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
