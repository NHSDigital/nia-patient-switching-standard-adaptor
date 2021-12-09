
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ContextControlAdditive_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ContextControlAdditive_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="AN"/&amp;gt;
 *     &amp;lt;enumeration value="AP"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ContextControlAdditive_X")
@XmlEnum
public enum ContextControlAdditiveX {

    AN,
    AP;

    public String value() {
        return name();
    }

    public static ContextControlAdditiveX fromValue(String v) {
        return valueOf(v);
    }

}
