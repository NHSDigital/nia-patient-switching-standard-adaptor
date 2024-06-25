
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for EntityClassNonPersonLivingSubject_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="EntityClassNonPersonLivingSubject_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="MIC"/&amp;gt;
 *     &amp;lt;enumeration value="PLNT"/&amp;gt;
 *     &amp;lt;enumeration value="ANM"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "EntityClassNonPersonLivingSubject_X")
@XmlEnum
public enum EntityClassNonPersonLivingSubjectX {

    MIC,
    PLNT,
    ANM;

    public String value() {
        return name();
    }

    public static EntityClassNonPersonLivingSubjectX fromValue(String v) {
        return valueOf(v);
    }

}
