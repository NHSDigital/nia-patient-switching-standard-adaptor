
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActClassROI_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActClassROI_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="ROIBND"/&amp;gt;
 *     &amp;lt;enumeration value="ROIOVL"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActClassROI_X")
@XmlEnum
public enum ActClassROIX {

    ROIBND,
    ROIOVL;

    public String value() {
        return name();
    }

    public static ActClassROIX fromValue(String v) {
        return valueOf(v);
    }

}
