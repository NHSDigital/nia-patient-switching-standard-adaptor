
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for RoleClassPartitive_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="RoleClassPartitive_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="CONT"/&amp;gt;
 *     &amp;lt;enumeration value="MBR"/&amp;gt;
 *     &amp;lt;enumeration value="PART"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "RoleClassPartitive_X")
@XmlEnum
public enum RoleClassPartitiveX {

    CONT,
    MBR,
    PART;

    public String value() {
        return name();
    }

    public static RoleClassPartitiveX fromValue(String v) {
        return valueOf(v);
    }

}
