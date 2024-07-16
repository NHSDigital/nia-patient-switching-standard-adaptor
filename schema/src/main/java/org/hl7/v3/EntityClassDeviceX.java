
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for EntityClassDevice_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="EntityClassDevice_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="CER"/&amp;gt;
 *     &amp;lt;enumeration value="MODDV"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "EntityClassDevice_X")
@XmlEnum
public enum EntityClassDeviceX {

    CER,
    MODDV;

    public String value() {
        return name();
    }

    public static EntityClassDeviceX fromValue(String v) {
        return valueOf(v);
    }

}
