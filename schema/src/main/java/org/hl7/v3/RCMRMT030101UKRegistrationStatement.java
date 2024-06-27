package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKRegistrationStatement {
    II getId();

    void setId(II value);

    CE getCode();

    void setCode(CE value);

    CS getStatusCode();

    void setStatusCode(CS value);

    IVLTS getEffectiveTime();

    void setEffectiveTime(IVLTS value);

    TS getAvailabilityTime();

    void setAvailabilityTime(TS value);

    RCMRMT030101UKResponsibleParty2 getResponsibleParty();

    void setResponsibleParty(RCMRMT030101UKResponsibleParty2 value);

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
