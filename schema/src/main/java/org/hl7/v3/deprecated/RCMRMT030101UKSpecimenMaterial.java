package org.hl7.v3.deprecated;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKSpecimenMaterial {
    String getDesc();

    void setDesc(String value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getDeterminerCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}