
package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * A telephone number (voice or fax), e-mail address, or other locator
 * for a resource (information or service) mediated by telecommunication
 * equipment. The address is specified as a Universal Resource Locator
 * (URL) qualified by time specification and use codes that help in
 * deciding which address to use for a given time and purpose.
 * 
 * 
 * &lt;p&gt;Java class for TEL complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="TEL"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}URL"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="useablePeriod" type="{urn:hl7-org:v3}IVL_TS" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="id" type="{urn:hl7-org:v3}II" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="use" type="{urn:hl7-org:v3}set_cs_TelecommunicationAddressUse" /&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TEL", propOrder = {
    "useablePeriod",
    "id"
})
public class TEL
    extends URL
{

    protected List<IVLTS> useablePeriod;
    protected II id;
    @XmlAttribute(name = "use")
    protected List<CsTelecommunicationAddressUse> use;

    /**
     * Gets the value of the useablePeriod property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the useablePeriod property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getUseablePeriod().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link IVLTS }
     * 
     * 
     */
    public List<IVLTS> getUseablePeriod() {
        if (useablePeriod == null) {
            useablePeriod = new ArrayList<>();
        }
        return this.useablePeriod;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link II }
     *     
     */
    public II getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link II }
     *     
     */
    public void setId(II value) {
        this.id = value;
    }

    /**
     * Gets the value of the use property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the use property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getUse().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link CsTelecommunicationAddressUse }
     * 
     * 
     */
    public List<CsTelecommunicationAddressUse> getUse() {
        if (use == null) {
            use = new ArrayList<>();
        }
        return this.use;
    }

}
