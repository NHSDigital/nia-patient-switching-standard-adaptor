package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKMaterial {

    CE getCode();

    void setCode(CE value);

    boolean hasCode();

    PQ getQuantity();

    void setQuantity(PQ value);

    TS getExpirationTime();

    void setExpirationTime(TS value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getDeterminerCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
