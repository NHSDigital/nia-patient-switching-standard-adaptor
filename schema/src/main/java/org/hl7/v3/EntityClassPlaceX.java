
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for EntityClassPlace_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="EntityClassPlace_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="PROVINCE"/&amp;gt;
 *     &amp;lt;enumeration value="COUNTY"/&amp;gt;
 *     &amp;lt;enumeration value="COUNTRY"/&amp;gt;
 *     &amp;lt;enumeration value="CITY"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "EntityClassPlace_X")
@XmlEnum
public enum EntityClassPlaceX {

    PROVINCE,
    COUNTY,
    COUNTRY,
    CITY;

    public String value() {
        return name();
    }

    public static EntityClassPlaceX fromValue(String v) {
        return valueOf(v);
    }

}
