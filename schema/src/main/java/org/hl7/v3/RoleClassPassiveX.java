
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for RoleClassPassive_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="RoleClassPassive_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="BIRTHPL"/&amp;gt;
 *     &amp;lt;enumeration value="ACCESS"/&amp;gt;
 *     &amp;lt;enumeration value="EXPR"/&amp;gt;
 *     &amp;lt;enumeration value="HLTHCHRT"/&amp;gt;
 *     &amp;lt;enumeration value="HLD"/&amp;gt;
 *     &amp;lt;enumeration value="IDENT"/&amp;gt;
 *     &amp;lt;enumeration value="MNT"/&amp;gt;
 *     &amp;lt;enumeration value="OWN"/&amp;gt;
 *     &amp;lt;enumeration value="RGPR"/&amp;gt;
 *     &amp;lt;enumeration value="TERR"/&amp;gt;
 *     &amp;lt;enumeration value="WRTE"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "RoleClassPassive_X")
@XmlEnum
public enum RoleClassPassiveX {

    BIRTHPL,
    ACCESS,
    EXPR,
    HLTHCHRT,
    HLD,
    IDENT,
    MNT,
    OWN,
    RGPR,
    TERR,
    WRTE;

    public String value() {
        return name();
    }

    public static RoleClassPassiveX fromValue(String v) {
        return valueOf(v);
    }

}
