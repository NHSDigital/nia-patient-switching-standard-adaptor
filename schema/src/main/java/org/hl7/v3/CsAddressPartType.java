
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_AddressPartType.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_AddressPartType"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="DEL"/&amp;gt;
 *     &amp;lt;enumeration value="CNT"/&amp;gt;
 *     &amp;lt;enumeration value="STA"/&amp;gt;
 *     &amp;lt;enumeration value="CPA"/&amp;gt;
 *     &amp;lt;enumeration value="CTY"/&amp;gt;
 *     &amp;lt;enumeration value="ZIP"/&amp;gt;
 *     &amp;lt;enumeration value="SAL"/&amp;gt;
 *     &amp;lt;enumeration value="BNR"/&amp;gt;
 *     &amp;lt;enumeration value="BNN"/&amp;gt;
 *     &amp;lt;enumeration value="DIR"/&amp;gt;
 *     &amp;lt;enumeration value="STR"/&amp;gt;
 *     &amp;lt;enumeration value="STB"/&amp;gt;
 *     &amp;lt;enumeration value="STTYP"/&amp;gt;
 *     &amp;lt;enumeration value="ADL"/&amp;gt;
 *     &amp;lt;enumeration value="UNID"/&amp;gt;
 *     &amp;lt;enumeration value="UNIT"/&amp;gt;
 *     &amp;lt;enumeration value="CAR"/&amp;gt;
 *     &amp;lt;enumeration value="CEN"/&amp;gt;
 *     &amp;lt;enumeration value="DESC"/&amp;gt;
 *     &amp;lt;enumeration value="ADDRK"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_AddressPartType")
@XmlEnum
public enum CsAddressPartType {

    DEL,
    CNT,
    STA,
    CPA,
    CTY,
    ZIP,
    SAL,
    BNR,
    BNN,
    DIR,
    STR,
    STB,
    STTYP,
    ADL,
    UNID,
    UNIT,
    CAR,
    CEN,
    DESC,
    ADDRK;

    public String value() {
        return name();
    }

    public static CsAddressPartType fromValue(String v) {
        return valueOf(v);
    }

}
