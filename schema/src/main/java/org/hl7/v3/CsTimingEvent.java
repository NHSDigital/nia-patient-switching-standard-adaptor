
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_TimingEvent.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_TimingEvent"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="AC"/&amp;gt;
 *     &amp;lt;enumeration value="ACD"/&amp;gt;
 *     &amp;lt;enumeration value="ACM"/&amp;gt;
 *     &amp;lt;enumeration value="ACV"/&amp;gt;
 *     &amp;lt;enumeration value="HS"/&amp;gt;
 *     &amp;lt;enumeration value="IC"/&amp;gt;
 *     &amp;lt;enumeration value="ICD"/&amp;gt;
 *     &amp;lt;enumeration value="ICM"/&amp;gt;
 *     &amp;lt;enumeration value="ICV"/&amp;gt;
 *     &amp;lt;enumeration value="PC"/&amp;gt;
 *     &amp;lt;enumeration value="PCD"/&amp;gt;
 *     &amp;lt;enumeration value="PCM"/&amp;gt;
 *     &amp;lt;enumeration value="PCV"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_TimingEvent")
@XmlEnum
public enum CsTimingEvent {

    AC,
    ACD,
    ACM,
    ACV,
    HS,
    IC,
    ICD,
    ICM,
    ICV,
    PC,
    PCD,
    PCM,
    PCV;

    public String value() {
        return name();
    }

    public static CsTimingEvent fromValue(String v) {
        return valueOf(v);
    }

}
