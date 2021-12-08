
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for x_ParticipationVrfRespSprfWit_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="x_ParticipationVrfRespSprfWit_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="RESP"/&amp;gt;
 *     &amp;lt;enumeration value="SPRF"/&amp;gt;
 *     &amp;lt;enumeration value="VRF"/&amp;gt;
 *     &amp;lt;enumeration value="WIT"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "x_ParticipationVrfRespSprfWit_X")
@XmlEnum
public enum XParticipationVrfRespSprfWitX {

    RESP,
    SPRF,
    VRF,
    WIT;

    public String value() {
        return name();
    }

    public static XParticipationVrfRespSprfWitX fromValue(String v) {
        return valueOf(v);
    }

}
