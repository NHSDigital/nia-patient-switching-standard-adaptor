
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_SetOperator.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_SetOperator"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="I"/&amp;gt;
 *     &amp;lt;enumeration value="E"/&amp;gt;
 *     &amp;lt;enumeration value="A"/&amp;gt;
 *     &amp;lt;enumeration value="H"/&amp;gt;
 *     &amp;lt;enumeration value="P"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_SetOperator")
@XmlEnum
public enum CsSetOperator {

    I,
    E,
    A,
    H,
    P;

    public String value() {
        return name();
    }

    public static CsSetOperator fromValue(String v) {
        return valueOf(v);
    }

}
