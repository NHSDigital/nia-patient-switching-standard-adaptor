package org.hl7.v3.deprecated;

import org.hl7.v3.BL;
import org.hl7.v3.PN;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKPerson {
    List<PN> getName();

    BL getDeceasedInd();

    void setDeceasedInd(BL value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getDeterminerCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}