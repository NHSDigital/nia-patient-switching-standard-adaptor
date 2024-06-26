
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActRelationshipHasComponent_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActRelationshipHasComponent_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="DEP"/&amp;gt;
 *     &amp;lt;enumeration value="ARR"/&amp;gt;
 *     &amp;lt;enumeration value="CTRLV"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActRelationshipHasComponent_X")
@XmlEnum
public enum ActRelationshipHasComponentX {

    DEP,
    ARR,
    CTRLV;

    public String value() {
        return name();
    }

    public static ActRelationshipHasComponentX fromValue(String v) {
        return valueOf(v);
    }

}
