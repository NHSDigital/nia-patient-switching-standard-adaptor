
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for RoleStatusNormal_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="RoleStatusNormal_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="terminated"/&amp;gt;
 *     &amp;lt;enumeration value="suspended"/&amp;gt;
 *     &amp;lt;enumeration value="active"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "RoleStatusNormal_X")
@XmlEnum
public enum RoleStatusNormalX {

    @XmlEnumValue("terminated")
    TERMINATED("terminated"),
    @XmlEnumValue("suspended")
    SUSPENDED("suspended"),
    @XmlEnumValue("active")
    ACTIVE("active");
    private final String value;

    RoleStatusNormalX(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RoleStatusNormalX fromValue(String v) {
        for (RoleStatusNormalX c: RoleStatusNormalX.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
