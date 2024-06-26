
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_ProbabilityDistributionType.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_ProbabilityDistributionType"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="U"/&amp;gt;
 *     &amp;lt;enumeration value="N"/&amp;gt;
 *     &amp;lt;enumeration value="LN"/&amp;gt;
 *     &amp;lt;enumeration value="G"/&amp;gt;
 *     &amp;lt;enumeration value="E"/&amp;gt;
 *     &amp;lt;enumeration value="X2"/&amp;gt;
 *     &amp;lt;enumeration value="T"/&amp;gt;
 *     &amp;lt;enumeration value="F"/&amp;gt;
 *     &amp;lt;enumeration value="B"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_ProbabilityDistributionType")
@XmlEnum
public enum CsProbabilityDistributionType {

    U("U"),
    N("N"),
    LN("LN"),
    G("G"),
    E("E"),
    @XmlEnumValue("X2")
    X_2("X2"),
    T("T"),
    F("F"),
    B("B");
    private final String value;

    CsProbabilityDistributionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CsProbabilityDistributionType fromValue(String v) {
        for (CsProbabilityDistributionType c: CsProbabilityDistributionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
