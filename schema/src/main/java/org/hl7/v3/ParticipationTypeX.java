
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ParticipationType_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ParticipationType_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="RESP"/&amp;gt;
 *     &amp;lt;enumeration value="CST"/&amp;gt;
 *     &amp;lt;enumeration value="SPC"/&amp;gt;
 *     &amp;lt;enumeration value="RESPROV"/&amp;gt;
 *     &amp;lt;enumeration value="PATSBJ"/&amp;gt;
 *     &amp;lt;enumeration value="LOC"/&amp;gt;
 *     &amp;lt;enumeration value="CSM"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ParticipationType_X")
@XmlEnum
public enum ParticipationTypeX {

    RESP,
    CST,
    SPC,
    RESPROV,
    PATSBJ,
    LOC,
    CSM;

    public String value() {
        return name();
    }

    public static ParticipationTypeX fromValue(String v) {
        return valueOf(v);
    }

}
