package org.hl7.v3.deprecated;

import org.hl7.v3.CV;
import org.hl7.v3.II;
import org.hl7.v3.IVLTS;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKEhrExtractSpecification {
    II getId();

    void setId(II value);

    CV getCode();

    void setCode(CV value);

    IVLTS getEffectiveTime();

    void setEffectiveTime(IVLTS value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getMoodCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
