
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_TelecommunicationAddressUse.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_TelecommunicationAddressUse"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="H"/&amp;gt;
 *     &amp;lt;enumeration value="HP"/&amp;gt;
 *     &amp;lt;enumeration value="HV"/&amp;gt;
 *     &amp;lt;enumeration value="WP"/&amp;gt;
 *     &amp;lt;enumeration value="AS"/&amp;gt;
 *     &amp;lt;enumeration value="EC"/&amp;gt;
 *     &amp;lt;enumeration value="PG"/&amp;gt;
 *     &amp;lt;enumeration value="MC"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_TelecommunicationAddressUse")
@XmlEnum
public enum CsTelecommunicationAddressUse {

    H,
    HP,
    HV,
    WP,
    AS,
    EC,
    PG,
    MC;

    public String value() {
        return name();
    }

    public static CsTelecommunicationAddressUse fromValue(String v) {
        return valueOf(v);
    }

}
