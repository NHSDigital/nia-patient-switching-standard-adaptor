
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * Binary data is a raw block of bits. Binary data is a protected
 * type that should not be declared outside the data type specification.
 * 
 * 
 * &lt;p&gt;Java class for BIN complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="BIN"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}ANY"&amp;gt;
 *       &amp;lt;attribute name="representation" type="{urn:hl7-org:v3}cs_BinaryDataEncoding" default="TXT" /&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BIN")
@XmlSeeAlso({
    ED.class
})
public abstract class BIN
    extends ANY
{

    @XmlAttribute(name = "representation")
    protected CsBinaryDataEncoding representation;

    /**
     * Gets the value of the representation property.
     * 
     * @return
     *     possible object is
     *     {@link CsBinaryDataEncoding }
     *     
     */
    public CsBinaryDataEncoding getRepresentation() {
        if (representation == null) {
            return CsBinaryDataEncoding.TXT;
        } else {
            return representation;
        }
    }

    /**
     * Sets the value of the representation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CsBinaryDataEncoding }
     *     
     */
    public void setRepresentation(CsBinaryDataEncoding value) {
        this.representation = value;
    }

}
