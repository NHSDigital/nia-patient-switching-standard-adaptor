
package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * &lt;p&gt;Java class for IVL_PQ complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="IVL_PQ"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}ANY"&amp;gt;
 *       &amp;lt;choice minOccurs="0"&amp;gt;
 *         &amp;lt;sequence&amp;gt;
 *           &amp;lt;element name="low" type="{urn:hl7-org:v3}PQ_inc"/&amp;gt;
 *           &amp;lt;choice minOccurs="0"&amp;gt;
 *             &amp;lt;element name="width" type="{urn:hl7-org:v3}PQ"/&amp;gt;
 *             &amp;lt;element name="high" type="{urn:hl7-org:v3}PQ_inc"/&amp;gt;
 *           &amp;lt;/choice&amp;gt;
 *         &amp;lt;/sequence&amp;gt;
 *         &amp;lt;element name="high" type="{urn:hl7-org:v3}PQ_inc"/&amp;gt;
 *         &amp;lt;sequence&amp;gt;
 *           &amp;lt;element name="width" type="{urn:hl7-org:v3}PQ"/&amp;gt;
 *           &amp;lt;element name="high" type="{urn:hl7-org:v3}PQ_inc" minOccurs="0"/&amp;gt;
 *         &amp;lt;/sequence&amp;gt;
 *         &amp;lt;sequence&amp;gt;
 *           &amp;lt;element name="center" type="{urn:hl7-org:v3}PQ"/&amp;gt;
 *           &amp;lt;element name="width" type="{urn:hl7-org:v3}PQ" minOccurs="0"/&amp;gt;
 *         &amp;lt;/sequence&amp;gt;
 *       &amp;lt;/choice&amp;gt;
 *       &amp;lt;attribute name="operator" type="{urn:hl7-org:v3}cs_SetOperator" default="I" /&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IVL_PQ", propOrder = {
    "rest",
    "low",
    "width",
    "high",
    "center"
})
public class IVLPQ
    extends ANY
{

    protected List<JAXBElement<?>> rest;
    @XmlAttribute(name = "operator")
    protected CsSetOperator operator;
    protected PQInc low;
    protected PQInc width;
    protected PQInc high;
    protected PQInc center;

    /**
     * Gets the rest of the content model. 
     * 
     * &lt;p&gt;
     * You are getting this "catch-all" property because of the following reason: 
     * The field name "High" is used by two different parts of a schema. See: 
     * line 2116 of file:/Users/malgorzatar/nhs/nia-patient-switching-standard-adaptor/schema/src/main/resources/dt/datatypes.xsd
     * line 2113 of file:/Users/malgorzatar/nhs/nia-patient-switching-standard-adaptor/schema/src/main/resources/dt/datatypes.xsd
     * &lt;p&gt;
     * To get rid of this property, apply a property customization to one 
     * of both of the following declarations to change their names: 
     * Gets the value of the rest property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the rest property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getRest().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link PQInc }{@code >}
     * {@link JAXBElement }{@code <}{@link PQ }{@code >}
     * {@link JAXBElement }{@code <}{@link PQInc }{@code >}
     * {@link JAXBElement }{@code <}{@link PQ }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getRest() {
        if (rest == null) {
            rest = new ArrayList<>();
        }
        return this.rest;
    }

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link CsSetOperator }
     *     
     */
    public CsSetOperator getOperator() {
        if (operator == null) {
            return CsSetOperator.I;
        } else {
            return operator;
        }
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link CsSetOperator }
     *     
     */
    public void setOperator(CsSetOperator value) {
        this.operator = value;
    }

    public PQInc getLow() {
        return low;
    }

    public void setLow(PQInc low) {
        this.low = low;
    }

    public PQInc getWidth() {
        return width;
    }

    public void setWidth(PQInc width) {
        this.width = width;
    }

    public PQInc getHigh() {
        return high;
    }

    public void setHigh(PQInc high) {
        this.high = high;
    }

    public PQInc getCenter() {
        return center;
    }

    public void setCenter(PQInc center) {
        this.center = center;
    }
}
