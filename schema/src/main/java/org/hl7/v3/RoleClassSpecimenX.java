
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for RoleClassSpecimen_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="RoleClassSpecimen_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="ISLT"/&amp;gt;
 *     &amp;lt;enumeration value="ALQT"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "RoleClassSpecimen_X")
@XmlEnum
public enum RoleClassSpecimenX {

    ISLT,
    ALQT;

    public String value() {
        return name();
    }

    public static RoleClassSpecimenX fromValue(String v) {
        return valueOf(v);
    }

}
