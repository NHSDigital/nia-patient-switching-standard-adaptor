
package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java class for RCMR_MT030101UK04.Component02 complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="RCMR_MT030101UK04.Component02"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="sequenceNumber" type="{urn:hl7-org:v3}INT" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="seperatableInd" type="{urn:hl7-org:v3}BL" minOccurs="0"/&amp;gt;
 *         &amp;lt;choice&amp;gt;
 *           &amp;lt;element name="CompoundStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK04.CompoundStatement"/&amp;gt;
 *           &amp;lt;element name="EhrEmpty" type="{urn:hl7-org:v3}RCMR_MT030101UK04.EhrEmpty"/&amp;gt;
 *           &amp;lt;element name="LinkSet" type="{urn:hl7-org:v3}RCMR_MT030101UK04.LinkSet"/&amp;gt;
 *           &amp;lt;element name="MedicationStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK04.MedicationStatement"/&amp;gt;
 *           &amp;lt;element name="NarrativeStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK04.NarrativeStatement"/&amp;gt;
 *           &amp;lt;element name="ObservationStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK04.ObservationStatement"/&amp;gt;
 *           &amp;lt;element name="PlanStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK04.PlanStatement"/&amp;gt;
 *           &amp;lt;element name="RegistrationStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK04.RegistrationStatement"/&amp;gt;
 *           &amp;lt;element name="RequestStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK04.RequestStatement"/&amp;gt;
 *         &amp;lt;/choice&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="type" type="{urn:hl7-org:v3}Classes" default="ActRelationship" /&amp;gt;
 *       &amp;lt;attribute name="typeCode" type="{urn:hl7-org:v3}ActRelationshipType" default="COMP" /&amp;gt;
 *       &amp;lt;attribute name="contextConductionInd" type="{urn:hl7-org:v3}bl" default="true" /&amp;gt;
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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RCMR_MT030101UK04.Component02", propOrder = {
    "sequenceNumber",
    "seperatableInd",
    "compoundStatement",
    "ehrEmpty",
    "linkSet",
    "medicationStatement",
    "narrativeStatement",
    "observationStatement",
    "planStatement",
    "registrationStatement",
    "requestStatement"
})
public class RCMRMT030101UK04Component02 {

