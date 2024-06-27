package org.hl7.v3.deprecated;

import org.hl7.v3.*;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKMedicationStatement {

    II getId();

    void setId(II value);

    CS getStatusCode();

    void setStatusCode(CS value);

    IVLTS getEffectiveTime();

    void setEffectiveTime(IVLTS value);

    boolean hasEffectiveTime();

    TS getAvailabilityTime();

    void setAvailabilityTime(TS value);

    CV getPriorityCode();

    void setPriorityCode(CV value);

    List<RCMRMT030101UKConsumable> getConsumable();

    boolean hasConsumable();

    List<RCMRMT030101UKComponent2> getComponent();

    boolean hasComponent();

    List<RCMRMT030101UKPertinentInformation> getPertinentInformation();

    boolean hasPertinentInformation();

    List<RCMRMT030101UKInformant> getInformant();

    List<RCMRMT030101UKParticipant> getParticipant();

    boolean hasParticipant();

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
