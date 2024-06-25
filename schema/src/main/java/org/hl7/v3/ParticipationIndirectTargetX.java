
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ParticipationIndirectTarget_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ParticipationIndirectTarget_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="HLD"/&amp;gt;
 *     &amp;lt;enumeration value="COV"/&amp;gt;
 *     &amp;lt;enumeration value="RCV"/&amp;gt;
 *     &amp;lt;enumeration value="RCT"/&amp;gt;
 *     &amp;lt;enumeration value="BEN"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ParticipationIndirectTarget_X")
@XmlEnum
public enum ParticipationIndirectTargetX {

    HLD,
    COV,
    RCV,
    RCT,
    BEN;

    public String value() {
        return name();
    }

    public static ParticipationIndirectTargetX fromValue(String v) {
        return valueOf(v);
    }

}
