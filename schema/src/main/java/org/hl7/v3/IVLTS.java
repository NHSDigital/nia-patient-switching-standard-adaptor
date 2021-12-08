
package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for IVL_TS complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="IVL_TS"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}SXCM_TS"&amp;gt;
 *       &amp;lt;choice minOccurs="0"&amp;gt;
 *         &amp;lt;sequence&amp;gt;
 *           &amp;lt;element name="low" type="{urn:hl7-org:v3}IVXB_TS"/&amp;gt;
 *           &amp;lt;choice minOccurs="0"&amp;gt;
 *             &amp;lt;element name="width" type="{urn:hl7-org:v3}PQ" minOccurs="0"/&amp;gt;
 *             &amp;lt;element name="high" type="{urn:hl7-org:v3}IVXB_TS" minOccurs="0"/&amp;gt;
 *           &amp;lt;/choice&amp;gt;
 *         &amp;lt;/sequence&amp;gt;
 *         &amp;lt;element name="high" type="{urn:hl7-org:v3}IVXB_TS"/&amp;gt;
 *         &amp;lt;sequence&amp;gt;
 *           &amp;lt;element name="width" type="{urn:hl7-org:v3}PQ"/&amp;gt;
 *           &amp;lt;element name="high" type="{urn:hl7-org:v3}IVXB_TS" minOccurs="0"/&amp;gt;
 *         &amp;lt;/sequence&amp;gt;
 *         &amp;lt;sequence&amp;gt;
 *           &amp;lt;element name="center" type="{urn:hl7-org:v3}TS"/&amp;gt;
 *           &amp;lt;element name="width" type="{urn:hl7-org:v3}PQ" minOccurs="0"/&amp;gt;
 *         &amp;lt;/sequence&amp;gt;
 *       &amp;lt;/choice&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IVL_TS", propOrder = {
    "rest"
})
public class IVLTS
    extends SXCMTS
{

    @XmlElementRefs({
        @XmlElementRef(name = "low", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "width", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "high", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "center", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<? extends QTY>> rest;

    /**
     * Gets the rest of the content model. 
     * 
     * &lt;p&gt;
     * You are getting this "catch-all" property because of the following reason: 
     * The field name "High" is used by two different parts of a schema. See: 
     * line 2035 of file:/Users/malgorzatar/nhs/nia-patient-switching-standard-adaptor/schema/src/main/resources/dt/datatypes.xsd
     * line 2025 of file:/Users/malgorzatar/nhs/nia-patient-switching-standard-adaptor/schema/src/main/resources/dt/datatypes.xsd
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
     * {@link JAXBElement }{@code <}{@link IVXBTS }{@code >}
     * {@link JAXBElement }{@code <}{@link PQ }{@code >}
     * {@link JAXBElement }{@code <}{@link IVXBTS }{@code >}
     * {@link JAXBElement }{@code <}{@link TS }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends QTY>> getRest() {
        if (rest == null) {
            rest = new ArrayList<JAXBElement<? extends QTY>>();
        }
        return this.rest;
    }

}
