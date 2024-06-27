package org.hl7.v3.deprecated;

import org.hl7.v3.II;
import org.hl7.v3.IVLTS;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKSpecimenRole {
    List<II> getId();

    IVLTS getEffectiveTime();

    void setEffectiveTime(IVLTS value);

    RCMRMT030101UKSpecimenMaterial getSpecimenSpecimenMaterial();

    void setSpecimenSpecimenMaterial(RCMRMT030101UKSpecimenMaterial value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
