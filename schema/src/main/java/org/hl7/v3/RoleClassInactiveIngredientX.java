
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for RoleClassInactiveIngredient_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="RoleClassInactiveIngredient_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="COLR"/&amp;gt;
 *     &amp;lt;enumeration value="FLVR"/&amp;gt;
 *     &amp;lt;enumeration value="PRSV"/&amp;gt;
 *     &amp;lt;enumeration value="STBL"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "RoleClassInactiveIngredient_X")
@XmlEnum
public enum RoleClassInactiveIngredientX {

    COLR,
    FLVR,
    PRSV,
    STBL;

    public String value() {
        return name();
    }

    public static RoleClassInactiveIngredientX fromValue(String v) {
        return valueOf(v);
    }

}
