
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActRelationshipConditional_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActRelationshipConditional_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="CIND"/&amp;gt;
 *     &amp;lt;enumeration value="PRCN"/&amp;gt;
 *     &amp;lt;enumeration value="RSON"/&amp;gt;
 *     &amp;lt;enumeration value="TRIG"/&amp;gt;
 *     &amp;lt;enumeration value="RACT"/&amp;gt;
 *     &amp;lt;enumeration value="SUGG"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActRelationshipConditional_X")
@XmlEnum
public enum ActRelationshipConditionalX {

    CIND,
    PRCN,
    RSON,
    TRIG,
    RACT,
    SUGG;

    public String value() {
        return name();
    }

    public static ActRelationshipConditionalX fromValue(String v) {
        return valueOf(v);
    }

}
