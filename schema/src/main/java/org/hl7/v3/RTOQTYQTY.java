
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for RTO_QTY_QTY complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="RTO_QTY_QTY"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}QTY"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="numerator"&amp;gt;
 *           &amp;lt;complexType&amp;gt;
 *             &amp;lt;complexContent&amp;gt;
 *               &amp;lt;extension base="{urn:hl7-org:v3}QTY"&amp;gt;
 *                 &amp;lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" default="1" /&amp;gt;
 *               &amp;lt;/extension&amp;gt;
 *             &amp;lt;/complexContent&amp;gt;
 *           &amp;lt;/complexType&amp;gt;
 *         &amp;lt;/element&amp;gt;
 *         &amp;lt;element name="denominator"&amp;gt;
 *           &amp;lt;complexType&amp;gt;
 *             &amp;lt;complexContent&amp;gt;
 *               &amp;lt;extension base="{urn:hl7-org:v3}QTY"&amp;gt;
 *                 &amp;lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" default="1" /&amp;gt;
 *               &amp;lt;/extension&amp;gt;
 *             &amp;lt;/complexContent&amp;gt;
 *           &amp;lt;/complexType&amp;gt;
 *         &amp;lt;/element&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RTO_QTY_QTY", propOrder = {
    "numerator",
    "denominator"
})
@XmlSeeAlso({
    RTO.class
})
public class RTOQTYQTY
    extends QTY
{

    @XmlElement(required = true)
    protected RTOQTYQTY.Numerator numerator;
    @XmlElement(required = true)
    protected RTOQTYQTY.Denominator denominator;

    /**
     * Gets the value of the numerator property.
     * 
     * @return
     *     possible object is
     *     {@link RTOQTYQTY.Numerator }
     *     
     */
    public RTOQTYQTY.Numerator getNumerator() {
        return numerator;
    }

    /**
     * Sets the value of the numerator property.
     * 
     * @param value
     *     allowed object is
     *     {@link RTOQTYQTY.Numerator }
     *     
     */
    public void setNumerator(RTOQTYQTY.Numerator value) {
        this.numerator = value;
    }

    /**
     * Gets the value of the denominator property.
     * 
     * @return
     *     possible object is
     *     {@link RTOQTYQTY.Denominator }
     *     
     */
    public RTOQTYQTY.Denominator getDenominator() {
        return denominator;
    }

    /**
     * Sets the value of the denominator property.
     * 
     * @param value
     *     allowed object is
     *     {@link RTOQTYQTY.Denominator }
     *     
     */
    public void setDenominator(RTOQTYQTY.Denominator value) {
        this.denominator = value;
    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;extension base="{urn:hl7-org:v3}QTY"&amp;gt;
     *       &amp;lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" default="1" /&amp;gt;
     *     &amp;lt;/extension&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Denominator
        extends QTY
    {

        @XmlAttribute(name = "value")
        @XmlSchemaType(name = "anySimpleType")
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
            if (value == null) {
                return "1";
            } else {
                return value;
            }
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


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;extension base="{urn:hl7-org:v3}QTY"&amp;gt;
     *       &amp;lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" default="1" /&amp;gt;
     *     &amp;lt;/extension&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Numerator
        extends QTY
    {

        @XmlAttribute(name = "value")
        @XmlSchemaType(name = "anySimpleType")
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
            if (value == null) {
                return "1";
            } else {
                return value;
            }
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

}
