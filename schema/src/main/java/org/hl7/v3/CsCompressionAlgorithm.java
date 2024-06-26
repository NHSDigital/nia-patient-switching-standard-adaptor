
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_CompressionAlgorithm.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_CompressionAlgorithm"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="DF"/&amp;gt;
 *     &amp;lt;enumeration value="GZ"/&amp;gt;
 *     &amp;lt;enumeration value="ZL"/&amp;gt;
 *     &amp;lt;enumeration value="Z"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_CompressionAlgorithm")
@XmlEnum
public enum CsCompressionAlgorithm {

    DF,
    GZ,
    ZL,
    Z;

    public String value() {
        return name();
    }

    public static CsCompressionAlgorithm fromValue(String v) {
        return valueOf(v);
    }

}
