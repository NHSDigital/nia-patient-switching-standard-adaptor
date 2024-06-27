package org.hl7.v3.deprecated;

import org.hl7.v3.IVLTS;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKParticipant {

    IVLTS getTime();

    void setTime(IVLTS value);

    RCMRMT030101UKAgentRef getAgentRef();

    void setAgentRef(RCMRMT030101UKAgentRef value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getContextControlCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);

    boolean hasNullFlavour();
}
