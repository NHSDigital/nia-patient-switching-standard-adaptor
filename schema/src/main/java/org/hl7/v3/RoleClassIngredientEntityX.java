
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for RoleClassIngredientEntity_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="RoleClassIngredientEntity_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="ACTI"/&amp;gt;
 *     &amp;lt;enumeration value="ACTM"/&amp;gt;
 *     &amp;lt;enumeration value="ADTV"/&amp;gt;
 *     &amp;lt;enumeration value="BASE"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "RoleClassIngredientEntity_X")
@XmlEnum
public enum RoleClassIngredientEntityX {

    ACTI,
    ACTM,
    ADTV,
    BASE;

    public String value() {
        return name();
    }

    public static RoleClassIngredientEntityX fromValue(String v) {
        return valueOf(v);
    }

}
