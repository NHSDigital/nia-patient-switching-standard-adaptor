
package org.hl7.v3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * Defines the basic properties of every data value. This is an abstract
 * type, meaning that no value can be just a data value without belonging
 * to any concrete type. Every concrete type is a specialization of this
 * general abstract DataValue type.
 *   
 * 
 * &lt;p&gt;Java class for ANY complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="ANY"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;attribute name="nullFlavor" type="{urn:hl7-org:v3}cs_NullFlavor" /&amp;gt;
 *       &amp;lt;attribute name="updateMode" type="{urn:hl7-org:v3}cs_UpdateMode" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ANY")
@XmlSeeAlso({
    BL.class,
    BN.class,
    II.class,
    URL.class,
    BIN.class,
    CD.class,
    IVLPQ.class,
    QTY.class
})
public abstract class ANY {

    @XmlAttribute(name = "nullFlavor")
    protected CsNullFlavor nullFlavor;
    @XmlAttribute(name = "updateMode")
    protected CsUpdateMode updateMode;

    /**
     * Gets the value of the nullFlavor property.
     * 
     * @return
     *     possible object is
     *     {@link CsNullFlavor }
     *     
     */
    public CsNullFlavor getNullFlavor() {
        return nullFlavor;
    }

    /**
     * Sets the value of the nullFlavor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CsNullFlavor }
     *     
     */
    public void setNullFlavor(CsNullFlavor value) {
        this.nullFlavor = value;
    }

    /**
     * Gets the value of the updateMode property.
     * 
     * @return
     *     possible object is
     *     {@link CsUpdateMode }
     *     
     */
    public CsUpdateMode getUpdateMode() {
        return updateMode;
    }

    /**
     * Sets the value of the updateMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CsUpdateMode }
     *     
     */
    public void setUpdateMode(CsUpdateMode value) {
        this.updateMode = value;
    }

}
