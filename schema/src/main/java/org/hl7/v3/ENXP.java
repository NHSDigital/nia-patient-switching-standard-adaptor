
package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * A character string token representing a part of a name. May have a
 * type code signifying the role of the part in the whole entity name,
 * and a qualifier code for more detail about the name part type.
 * Typical name parts for person names are given names, and family names,
 * titles, etc.
 * 
 * 
 * &lt;p&gt;Java class for ENXP complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="ENXP"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}ST"&amp;gt;
 *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_EntityNamePartType" /&amp;gt;
 *       &amp;lt;attribute name="qualifier" type="{urn:hl7-org:v3}set_cs_EntityNamePartQualifier" /&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ENXP")
@XmlSeeAlso({
    EnDelimiter.class,
    EnFamily.class,
    EnGiven.class,
    EnPrefix.class,
    EnSuffix.class
})
public class ENXP
    extends ED
{

    @XmlAttribute(name = "partType")
    protected CsEntityNamePartType partType;
    @XmlAttribute(name = "qualifier")
    protected List<CsEntityNamePartQualifier> qualifier;

    /**
     * Gets the value of the partType property.
     * 
     * @return
     *     possible object is
     *     {@link CsEntityNamePartType }
     *     
     */
    public CsEntityNamePartType getPartType() {
        return partType;
    }

    /**
     * Sets the value of the partType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CsEntityNamePartType }
     *     
     */
    public void setPartType(CsEntityNamePartType value) {
        this.partType = value;
    }

    /**
     * Gets the value of the qualifier property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the qualifier property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getQualifier().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link CsEntityNamePartQualifier }
     * 
     * 
     */
    public List<CsEntityNamePartQualifier> getQualifier() {
        if (qualifier == null) {
            qualifier = new ArrayList<>();
        }
        return this.qualifier;
    }

}
