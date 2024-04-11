package org.hl7.v3;

import java.util.List;

public interface UKCTMT120501UKPlace {
    PN getName();

    void setName(PN value);

    TEL getTelecom();

    void setTelecom(TEL value);

    AD getAddr();

    void setAddr(AD value);

    String getType();

    void setType(String value);

    List<String> getClassCode();

    List<String> getDeterminerCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
