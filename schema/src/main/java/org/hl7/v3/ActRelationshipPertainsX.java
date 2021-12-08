
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActRelationshipPertains_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActRelationshipPertains_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="NAME"/&amp;gt;
 *     &amp;lt;enumeration value="AUTH"/&amp;gt;
 *     &amp;lt;enumeration value="COVBY"/&amp;gt;
 *     &amp;lt;enumeration value="EXPL"/&amp;gt;
 *     &amp;lt;enumeration value="PREV"/&amp;gt;
 *     &amp;lt;enumeration value="REFV"/&amp;gt;
 *     &amp;lt;enumeration value="SUBJ"/&amp;gt;
 *     &amp;lt;enumeration value="CAUS"/&amp;gt;
 *     &amp;lt;enumeration value="DRIV"/&amp;gt;
 *     &amp;lt;enumeration value="MFST"/&amp;gt;
 *     &amp;lt;enumeration value="LIMIT"/&amp;gt;
 *     &amp;lt;enumeration value="REFR"/&amp;gt;
 *     &amp;lt;enumeration value="SUMM"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActRelationshipPertains_X")
@XmlEnum
public enum ActRelationshipPertainsX {

    NAME,
    AUTH,
    COVBY,
    EXPL,
    PREV,
    REFV,
    SUBJ,
    CAUS,
    DRIV,
    MFST,
    LIMIT,
    REFR,
    SUMM;

    public String value() {
        return name();
    }

    public static ActRelationshipPertainsX fromValue(String v) {
        return valueOf(v);
    }

}
