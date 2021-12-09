
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ParticipationPhysicalPerformer_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ParticipationPhysicalPerformer_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="SPRF"/&amp;gt;
 *     &amp;lt;enumeration value="PPRF"/&amp;gt;
 *     &amp;lt;enumeration value="DIST"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ParticipationPhysicalPerformer_X")
@XmlEnum
public enum ParticipationPhysicalPerformerX {

    SPRF,
    PPRF,
    DIST;

    public String value() {
        return name();
    }

    public static ParticipationPhysicalPerformerX fromValue(String v) {
        return valueOf(v);
    }

}
