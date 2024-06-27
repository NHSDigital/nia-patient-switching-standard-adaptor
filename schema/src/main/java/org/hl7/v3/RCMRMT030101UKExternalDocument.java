package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKExternalDocument {

    II getId();

    void setId(II value);

    CD getCode();

    boolean hasCode();

    void setCode(CD value);

    ED getText();

    boolean hasText();

    void setText(ED value);

    IVLTS getEffectiveTime();

    void setEffectiveTime(IVLTS value);

    II getSetId();

    void setSetId(II value);

    INT getVersionNumber();

    void setVersionNumber(INT value);

    List<RCMRMT030101UKAuthor4> getAuthor();

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getMoodCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
