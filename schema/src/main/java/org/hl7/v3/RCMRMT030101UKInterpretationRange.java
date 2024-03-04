package org.hl7.v3;

import java.util.List;

public interface RCMRMT030101UKInterpretationRange {

    String getText();

    void setText(String value);

    IVLPQ getValue();

    void setValue(IVLPQ value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getMoodCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
