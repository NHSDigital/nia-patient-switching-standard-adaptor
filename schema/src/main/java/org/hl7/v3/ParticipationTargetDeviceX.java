
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ParticipationTargetDevice_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ParticipationTargetDevice_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="RDV"/&amp;gt;
 *     &amp;lt;enumeration value="NRD"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ParticipationTargetDevice_X")
@XmlEnum
public enum ParticipationTargetDeviceX {

    RDV,
    NRD;

    public String value() {
        return name();
    }

    public static ParticipationTargetDeviceX fromValue(String v) {
        return valueOf(v);
    }

}
