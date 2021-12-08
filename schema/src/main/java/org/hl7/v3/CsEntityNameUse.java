
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_EntityNameUse.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_EntityNameUse"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="L"/&amp;gt;
 *     &amp;lt;enumeration value="A"/&amp;gt;
 *     &amp;lt;enumeration value="I"/&amp;gt;
 *     &amp;lt;enumeration value="R"/&amp;gt;
 *     &amp;lt;enumeration value="ABC"/&amp;gt;
 *     &amp;lt;enumeration value="SYL"/&amp;gt;
 *     &amp;lt;enumeration value="IDE"/&amp;gt;
 *     &amp;lt;enumeration value="PREVIOUS-BIRTH"/&amp;gt;
 *     &amp;lt;enumeration value="PREVIOUS-MAIDEN"/&amp;gt;
 *     &amp;lt;enumeration value="PREVIOUS-BACHELOR"/&amp;gt;
 *     &amp;lt;enumeration value="PREVIOUS"/&amp;gt;
 *     &amp;lt;enumeration value="PREFERRED"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_EntityNameUse")
@XmlEnum
public enum CsEntityNameUse {

    L("L"),
    A("A"),
    I("I"),
    R("R"),
    ABC("ABC"),
    SYL("SYL"),
    IDE("IDE"),
    @XmlEnumValue("PREVIOUS-BIRTH")
    PREVIOUS_BIRTH("PREVIOUS-BIRTH"),
    @XmlEnumValue("PREVIOUS-MAIDEN")
    PREVIOUS_MAIDEN("PREVIOUS-MAIDEN"),
    @XmlEnumValue("PREVIOUS-BACHELOR")
    PREVIOUS_BACHELOR("PREVIOUS-BACHELOR"),
    PREVIOUS("PREVIOUS"),
    PREFERRED("PREFERRED");
    private final String value;

    CsEntityNameUse(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CsEntityNameUse fromValue(String v) {
        for (CsEntityNameUse c: CsEntityNameUse.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
