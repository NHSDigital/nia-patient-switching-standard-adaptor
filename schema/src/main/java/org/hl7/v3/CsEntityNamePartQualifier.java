
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for cs_EntityNamePartQualifier.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="cs_EntityNamePartQualifier"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="BR"/&amp;gt;
 *     &amp;lt;enumeration value="SP"/&amp;gt;
 *     &amp;lt;enumeration value="VV"/&amp;gt;
 *     &amp;lt;enumeration value="AC"/&amp;gt;
 *     &amp;lt;enumeration value="PR"/&amp;gt;
 *     &amp;lt;enumeration value="NB"/&amp;gt;
 *     &amp;lt;enumeration value="LS"/&amp;gt;
 *     &amp;lt;enumeration value="CL"/&amp;gt;
 *     &amp;lt;enumeration value="IN"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "cs_EntityNamePartQualifier")
@XmlEnum
public enum CsEntityNamePartQualifier {

    BR,
    SP,
    VV,
    AC,
    PR,
    NB,
    LS,
    CL,
    IN;

    public String value() {
        return name();
    }

    public static CsEntityNamePartQualifier fromValue(String v) {
        return valueOf(v);
    }

}
