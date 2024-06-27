package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKReference {

    RCMRMT030101UKExternalDocument getReferredToExternalDocument();

    void setReferredToExternalDocument(RCMRMT030101UKExternalDocument value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
