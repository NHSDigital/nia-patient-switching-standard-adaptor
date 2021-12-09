
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for LicensedEntityRole_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="LicensedEntityRole_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="DSDLOC"/&amp;gt;
 *     &amp;lt;enumeration value="PROV"/&amp;gt;
 *     &amp;lt;enumeration value="NOT"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "LicensedEntityRole_X")
@XmlEnum
public enum LicensedEntityRoleX {

    DSDLOC,
    PROV,
    NOT;

    public String value() {
        return name();
    }

    public static LicensedEntityRoleX fromValue(String v) {
        return valueOf(v);
    }

}
