
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_NullFlavor.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_NullFlavor"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="NI"/&amp;gt;
 *     &amp;lt;enumeration value="NA"/&amp;gt;
 *     &amp;lt;enumeration value="UNK"/&amp;gt;
 *     &amp;lt;enumeration value="NASK"/&amp;gt;
 *     &amp;lt;enumeration value="ASKU"/&amp;gt;
 *     &amp;lt;enumeration value="NAV"/&amp;gt;
 *     &amp;lt;enumeration value="OTH"/&amp;gt;
 *     &amp;lt;enumeration value="PINF"/&amp;gt;
 *     &amp;lt;enumeration value="NINF"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_NullFlavor")
@XmlEnum
public enum CsNullFlavor {

    NI,
    NA,
    UNK,
    NASK,
    ASKU,
    NAV,
    OTH,
    PINF,
    NINF;

    public String value() {
        return name();
    }

    public static CsNullFlavor fromValue(String v) {
        return valueOf(v);
    }

}
