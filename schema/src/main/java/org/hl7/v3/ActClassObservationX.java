
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActClassObservation_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActClassObservation_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="CLNTRL"/&amp;gt;
 *     &amp;lt;enumeration value="CNOD"/&amp;gt;
 *     &amp;lt;enumeration value="COND"/&amp;gt;
 *     &amp;lt;enumeration value="MPROT"/&amp;gt;
 *     &amp;lt;enumeration value="ALRT"/&amp;gt;
 *     &amp;lt;enumeration value="SPCOBS"/&amp;gt;
 *     &amp;lt;enumeration value="DGIMG"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActClassObservation_X")
@XmlEnum
public enum ActClassObservationX {

    CLNTRL,
    CNOD,
    COND,
    MPROT,
    ALRT,
    SPCOBS,
    DGIMG;

    public String value() {
        return name();
    }

    public static ActClassObservationX fromValue(String v) {
        return valueOf(v);
    }

}
