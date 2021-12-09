
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ParticipationAncillary_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ParticipationAncillary_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="ADM"/&amp;gt;
 *     &amp;lt;enumeration value="ATND"/&amp;gt;
 *     &amp;lt;enumeration value="CON"/&amp;gt;
 *     &amp;lt;enumeration value="DIS"/&amp;gt;
 *     &amp;lt;enumeration value="ESC"/&amp;gt;
 *     &amp;lt;enumeration value="REF"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ParticipationAncillary_X")
@XmlEnum
public enum ParticipationAncillaryX {

    ADM,
    ATND,
    CON,
    DIS,
    ESC,
    REF;

    public String value() {
        return name();
    }

    public static ParticipationAncillaryX fromValue(String v) {
        return valueOf(v);
    }

}
