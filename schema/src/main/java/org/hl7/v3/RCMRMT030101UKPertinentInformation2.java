package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKPertinentInformation2 {

    RCMRMT030101UKSupplyAnnotation getPertinentSupplyAnnotation();

    void setPertinentSupplyAnnotation(RCMRMT030101UKSupplyAnnotation value);

    boolean hasPertinentSupplyAnnotation();

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
