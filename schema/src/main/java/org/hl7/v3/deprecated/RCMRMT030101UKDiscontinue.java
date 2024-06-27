package org.hl7.v3.deprecated;

import org.hl7.v3.CD;
import org.hl7.v3.CS;
import org.hl7.v3.II;
import org.hl7.v3.TS;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKDiscontinue {
    II getId();

    void setId(II value);

    CD getCode();

    void setCode(CD value);

    boolean hasCode();

    CS getStatusCode();

    void setStatusCode(CS value);

    TS getAvailabilityTime();

    void setAvailabilityTime(TS value);

    boolean hasAvailabilityTime();

    List<RCMRMT030101UKReversalOf> getReversalOf();

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
