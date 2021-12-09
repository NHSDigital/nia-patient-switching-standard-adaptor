
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ParticipationTargetLocation_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ParticipationTargetLocation_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="ELOC"/&amp;gt;
 *     &amp;lt;enumeration value="RML"/&amp;gt;
 *     &amp;lt;enumeration value="VIA"/&amp;gt;
 *     &amp;lt;enumeration value="DST"/&amp;gt;
 *     &amp;lt;enumeration value="ORG"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ParticipationTargetLocation_X")
@XmlEnum
public enum ParticipationTargetLocationX {

    ELOC,
    RML,
    VIA,
    DST,
    ORG;

    public String value() {
        return name();
    }

    public static ParticipationTargetLocationX fromValue(String v) {
        return valueOf(v);
    }

}
