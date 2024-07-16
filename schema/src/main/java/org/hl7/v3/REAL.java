
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * Fractional numbers. Typically used whenever quantities are measured,
 * estimated, or computed from other real numbers.  The typical
 * representation is decimal, where the number of significant decimal
 * digits is known as the precision.
 * 
 * Real numbers are needed beyond integers whenever quantities of the
 * real world are measured, estimated, or computed from other real
 * numbers. The term "Real number" in this specification is used to mean
 * that fractional values are covered without necessarily implying the
 * full set of the mathematical real numbers.
 * 
 * 
 * &lt;p&gt;Java class for REAL complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="REAL"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}QTY"&amp;gt;
 *       &amp;lt;attribute name="value" type="{urn:hl7-org:v3}real" /&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "REAL")
public class REAL
    extends QTY
{

    @XmlAttribute(name = "value")
    protected String value;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

}