    protected INT sequenceNumber;
    protected BL seperatableInd;
    @XmlElement(name = "CompoundStatement")
    protected RCMRMT030101UK04CompoundStatement compoundStatement;
    @XmlElement(name = "EhrEmpty")
    protected RCMRMT030101UK04EhrEmpty ehrEmpty;
    @XmlElement(name = "LinkSet")
    protected RCMRMT030101UK04LinkSet linkSet;
    @XmlElement(name = "MedicationStatement")
    protected RCMRMT030101UK04MedicationStatement medicationStatement;
    @XmlElement(name = "NarrativeStatement")
    protected RCMRMT030101UK04NarrativeStatement narrativeStatement;
    @XmlElement(name = "ObservationStatement")
    protected RCMRMT030101UK04ObservationStatement observationStatement;
    @XmlElement(name = "PlanStatement")
    protected RCMRMT030101UK04PlanStatement planStatement;
    @XmlElement(name = "RegistrationStatement")
    protected RCMRMT030101UK04RegistrationStatement registrationStatement;
    @XmlElement(name = "RequestStatement")
    protected RCMRMT030101UK04RequestStatement requestStatement;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "typeCode")
    protected List<String> typeCode;
    @XmlAttribute(name = "contextConductionInd")
    protected Boolean contextConductionInd;
    @XmlAttribute(name = "typeID")
    protected List<String> typeID;
    @XmlAttribute(name = "realmCode")
    protected List<String> realmCode;
    @XmlAttribute(name = "nullFlavor")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String nullFlavor;

    /**
     * Gets the value of the sequenceNumber property.
     * 
     * @return
     *     possible object is
     *     {@link INT }
     *     
     */
    public INT getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the value of the sequenceNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link INT }
     *     
     */
    public void setSequenceNumber(INT value) {
        this.sequenceNumber = value;
    }

    /**
     * Gets the value of the seperatableInd property.
     * 
     * @return
     *     possible object is
     *     {@link BL }
     *     
     */
    public BL getSeperatableInd() {
        return seperatableInd;
    }

    /**
     * Sets the value of the seperatableInd property.
     * 
     * @param value
     *     allowed object is
     *     {@link BL }
     *     
     */
    public void setSeperatableInd(BL value) {
        this.seperatableInd = value;
    }

    /**
     * Gets the value of the compoundStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04CompoundStatement }
     *     
     */
    public RCMRMT030101UK04CompoundStatement getCompoundStatement() {
        return compoundStatement;
    }

    /**
     * Sets the value of the compoundStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04CompoundStatement }
     *     
     */
    public void setCompoundStatement(RCMRMT030101UK04CompoundStatement value) {
        this.compoundStatement = value;
    }

    /**
     * Gets the value of the ehrEmpty property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04EhrEmpty }
     *     
     */
    public RCMRMT030101UK04EhrEmpty getEhrEmpty() {
        return ehrEmpty;
    }

    /**
     * Sets the value of the ehrEmpty property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04EhrEmpty }
     *     
     */
    public void setEhrEmpty(RCMRMT030101UK04EhrEmpty value) {
        this.ehrEmpty = value;
    }

    /**
     * Gets the value of the linkSet property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04LinkSet }
     *     
     */
    public RCMRMT030101UK04LinkSet getLinkSet() {
        return linkSet;
    }

    /**
     * Sets the value of the linkSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04LinkSet }
     *     
     */
    public void setLinkSet(RCMRMT030101UK04LinkSet value) {
        this.linkSet = value;
    }

    /**
     * Gets the value of the medicationStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04MedicationStatement }
     *     
     */
    public RCMRMT030101UK04MedicationStatement getMedicationStatement() {
        return medicationStatement;
    }

    /**
     * Sets the value of the medicationStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04MedicationStatement }
     *     
     */
    public void setMedicationStatement(RCMRMT030101UK04MedicationStatement value) {
        this.medicationStatement = value;
    }

    /**
     * Gets the value of the narrativeStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04NarrativeStatement }
     *     
     */
    public RCMRMT030101UK04NarrativeStatement getNarrativeStatement() {
        return narrativeStatement;
    }

    /**
     * Sets the value of the narrativeStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04NarrativeStatement }
     *     
     */
    public void setNarrativeStatement(RCMRMT030101UK04NarrativeStatement value) {
        this.narrativeStatement = value;
    }

    /**
     * Gets the value of the observationStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04ObservationStatement }
     *     
     */
    public RCMRMT030101UK04ObservationStatement getObservationStatement() {
        return observationStatement;
    }

    /**
     * Sets the value of the observationStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04ObservationStatement }
     *     
     */
    public void setObservationStatement(RCMRMT030101UK04ObservationStatement value) {
        this.observationStatement = value;
    }

    /**
     * Gets the value of the planStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04PlanStatement }
     *     
     */
    public RCMRMT030101UK04PlanStatement getPlanStatement() {
        return planStatement;
    }

    /**
     * Sets the value of the planStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04PlanStatement }
     *     
     */
    public void setPlanStatement(RCMRMT030101UK04PlanStatement value) {
        this.planStatement = value;
    }

    /**
     * Gets the value of the registrationStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04RegistrationStatement }
     *     
     */
    public RCMRMT030101UK04RegistrationStatement getRegistrationStatement() {
        return registrationStatement;
    }

    /**
     * Sets the value of the registrationStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04RegistrationStatement }
     *     
     */
    public void setRegistrationStatement(RCMRMT030101UK04RegistrationStatement value) {
        this.registrationStatement = value;
    }

    /**
     * Gets the value of the requestStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04RequestStatement }
     *     
     */
    public RCMRMT030101UK04RequestStatement getRequestStatement() {
        return requestStatement;
    }

    /**
     * Sets the value of the requestStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04RequestStatement }
     *     
     */
    public void setRequestStatement(RCMRMT030101UK04RequestStatement value) {
        this.requestStatement = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
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
    public List<String> getTypeCode() {
        if (typeCode == null) {
            typeCode = new ArrayList<String>();
        }
        return this.typeCode;
    }

    /**
     * Gets the value of the contextConductionInd property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isContextConductionInd() {
        if (contextConductionInd == null) {
            return true;
        } else {
            return contextConductionInd;
        }
    }

    /**
     * Sets the value of the contextConductionInd property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContextConductionInd(Boolean value) {
        this.contextConductionInd = value;
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
    public List<String> getTypeID() {
        if (typeID == null) {
            typeID = new ArrayList<String>();
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
    public List<String> getRealmCode() {
        if (realmCode == null) {
            realmCode = new ArrayList<String>();
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
    public void setNullFlavor(String value) {
        this.nullFlavor = value;
    }

}
