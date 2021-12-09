
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_EntityNamePartType.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_EntityNamePartType"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="FAM"/&amp;gt;
 *     &amp;lt;enumeration value="GIV"/&amp;gt;
 *     &amp;lt;enumeration value="PFX"/&amp;gt;
 *     &amp;lt;enumeration value="SFX"/&amp;gt;
 *     &amp;lt;enumeration value="DEL"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_EntityNamePartType")
@XmlEnum
public enum CsEntityNamePartType {

    FAM,
    GIV,
    PFX,
    SFX,
    DEL;

    public String value() {
        return name();
    }

    public static CsEntityNamePartType fromValue(String v) {
        return valueOf(v);
    }

}
