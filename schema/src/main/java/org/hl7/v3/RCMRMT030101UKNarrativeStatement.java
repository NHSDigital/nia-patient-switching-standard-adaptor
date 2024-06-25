package org.hl7.v3;

import java.util.List;
import java.util.Optional;

public interface RCMRMT030101UKNarrativeStatement {
    II getId();

    void setId(II value);

    String getText();

    boolean hasText();

    void setText(String value);

    CS getStatusCode();

    void setStatusCode(CS value);

    TS getAvailabilityTime();

    boolean hasAvailabilityTime();

    void setAvailabilityTime(TS value);

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
