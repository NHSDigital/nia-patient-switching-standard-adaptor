
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActRelationshipPosts_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActRelationshipPosts_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="CHRG"/&amp;gt;
 *     &amp;lt;enumeration value="COST"/&amp;gt;
 *     &amp;lt;enumeration value="CREDIT"/&amp;gt;
 *     &amp;lt;enumeration value="DEBIT"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActRelationshipPosts_X")
@XmlEnum
public enum ActRelationshipPostsX {

    CHRG,
    COST,
    CREDIT,
    DEBIT;

    public String value() {
        return name();
    }

    public static ActRelationshipPostsX fromValue(String v) {
        return valueOf(v);
    }

}
