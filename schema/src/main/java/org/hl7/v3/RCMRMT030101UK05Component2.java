
package org.hl7.v3;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * &lt;p&gt;Java class for RCMR_MT030101UK04.Component2 complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="RCMR_MT030101UK04.Component2"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;choice&amp;gt;
 *           &amp;lt;element name="ehrSupplyAuthorise" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Authorise"/&amp;gt;
 *           &amp;lt;element name="ehrSupplyDiscontinue" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Discontinue"/&amp;gt;
 *           &amp;lt;element name="ehrSupplyDispense" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Dispense"/&amp;gt;
 *           &amp;lt;element name="ehrSupplyPrescribe" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Prescribe"/&amp;gt;
 *         &amp;lt;/choice&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="type" type="{urn:hl7-org:v3}Classes" default="ActRelationship" /&amp;gt;
 *       &amp;lt;attribute name="typeCode" type="{urn:hl7-org:v3}ActRelationshipType" default="COMP" /&amp;gt;
 *       &amp;lt;attribute name="typeID"&amp;gt;
 *         &amp;lt;simpleType&amp;gt;
 *           &amp;lt;list itemType="{urn:hl7-org:v3}oid" /&amp;gt;
 *         &amp;lt;/simpleType&amp;gt;
 *       &amp;lt;/attribute&amp;gt;
 *       &amp;lt;attribute name="realmCode"&amp;gt;
 *         &amp;lt;simpleType&amp;gt;
 *           &amp;lt;list itemType="{urn:hl7-org:v3}cs" /&amp;gt;
 *         &amp;lt;/simpleType&amp;gt;
 *       &amp;lt;/attribute&amp;gt;
 *       &amp;lt;attribute name="nullFlavor" type="{urn:hl7-org:v3}cs" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RCMR_MT030101UK05.Component2", propOrder = {
    "ehrSupplyAuthorise",
    "ehrSupplyDiscontinue",
    "ehrSupplyDispense",
    "ehrSupplyPrescribe"
})
public class RCMRMT030101UK05Component2 implements RCMRMT030101UKComponent2 {

    @XmlElement(type = RCMRMT030101UK05Authorise.class)
    protected RCMRMT030101UKAuthorise ehrSupplyAuthorise;

    @XmlElement(type = RCMRMT030101UK05Discontinue.class)
    protected RCMRMT030101UKDiscontinue ehrSupplyDiscontinue;

    @XmlElement(type = RCMRMT030101UK05Dispense.class)
    protected RCMRMT030101UKDispense ehrSupplyDispense;

    @XmlElement(type = RCMRMT030101UK05Prescribe.class)
    protected RCMRMT030101UKPrescribe ehrSupplyPrescribe;

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;

    @XmlAttribute(name = "typeCode")
    protected List<String> typeCode;

    @XmlAttribute(name = "typeID")
    protected List<String> typeID;
    @XmlAttribute(name = "realmCode")
    protected List<String> realmCode;
    @XmlAttribute(name = "nullFlavor")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String nullFlavor;

    /**
     * Gets the value of the ehrSupplyAuthorise property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04Authorise }
     *     
     */
    @Override
    public RCMRMT030101UKAuthorise getEhrSupplyAuthorise() {
        return ehrSupplyAuthorise;
    }

    /**
     * Sets the value of the ehrSupplyAuthorise property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04Authorise }
     *     
     */
    @Override
    public void setEhrSupplyAuthorise(RCMRMT030101UKAuthorise value) {
        this.ehrSupplyAuthorise = value;
    }

    @Override
    public boolean hasEhrSupplyAuthorise() {
        return ehrSupplyAuthorise != null;
    }
    /**
     * Gets the value of the ehrSupplyDiscontinue property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04Discontinue }
     *     
     */
    @Override
    public RCMRMT030101UKDiscontinue getEhrSupplyDiscontinue() {
        return ehrSupplyDiscontinue;
    }

    /**
     * Sets the value of the ehrSupplyDiscontinue property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04Discontinue }
     *     
     */
    @Override
    public void setEhrSupplyDiscontinue(RCMRMT030101UKDiscontinue value) {
        this.ehrSupplyDiscontinue = value;
    }

    @Override
    public boolean hasEhrSupplyDiscontinue() {
        return ehrSupplyDiscontinue != null;
    }

    /**
     * Gets the value of the ehrSupplyDispense property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04Dispense }
     *     
     */
    @Override
    public RCMRMT030101UKDispense getEhrSupplyDispense() {
        return ehrSupplyDispense;
    }

    /**
     * Sets the value of the ehrSupplyDispense property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04Dispense }
     *     
     */
    @Override
    public void setEhrSupplyDispense(RCMRMT030101UKDispense value) {
        this.ehrSupplyDispense = value;
    }

    /**
     * Gets the value of the ehrSupplyPrescribe property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04Prescribe }
     *     
     */
    @Override
    public RCMRMT030101UKPrescribe getEhrSupplyPrescribe() {
        return ehrSupplyPrescribe;
    }

    /**
     * Sets the value of the ehrSupplyPrescribe property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04Prescribe }
     *     
     */
    @Override
    public void setEhrSupplyPrescribe(RCMRMT030101UKPrescribe value) {
        this.ehrSupplyPrescribe = value;
    }

    @Override
    public boolean hasEhrSupplyPrescribe() {
        return ehrSupplyPrescribe != null;
    }
    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
    public String getType() {
        if (type == null) {
            return "ActRelationship";
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the typeCode property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the typeCode property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getTypeCode().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    @Override
    public List<String> getTypeCode() {
        if (typeCode == null) {
            typeCode = new ArrayList<>();
        }
        return this.typeCode;
    }

    /**
     * Gets the value of the typeID property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the typeID property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getTypeID().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    @Override
    public List<String> getTypeID() {
        if (typeID == null) {
            typeID = new ArrayList<>();
        }
        return this.typeID;
    }

    /**
     * Gets the value of the realmCode property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the realmCode property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getRealmCode().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    @Override
    public List<String> getRealmCode() {
        if (realmCode == null) {
            realmCode = new ArrayList<>();
        }
        return this.realmCode;
    }

    /**
     * Gets the value of the nullFlavor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Override
    public String getNullFlavor() {
        return nullFlavor;
    }

    /**
     * Sets the value of the nullFlavor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Override
    public void setNullFlavor(String value) {
        this.nullFlavor = value;
    }

}
