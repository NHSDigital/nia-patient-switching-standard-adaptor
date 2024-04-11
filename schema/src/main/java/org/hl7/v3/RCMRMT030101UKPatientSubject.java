package org.hl7.v3;

import java.util.List;


public interface RCMRMT030101UKPatientSubject {

    RCMRMT030101UKPatient getPatient();

    void setPatient(RCMRMT030101UKPatient value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);

}
