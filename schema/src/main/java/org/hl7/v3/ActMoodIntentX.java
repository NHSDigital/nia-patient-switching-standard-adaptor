
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActMoodIntent_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActMoodIntent_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="PRMS"/&amp;gt;
 *     &amp;lt;enumeration value="PRP"/&amp;gt;
 *     &amp;lt;enumeration value="APT"/&amp;gt;
 *     &amp;lt;enumeration value="ARQ"/&amp;gt;
 *     &amp;lt;enumeration value="RQO"/&amp;gt;
 *     &amp;lt;enumeration value="SLOT"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActMoodIntent_X")
@XmlEnum
public enum ActMoodIntentX {

    PRMS,
    PRP,
    APT,
    ARQ,
    RQO,
    SLOT;

    public String value() {
        return name();
    }

    public static ActMoodIntentX fromValue(String v) {
        return valueOf(v);
    }

}
