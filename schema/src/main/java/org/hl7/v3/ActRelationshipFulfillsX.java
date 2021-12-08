
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActRelationshipFulfills_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActRelationshipFulfills_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="OREF"/&amp;gt;
 *     &amp;lt;enumeration value="SCH"/&amp;gt;
 *     &amp;lt;enumeration value="OCCR"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActRelationshipFulfills_X")
@XmlEnum
public enum ActRelationshipFulfillsX {

    OREF,
    SCH,
    OCCR;

    public String value() {
        return name();
    }

    public static ActRelationshipFulfillsX fromValue(String v) {
        return valueOf(v);
    }

}
