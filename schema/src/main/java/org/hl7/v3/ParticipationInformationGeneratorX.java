
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ParticipationInformationGenerator_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ParticipationInformationGenerator_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="AUT"/&amp;gt;
 *     &amp;lt;enumeration value="ENT"/&amp;gt;
 *     &amp;lt;enumeration value="INF"/&amp;gt;
 *     &amp;lt;enumeration value="WIT"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ParticipationInformationGenerator_X")
@XmlEnum
public enum ParticipationInformationGeneratorX {

    AUT,
    ENT,
    INF,
    WIT;

    public String value() {
        return name();
    }

    public static ParticipationInformationGeneratorX fromValue(String v) {
        return valueOf(v);
    }

}
