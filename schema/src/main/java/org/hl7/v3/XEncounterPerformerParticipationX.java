
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for x_EncounterPerformerParticipation_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="x_EncounterPerformerParticipation_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="CON"/&amp;gt;
 *     &amp;lt;enumeration value="PRF"/&amp;gt;
 *     &amp;lt;enumeration value="SPRF"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "x_EncounterPerformerParticipation_X")
@XmlEnum
public enum XEncounterPerformerParticipationX {

    CON,
    PRF,
    SPRF;

    public String value() {
        return name();
    }

    public static XEncounterPerformerParticipationX fromValue(String v) {
        return valueOf(v);
    }

}
