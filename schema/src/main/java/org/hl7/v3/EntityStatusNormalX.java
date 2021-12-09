
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for EntityStatusNormal_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="EntityStatusNormal_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="terminated"/&amp;gt;
 *     &amp;lt;enumeration value="active"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "EntityStatusNormal_X")
@XmlEnum
public enum EntityStatusNormalX {

    @XmlEnumValue("terminated")
    TERMINATED("terminated"),
    @XmlEnumValue("active")
    ACTIVE("active");
    private final String value;

    EntityStatusNormalX(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EntityStatusNormalX fromValue(String v) {
        for (EntityStatusNormalX c: EntityStatusNormalX.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
