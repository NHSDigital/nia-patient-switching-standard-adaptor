
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActClassRoot_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActClassRoot_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="INVE"/&amp;gt;
 *     &amp;lt;enumeration value="PROC"/&amp;gt;
 *     &amp;lt;enumeration value="CONS"/&amp;gt;
 *     &amp;lt;enumeration value="REFR"/&amp;gt;
 *     &amp;lt;enumeration value="TRNS"/&amp;gt;
 *     &amp;lt;enumeration value="LIST"/&amp;gt;
 *     &amp;lt;enumeration value="ENC"/&amp;gt;
 *     &amp;lt;enumeration value="XACT"/&amp;gt;
 *     &amp;lt;enumeration value="SBADM"/&amp;gt;
 *     &amp;lt;enumeration value="ACCT"/&amp;gt;
 *     &amp;lt;enumeration value="CTTEVENT"/&amp;gt;
 *     &amp;lt;enumeration value="CONTREG"/&amp;gt;
 *     &amp;lt;enumeration value="SPCTRT"/&amp;gt;
 *     &amp;lt;enumeration value="REG"/&amp;gt;
 *     &amp;lt;enumeration value="ACCM"/&amp;gt;
 *     &amp;lt;enumeration value="ACSN"/&amp;gt;
 *     &amp;lt;enumeration value="ADJUD"/&amp;gt;
 *     &amp;lt;enumeration value="INFRM"/&amp;gt;
 *     &amp;lt;enumeration value="PCPR"/&amp;gt;
 *     &amp;lt;enumeration value="INC"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActClassRoot_X")
@XmlEnum
public enum ActClassRootX {

    INVE,
    PROC,
    CONS,
    REFR,
    TRNS,
    LIST,
    ENC,
    XACT,
    SBADM,
    ACCT,
    CTTEVENT,
    CONTREG,
    SPCTRT,
    REG,
    ACCM,
    ACSN,
    ADJUD,
    INFRM,
    PCPR,
    INC;

    public String value() {
        return name();
    }

    public static ActClassRootX fromValue(String v) {
        return valueOf(v);
    }

}
