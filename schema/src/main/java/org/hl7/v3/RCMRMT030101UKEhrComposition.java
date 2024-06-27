package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKEhrComposition {

    II getId();

    void setId(II value);

    CD getCode();

    void setCode(CD value);

    CS getStatusCode();

    void setStatusCode(CS value);

    IVLTS getEffectiveTime();

    void setEffectiveTime(IVLTS value);

    TS getAvailabilityTime();

    void setAvailabilityTime(TS value);

    boolean hasAvailabilityTime();

    RCMRMT030101UKAuthor getAuthor();

    void setAuthor(RCMRMT030101UKAuthor value);

    boolean hasAuthor();

    RCMRMT030101UKLocation getLocation();

    void setLocation(RCMRMT030101UKLocation value);

    List<RCMRMT030101UKParticipant2> getParticipant2();

    boolean hasParticipant2();

    List<RCMRMT030101UKComponent4> getComponent();

    void setComponent(List<RCMRMT030101UKComponent4> value);

    RCMRMT030101UKReplacementOf2 getReplacementOf();

    void setReplacementOf(RCMRMT030101UKReplacementOf2 value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getMoodCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
