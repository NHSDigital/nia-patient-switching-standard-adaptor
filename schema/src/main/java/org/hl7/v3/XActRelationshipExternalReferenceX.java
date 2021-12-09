
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for x_ActRelationshipExternalReference_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="x_ActRelationshipExternalReference_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="ELNK"/&amp;gt;
 *     &amp;lt;enumeration value="VRXCRPT"/&amp;gt;
 *     &amp;lt;enumeration value="XCRPT"/&amp;gt;
 *     &amp;lt;enumeration value="SUBJ"/&amp;gt;
 *     &amp;lt;enumeration value="SPRT"/&amp;gt;
 *     &amp;lt;enumeration value="REFR"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "x_ActRelationshipExternalReference_X")
@XmlEnum
public enum XActRelationshipExternalReferenceX {

    ELNK,
    VRXCRPT,
    XCRPT,
    SUBJ,
    SPRT,
    REFR;

    public String value() {
        return name();
    }

    public static XActRelationshipExternalReferenceX fromValue(String v) {
        return valueOf(v);
    }

}
