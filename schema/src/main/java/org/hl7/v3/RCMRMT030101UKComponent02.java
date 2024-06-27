
package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java class for RCMR_MT030101UK.Component02 complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="RCMR_MT030101UK.Component02"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="sequenceNumber" type="{urn:hl7-org:v3}INT" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="seperatableInd" type="{urn:hl7-org:v3}BL" minOccurs="0"/&amp;gt;
 *         &amp;lt;choice&amp;gt;
 *           &amp;lt;element name="CompoundStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK.CompoundStatement"/&amp;gt;
 *           &amp;lt;element name="EhrEmpty" type="{urn:hl7-org:v3}RCMR_MT030101UK.EhrEmpty"/&amp;gt;
 *           &amp;lt;element name="LinkSet" type="{urn:hl7-org:v3}RCMR_MT030101UK.LinkSet"/&amp;gt;
 *           &amp;lt;element name="MedicationStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK.MedicationStatement"/&amp;gt;
 *           &amp;lt;element name="NarrativeStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK.NarrativeStatement"/&amp;gt;
 *           &amp;lt;element name="ObservationStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK.ObservationStatement"/&amp;gt;
 *           &amp;lt;element name="PlanStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK.PlanStatement"/&amp;gt;
 *           &amp;lt;element name="RegistrationStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK.RegistrationStatement"/&amp;gt;
 *           &amp;lt;element name="RequestStatement" type="{urn:hl7-org:v3}RCMR_MT030101UK.RequestStatement"/&amp;gt;
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
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RCMR_MT030101UK.Component02", propOrder = {
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
public class RCMRMT030101UKComponent02 {

    protected INT sequenceNumber;
    protected BL seperatableInd;

    @XmlElement(name = "CompoundStatement", type = RCMRMT030101UKCompoundStatement.class)
    protected RCMRMT030101UKCompoundStatement compoundStatement;

    @XmlElement(name = "EhrEmpty", type = RCMRMT030101UKEhrEmpty.class)
    protected RCMRMT030101UKEhrEmpty ehrEmpty;

    @XmlElement(name = "LinkSet", type = RCMRMT030101UKLinkSet.class)
    protected RCMRMT030101UKLinkSet linkSet;

    @XmlElement(name = "MedicationStatement", type = RCMRMT030101UKMedicationStatement.class)
    protected RCMRMT030101UKMedicationStatement medicationStatement;

    @XmlElement(name = "NarrativeStatement", type = RCMRMT030101UKNarrativeStatement.class)
    protected RCMRMT030101UKNarrativeStatement narrativeStatement;

    @XmlElement(name = "ObservationStatement", type = RCMRMT030101UKObservationStatement.class)
    protected RCMRMT030101UKObservationStatement observationStatement;

    @XmlElement(name = "PlanStatement", type = RCMRMT030101UKPlanStatement.class)
    protected RCMRMT030101UKPlanStatement planStatement;

    @XmlElement(name = "RegistrationStatement", type = RCMRMT030101UKRegistrationStatement.class)
    protected RCMRMT030101UKRegistrationStatement registrationStatement;

    @XmlElement(name = "RequestStatement", type = RCMRMT030101UKRequestStatement.class)
    protected RCMRMT030101UKRequestStatement requestStatement;
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
     *     {@link RCMRMT030101UKCompoundStatement }
     *     
     */
    public RCMRMT030101UKCompoundStatement getCompoundStatement() {
        return compoundStatement;
    }

    /**
     * Sets the value of the compoundStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKCompoundStatement }
     *     
     */
    public void setCompoundStatement(RCMRMT030101UKCompoundStatement value) {
        this.compoundStatement = value;
    }

    public boolean hasCompoundStatement() {
        return compoundStatement != null;
    }

    /**
     * Gets the value of the ehrEmpty property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKEhrEmpty }
     *     
     */
    public RCMRMT030101UKEhrEmpty getEhrEmpty() {
        return ehrEmpty;
    }

