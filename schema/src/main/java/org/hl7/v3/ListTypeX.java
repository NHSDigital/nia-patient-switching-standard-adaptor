
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ListType_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ListType_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="ordered"/&amp;gt;
 *     &amp;lt;enumeration value="unordered"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ListType_X")
@XmlEnum
public enum ListTypeX {

    @XmlEnumValue("ordered")
    ORDERED("ordered"),
    @XmlEnumValue("unordered")
    UNORDERED("unordered");
    private final String value;

    ListTypeX(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ListTypeX fromValue(String v) {
        for (ListTypeX c: ListTypeX.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
