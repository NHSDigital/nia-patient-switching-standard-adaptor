
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for ActDocumentStructureClass_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="ActDocumentStructureClass_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="DOCBODY"/&amp;gt;
 *     &amp;lt;enumeration value="DOCCNTNT"/&amp;gt;
 *     &amp;lt;enumeration value="DOCLSTITM"/&amp;gt;
 *     &amp;lt;enumeration value="DOCPARA"/&amp;gt;
 *     &amp;lt;enumeration value="DOCSECT"/&amp;gt;
 *     &amp;lt;enumeration value="DOCTBL"/&amp;gt;
 *     &amp;lt;enumeration value="LINKHTML"/&amp;gt;
 *     &amp;lt;enumeration value="LOCALATTR"/&amp;gt;
 *     &amp;lt;enumeration value="LOCALMRKP"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "ActDocumentStructureClass_X")
@XmlEnum
public enum ActDocumentStructureClassX {

    DOCBODY,
    DOCCNTNT,
    DOCLSTITM,
    DOCPARA,
    DOCSECT,
    DOCTBL,
    LINKHTML,
    LOCALATTR,
    LOCALMRKP;

    public String value() {
        return name();
    }

    public static ActDocumentStructureClassX fromValue(String v) {
        return valueOf(v);
    }

}
