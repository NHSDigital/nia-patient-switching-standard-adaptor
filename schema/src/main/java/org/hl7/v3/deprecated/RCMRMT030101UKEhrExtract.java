package org.hl7.v3.deprecated;

import org.hl7.v3.CS;
import org.hl7.v3.II;
import org.hl7.v3.TS;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKEhrExtract {

    II getId();

    void setId(II value);

    CS getStatusCode();

    void setStatusCode(CS value);

    TS getAvailabilityTime();

    void setAvailabilityTime(TS value);

    RCMRMT030101UKPatientSubject getRecordTarget();

    void setRecordTarget(RCMRMT030101UKPatientSubject value);

    RCMRMT030101UKAuthor3 getAuthor();

    boolean hasAuthor();

    void setAuthor(RCMRMT030101UKAuthor3 value);

    RCMRMT030101UKDestination getDestination();

    void setDestination(RCMRMT030101UKDestination value);

    List<RCMRMT030101UKComponent> getComponent();

    RCMRMT030101UKInFulfillmentOf2 getInFulfillmentOf();

    void setInFulfillmentOf(RCMRMT030101UKInFulfillmentOf2 value);

    RCMRMT030101UKLimitation getLimitation();

    void setLimitation(RCMRMT030101UKLimitation value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getMoodCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
