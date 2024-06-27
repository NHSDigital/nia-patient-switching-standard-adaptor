package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKAuthorise {

    II getId();

    void setId(II value);

    boolean hasId();

    CV getCode();

    void setCode(CV value);

    boolean hasCode();

    CS getStatusCode();

    void setStatusCode(CS value);

    boolean hasStatusCode();

    IVLTS getEffectiveTime();

    void setEffectiveTime(IVLTS value);

    boolean hasEffectiveTime();

    TS getAvailabilityTime();

    void setAvailabilityTime(TS value);

    boolean hasAvailabilityTime();

    INT getRepeatNumber();

    void setRepeatNumber(INT value);

    boolean hasRepeatNumber();

    PQ getQuantity();

    void setQuantity(PQ value);

    boolean hasQuantity();

    List<RCMRMT030101UKPredecessor> getPredecessor();

    RCMRMT030101UKPredecessor getPredecessorFirstRep();

    boolean hasPredecessor();

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
