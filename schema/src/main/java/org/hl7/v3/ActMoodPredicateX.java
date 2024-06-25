
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActMoodPredicate_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActMoodPredicate_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="EVN.CRT"/&amp;gt;
 *     &amp;lt;enumeration value="GOL"/&amp;gt;
 *     &amp;lt;enumeration value="OPT"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActMoodPredicate_X")
@XmlEnum
public enum ActMoodPredicateX {

    @XmlEnumValue("EVN.CRT")
    EVN_CRT("EVN.CRT"),
    GOL("GOL"),
    OPT("OPT");
    private final String value;

    ActMoodPredicateX(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ActMoodPredicateX fromValue(String v) {
        for (ActMoodPredicateX c: ActMoodPredicateX.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