    /**
     * Sets the value of the ehrEmpty property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKEhrEmpty }
     *     
     */
    public void setEhrEmpty(RCMRMT030101UKEhrEmpty value) {
        this.ehrEmpty = value;
    }

    public boolean hasEhrEmpty() {
        return ehrEmpty != null;
    }

    /**
     * Gets the value of the linkSet property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKLinkSet }
     *     
     */
    public RCMRMT030101UKLinkSet getLinkSet() {
        return linkSet;
    }

    /**
     * Sets the value of the linkSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKLinkSet }
     *     
     */
    public void setLinkSet(RCMRMT030101UKLinkSet value) {
        this.linkSet = value;
    }

    public boolean hasLinkSet() {
        return linkSet != null;
    }

    /**
     * Gets the value of the medicationStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKMedicationStatement }
     *     
     */
    public RCMRMT030101UKMedicationStatement getMedicationStatement() {
        return medicationStatement;
    }

    /**
     * Sets the value of the medicationStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKMedicationStatement }
     *     
     */
    public void setMedicationStatement(RCMRMT030101UKMedicationStatement value) {
        this.medicationStatement = value;
    }

    public boolean hasMedicationStatement() {
        return medicationStatement != null;
    }

    /**
     * Gets the value of the narrativeStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKNarrativeStatement }
     *     
     */
    public RCMRMT030101UKNarrativeStatement getNarrativeStatement() {
        return narrativeStatement;
    }

    /**
     * Sets the value of the narrativeStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKNarrativeStatement }
     *     
     */
    public void setNarrativeStatement(RCMRMT030101UKNarrativeStatement value) {
        this.narrativeStatement = value;
    }

    public boolean hasNarrativeStatement() {
        return narrativeStatement != null;
    }

    /**
     * Gets the value of the observationStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKObservationStatement }
     *     
     */
    public RCMRMT030101UKObservationStatement getObservationStatement() {
        return observationStatement;
    }

    /**
     * Sets the value of the observationStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKObservationStatement }
     *     
     */
    public void setObservationStatement(RCMRMT030101UKObservationStatement value) {
        this.observationStatement = value;
    }

    public boolean hasObservationStatement() {
        return observationStatement != null;
    }

    /**
     * Gets the value of the planStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKPlanStatement }
     *     
     */
    public RCMRMT030101UKPlanStatement getPlanStatement() {
        return planStatement;
    }

    /**
     * Sets the value of the planStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKPlanStatement }
     *     
     */
    public void setPlanStatement(RCMRMT030101UKPlanStatement value) {
        this.planStatement = value;
    }

    public boolean hasPlanStatement() {
        return planStatement != null;
    }

    /**
     * Gets the value of the registrationStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKRegistrationStatement }
     *     
     */
    public RCMRMT030101UKRegistrationStatement getRegistrationStatement() {
        return registrationStatement;
    }

    /**
     * Sets the value of the registrationStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKRegistrationStatement }
     *     
     */
    public void setRegistrationStatement(RCMRMT030101UKRegistrationStatement value) {
        this.registrationStatement = value;
    }
    
    public boolean hasRegistrationStatement() {
        return registrationStatement != null;
    }

    /**
     * Gets the value of the requestStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKRequestStatement }
     *     
     */
    public RCMRMT030101UKRequestStatement getRequestStatement() {
        return requestStatement;
    }

    /**
     * Sets the value of the requestStatement property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKRequestStatement }
     *     
     */
    public void setRequestStatement(RCMRMT030101UKRequestStatement value) {
        this.requestStatement = value;
    }

    public boolean hasRequestStatement() {
        return requestStatement != null;
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
        return Objects.requireNonNullElse(type, "ActRelationship");
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
            typeCode = new ArrayList<>();
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
        return Objects.requireNonNullElse(contextConductionInd, true);
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
