package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKInFulfillmentOf2 {

    RCMRMT030101UKEhrRequest getPriorEhrRequest();

    void setPriorEhrRequest(RCMRMT030101UKEhrRequest value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
