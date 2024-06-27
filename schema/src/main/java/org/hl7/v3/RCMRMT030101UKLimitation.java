package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKLimitation {

    RCMRMT030101UKEhrExtractSpecification getLimitingEhrExtractSpecification();

    void setLimitingEhrExtractSpecification(RCMRMT030101UKEhrExtractSpecification value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    boolean isInversionInd();

    void setInversionInd(Boolean value);

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
