package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKLocatedEntity {

    CV getCode();

    void setCode(CV value);

    RCMRMT030101UK04Place getLocatedPlace();

    void setLocatedPlace(RCMRMT030101UK04Place value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
