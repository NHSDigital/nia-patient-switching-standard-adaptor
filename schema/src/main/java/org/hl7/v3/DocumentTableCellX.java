
package org.hl7.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for DocumentTableCell_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="DocumentTableCell_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="TBLDATA"/&amp;gt;
 *     &amp;lt;enumeration value="TBLHDR"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "DocumentTableCell_X")
@XmlEnum
public enum DocumentTableCellX {

    TBLDATA,
    TBLHDR;

    public String value() {
        return name();
    }

    public static DocumentTableCellX fromValue(String v) {
        return valueOf(v);
    }

}
