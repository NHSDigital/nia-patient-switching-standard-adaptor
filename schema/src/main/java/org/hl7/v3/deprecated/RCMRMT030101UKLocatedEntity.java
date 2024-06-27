package org.hl7.v3.deprecated;

import org.hl7.v3.CV;
import org.hl7.v3.RCMRMT030101UKPlace;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKLocatedEntity {

    CV getCode();

    void setCode(CV value);

    RCMRMT030101UKPlace getLocatedPlace();

    void setLocatedPlace(RCMRMT030101UKPlace value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
