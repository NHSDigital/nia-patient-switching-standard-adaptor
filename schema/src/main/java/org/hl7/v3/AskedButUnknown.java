
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for AskedButUnknown.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="AskedButUnknown"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="ASKU"/&amp;gt;
 *     &amp;lt;enumeration value="NAV"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "AskedButUnknown")
@XmlEnum
public enum AskedButUnknown {

    ASKU,
    NAV;

    public String value() {
        return name();
    }

    public static AskedButUnknown fromValue(String v) {
        return valueOf(v);
    }

}
