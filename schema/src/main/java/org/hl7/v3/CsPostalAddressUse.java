
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_PostalAddressUse.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_PostalAddressUse"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="PHYS"/&amp;gt;
 *     &amp;lt;enumeration value="PST"/&amp;gt;
 *     &amp;lt;enumeration value="TMP"/&amp;gt;
 *     &amp;lt;enumeration value="BAD"/&amp;gt;
 *     &amp;lt;enumeration value="H"/&amp;gt;
 *     &amp;lt;enumeration value="HP"/&amp;gt;
 *     &amp;lt;enumeration value="HV"/&amp;gt;
 *     &amp;lt;enumeration value="WP"/&amp;gt;
 *     &amp;lt;enumeration value="ABC"/&amp;gt;
 *     &amp;lt;enumeration value="SYL"/&amp;gt;
 *     &amp;lt;enumeration value="IDE"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_PostalAddressUse")
@XmlEnum
public enum CsPostalAddressUse {

    PHYS,
    PST,
    TMP,
    BAD,
    H,
    HP,
    HV,
    WP,
    ABC,
    SYL,
    IDE;

    public String value() {
        return name();
    }

    public static CsPostalAddressUse fromValue(String v) {
        return valueOf(v);
    }

}
