
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ParticipationInformationRecipient_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ParticipationInformationRecipient_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="PRCP"/&amp;gt;
 *     &amp;lt;enumeration value="TRC"/&amp;gt;
 *     &amp;lt;enumeration value="NOT"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ParticipationInformationRecipient_X")
@XmlEnum
public enum ParticipationInformationRecipientX {

    PRCP,
    TRC,
    NOT;

    public String value() {
        return name();
    }

    public static ParticipationInformationRecipientX fromValue(String v) {
        return valueOf(v);
    }

}
