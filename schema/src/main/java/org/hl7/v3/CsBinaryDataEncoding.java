
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_BinaryDataEncoding.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_BinaryDataEncoding"&amp;gt;
 *   &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&amp;gt;
 *     &amp;lt;enumeration value="B64"/&amp;gt;
 *     &amp;lt;enumeration value="TXT"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_BinaryDataEncoding")
@XmlEnum
public enum CsBinaryDataEncoding {

    @XmlEnumValue("B64")
    B_64("B64"),
    TXT("TXT");
    private final String value;

    CsBinaryDataEncoding(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CsBinaryDataEncoding fromValue(String v) {
        for (CsBinaryDataEncoding c: CsBinaryDataEncoding.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
