
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for TableRowGroupType_X.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="TableRowGroupType_X"&amp;gt;
 *   &amp;lt;restriction base="{urn:hl7-org:v3}cs"&amp;gt;
 *     &amp;lt;enumeration value="tbody"/&amp;gt;
 *     &amp;lt;enumeration value="tfoot"/&amp;gt;
 *     &amp;lt;enumeration value="thead"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "TableRowGroupType_X")
@XmlEnum
public enum TableRowGroupTypeX {

    @XmlEnumValue("tbody")
    TBODY("tbody"),
    @XmlEnumValue("tfoot")
    TFOOT("tfoot"),
    @XmlEnumValue("thead")
    THEAD("thead");
    private final String value;

    TableRowGroupTypeX(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TableRowGroupTypeX fromValue(String v) {
        for (TableRowGroupTypeX c: TableRowGroupTypeX.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
