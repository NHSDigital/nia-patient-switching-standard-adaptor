
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ParticipationTargetDirect_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ParticipationTargetDirect_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="BBY"/&amp;gt;
 *     &amp;lt;enumeration value="DON"/&amp;gt;
 *     &amp;lt;enumeration value="PRD"/&amp;gt;
 *     &amp;lt;enumeration value="SBJ"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ParticipationTargetDirect_X")
@XmlEnum
public enum ParticipationTargetDirectX {

    BBY,
    DON,
    PRD,
    SBJ;

    public String value() {
        return name();
    }

    public static ParticipationTargetDirectX fromValue(String v) {
        return valueOf(v);
    }

}
