package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKEhrFolder {
    II getId();

    void setId(II value);

    CS getStatusCode();

    void setStatusCode(CS value);

    IVLTS getEffectiveTime();

    void setEffectiveTime(IVLTS value);

    TS getAvailabilityTime();

    void setAvailabilityTime(TS value);

    RCMRMT030101UKAuthor2 getAuthor();

    void setAuthor(RCMRMT030101UKAuthor2 value);

    RCMRMT030101UKResponsibleParty getResponsibleParty();

    void setResponsibleParty(RCMRMT030101UKResponsibleParty value);

    List<RCMRMT030101UKComponent3> getComponent();

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getMoodCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
