
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActRelationshipSequel_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActRelationshipSequel_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="DOC"/&amp;gt;
 *     &amp;lt;enumeration value="ELNK"/&amp;gt;
 *     &amp;lt;enumeration value="GEVL"/&amp;gt;
 *     &amp;lt;enumeration value="GEN"/&amp;gt;
 *     &amp;lt;enumeration value="OPTN"/&amp;gt;
 *     &amp;lt;enumeration value="INST"/&amp;gt;
 *     &amp;lt;enumeration value="APND"/&amp;gt;
 *     &amp;lt;enumeration value="MTCH"/&amp;gt;
 *     &amp;lt;enumeration value="REV"/&amp;gt;
 *     &amp;lt;enumeration value="XFRM"/&amp;gt;
 *     &amp;lt;enumeration value="UPDT"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActRelationshipSequel_X")
@XmlEnum
public enum ActRelationshipSequelX {

    DOC,
    ELNK,
    GEVL,
    GEN,
    OPTN,
    INST,
    APND,
    MTCH,
    REV,
    XFRM,
    UPDT;

    public String value() {
        return name();
    }

    public static ActRelationshipSequelX fromValue(String v) {
        return valueOf(v);
    }

}
