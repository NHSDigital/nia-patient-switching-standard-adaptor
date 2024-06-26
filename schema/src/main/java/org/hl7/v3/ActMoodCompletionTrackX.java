
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActMoodCompletionTrack_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActMoodCompletionTrack_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="DEF"/&amp;gt;
 *     &amp;lt;enumeration value="EVN"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActMoodCompletionTrack_X")
@XmlEnum
public enum ActMoodCompletionTrackX {

    DEF,
    EVN;

    public String value() {
        return name();
    }

    public static ActMoodCompletionTrackX fromValue(String v) {
        return valueOf(v);
    }

}
