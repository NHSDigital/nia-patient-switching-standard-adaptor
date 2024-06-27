package org.hl7.v3.deprecated;

import org.hl7.v3.*;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKDispense {
    II getId();

    void setId(II value);

    CV getCode();

    void setCode(CV value);

    CS getStatusCode();

    void setStatusCode(CS value);

    TS getAvailabilityTime();

    void setAvailabilityTime(TS value);

    PQ getQuantity();

    void setQuantity(PQ value);

    RCMRMT030101UKInFulfillmentOf getInFulfillmentOf();

    void setInFulfillmentOf(RCMRMT030101UKInFulfillmentOf value);

    RCMRMT030101UKPerformer getPerformer();

    void setPerformer(RCMRMT030101UKPerformer value);

    RCMRMT030101UKProduct getConsumable();

    void setConsumable(RCMRMT030101UKProduct value);

    List<RCMRMT030101UKPertinentInformation2> getPertinentInformation();

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getMoodCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
