package org.hl7.v3.deprecated;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKSupplyAnnotation {

    String getText();

    void setText(String value);

    boolean hasText();

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getMoodCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}