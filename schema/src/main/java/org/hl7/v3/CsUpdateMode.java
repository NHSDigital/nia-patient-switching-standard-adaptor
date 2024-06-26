
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_UpdateMode.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_UpdateMode"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="added"/&amp;gt;
 *     &amp;lt;enumeration value="altered"/&amp;gt;
 *     &amp;lt;enumeration value="removed"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_UpdateMode")
@XmlEnum
public enum CsUpdateMode {

    @XmlEnumValue("added")
    ADDED("added"),
    @XmlEnumValue("altered")
    ALTERED("altered"),
    @XmlEnumValue("removed")
    REMOVED("removed");
    private final String value;

    CsUpdateMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CsUpdateMode fromValue(String v) {
        for (CsUpdateMode c: CsUpdateMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
