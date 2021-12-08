
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActRelationshipOutcome_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActRelationshipOutcome_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="RISK"/&amp;gt;
 *     &amp;lt;enumeration value="OBJC"/&amp;gt;
 *     &amp;lt;enumeration value="OBJF"/&amp;gt;
 *     &amp;lt;enumeration value="GOAL"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActRelationshipOutcome_X")
@XmlEnum
public enum ActRelationshipOutcomeX {

    RISK,
    OBJC,
    OBJF,
    GOAL;

    public String value() {
        return name();
    }

    public static ActRelationshipOutcomeX fromValue(String v) {
        return valueOf(v);
    }

}
