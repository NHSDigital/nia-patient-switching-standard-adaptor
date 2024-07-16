
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_CalendarCycle.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_CalendarCycle"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="CY"/&amp;gt;
 *     &amp;lt;enumeration value="MY"/&amp;gt;
 *     &amp;lt;enumeration value="CM"/&amp;gt;
 *     &amp;lt;enumeration value="CW"/&amp;gt;
 *     &amp;lt;enumeration value="WY"/&amp;gt;
 *     &amp;lt;enumeration value="DM"/&amp;gt;
 *     &amp;lt;enumeration value="CD"/&amp;gt;
 *     &amp;lt;enumeration value="DY"/&amp;gt;
 *     &amp;lt;enumeration value="DW"/&amp;gt;
 *     &amp;lt;enumeration value="HD"/&amp;gt;
 *     &amp;lt;enumeration value="CH"/&amp;gt;
 *     &amp;lt;enumeration value="NH"/&amp;gt;
 *     &amp;lt;enumeration value="CN"/&amp;gt;
 *     &amp;lt;enumeration value="SN"/&amp;gt;
 *     &amp;lt;enumeration value="CS"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_CalendarCycle")
@XmlEnum
public enum CsCalendarCycle {

    CY,
    MY,
    CM,
    CW,
    WY,
    DM,
    CD,
    DY,
    DW,
    HD,
    CH,
    NH,
    CN,
    SN,
    CS;

    public String value() {
        return name();
    }

    public static CsCalendarCycle fromValue(String v) {
        return valueOf(v);
    }

}
