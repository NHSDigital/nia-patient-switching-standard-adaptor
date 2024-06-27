package org.hl7.v3.deprecated;

import org.hl7.v3.INT;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKPertinentInformation02 {
    INT getSequenceNumber();

    void setSequenceNumber(INT value);

    RCMRMT030101UKAnnotation getPertinentAnnotation();

    void setPertinentAnnotation(RCMRMT030101UKAnnotation value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
