package org.hl7.v3.deprecated;

import org.hl7.v3.*;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKCompoundStatement {

    List<II> getId();

    CD getCode();

    void setCode(CD value);

    boolean hasCode();

    CS getStatusCode();

    void setStatusCode(CS value);

    IVLTS getEffectiveTime();

    boolean hasEffectiveTime();

    void setEffectiveTime(IVLTS value);

    TS getAvailabilityTime();

    void setAvailabilityTime(TS value);

    CV getPriorityCode();

    void setPriorityCode(CV value);

    CV getUncertaintyCode();

    void setUncertaintyCode(CV value);

    List<RCMRMT030101UKSpecimen03> getSpecimen();

    List<RCMRMT030101UKComponent02> getComponent();

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
}
