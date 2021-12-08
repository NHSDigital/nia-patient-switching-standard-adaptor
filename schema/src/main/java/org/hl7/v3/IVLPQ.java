
package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;


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
    "rest"
})
public class IVLPQ
    extends ANY
{

    @XmlElementRefs({
        @XmlElementRef(name = "low", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "width", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "high", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "center", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> rest;
    @XmlAttribute(name = "operator")
    protected CsSetOperator operator;

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
            rest = new ArrayList<JAXBElement<?>>();
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

}
