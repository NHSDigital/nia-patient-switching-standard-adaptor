package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKObservationStatement {

    II getId();

    void setId(II value);

    CD getCode();

    void setCode(CD value);

    boolean hasCode();

    CS getStatusCode();

    void setStatusCode(CS value);

    IVLTS getEffectiveTime();

    void setEffectiveTime(IVLTS value);

    boolean hasEffectiveTime();

    TS getAvailabilityTime();

    void setAvailabilityTime(TS value);

    boolean hasAvailabilityTime();

    CV getPriorityCode();

    void setPriorityCode(CV value);

    CV getUncertaintyCode();

    void setUncertaintyCode(CV value);

    boolean hasUncertaintyCode();

    CV getInterpretationCode();

    void setInterpretationCode(CV value);

    RCMRMT030101UKSubject getSubject();

    void setSubject(RCMRMT030101UKSubject value);

    List<RCMRMT030101UKSpecimen> getSpecimen();

    List<RCMRMT030101UKPertinentInformation02> getPertinentInformation();

    List<RCMRMT030101UKReferenceRange> getReferenceRange();

    List<RCMRMT030101UKInformant> getInformant();

    List<RCMRMT030101UKParticipant> getParticipant();

    List<RCMRMT030101UKReplacementOf> getReplacementOf();

    List<RCMRMT030101UKReason> getReason();

    List<RCMRMT030101UKReference> getReference();

    List<RCMRMT030101UKSequelTo> getSequelTo();

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getMoodCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);

    Object getValue();

    void setValue(Object value);

    boolean hasValue();
}
