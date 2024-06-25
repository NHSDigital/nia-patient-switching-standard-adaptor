package org.hl7.v3;

import java.util.List;
import java.util.Optional;

public interface RCMRMT030101UKLinkSet {
    II getId();

    void setId(II value);

    CD getCode();

    void setCode(CD value);

    boolean hasCode();

    CS getStatusCode();

    void setStatusCode(CS value);

    IVLTS getEffectiveTime();

    void setEffectiveTime(IVLTS value);

    TS getAvailabilityTime();

    void setAvailabilityTime(TS value);

    List<RCMRMT030101UKComponent6> getComponent();

    RCMRMT030101UKConditionNamed getConditionNamed();

    void setConditionNamed(RCMRMT030101UKConditionNamed value);

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

    Optional<CV> getConfidentialityCode();

    void setConfidentialityCode(CV confidentialityCode);
}
