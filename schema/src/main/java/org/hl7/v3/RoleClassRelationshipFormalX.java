
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for RoleClassRelationshipFormal_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="RoleClassRelationshipFormal_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="CIT"/&amp;gt;
 *     &amp;lt;enumeration value="CRINV"/&amp;gt;
 *     &amp;lt;enumeration value="CRSPNSR"/&amp;gt;
 *     &amp;lt;enumeration value="COVPTY"/&amp;gt;
 *     &amp;lt;enumeration value="GUAR"/&amp;gt;
 *     &amp;lt;enumeration value="PAYOR"/&amp;gt;
 *     &amp;lt;enumeration value="PAT"/&amp;gt;
 *     &amp;lt;enumeration value="PAYEE"/&amp;gt;
 *     &amp;lt;enumeration value="POLHOLD"/&amp;gt;
 *     &amp;lt;enumeration value="QUAL"/&amp;gt;
 *     &amp;lt;enumeration value="RESBJ"/&amp;gt;
 *     &amp;lt;enumeration value="SPNSR"/&amp;gt;
 *     &amp;lt;enumeration value="STD"/&amp;gt;
 *     &amp;lt;enumeration value="UNDWRT"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "RoleClassRelationshipFormal_X")
@XmlEnum
public enum RoleClassRelationshipFormalX {

    CIT,
    CRINV,
    CRSPNSR,
    COVPTY,
    GUAR,
    PAYOR,
    PAT,
    PAYEE,
    POLHOLD,
    QUAL,
    RESBJ,
    SPNSR,
    STD,
    UNDWRT;

    public String value() {
        return name();
    }

    public static RoleClassRelationshipFormalX fromValue(String v) {
        return valueOf(v);
    }

}
