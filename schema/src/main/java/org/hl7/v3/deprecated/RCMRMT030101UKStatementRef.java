package org.hl7.v3.deprecated;

import org.hl7.v3.II;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKStatementRef {

    II getId();

    void setId(II value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getMoodCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}