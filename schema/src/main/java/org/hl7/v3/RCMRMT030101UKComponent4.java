
package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * &lt;p&gt;Java class for RCMR_MT030101UK04.Component4 complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="RCMR_MT030101UK04.Component4"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
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
@XmlType(name = "RCMR_MT030101UK04.Component4", propOrder = {
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
public class RCMRMT030101UKComponent4 implements org.hl7.v3.deprecated.RCMRMT030101UKComponent4 {

    @XmlElement(name = "CompoundStatement", type = RCMRMT030101UKCompoundStatement.class)
    protected org.hl7.v3.deprecated.RCMRMT030101UKCompoundStatement compoundStatement;

    @XmlElement(name = "EhrEmpty", type = RCMRMT030101UKEhrEmpty.class)
    protected org.hl7.v3.deprecated.RCMRMT030101UKEhrEmpty ehrEmpty;

    @XmlElement(name = "LinkSet", type = RCMRMT030101UKLinkSet.class)
    protected org.hl7.v3.deprecated.RCMRMT030101UKLinkSet linkSet;

    @XmlElement(name = "MedicationStatement", type = RCMRMT030101UKMedicationStatement.class)
    protected org.hl7.v3.deprecated.RCMRMT030101UKMedicationStatement medicationStatement;

    @XmlElement(name = "NarrativeStatement", type = RCMRMT030101UKNarrativeStatement.class)
    protected org.hl7.v3.deprecated.RCMRMT030101UKNarrativeStatement narrativeStatement;

    @XmlElement(name = "ObservationStatement", type = RCMRMT030101UKObservationStatement.class)
    protected org.hl7.v3.deprecated.RCMRMT030101UKObservationStatement observationStatement;

    @XmlElement(name = "PlanStatement", type = RCMRMT030101UKPlanStatement.class)
    protected org.hl7.v3.deprecated.RCMRMT030101UKPlanStatement planStatement;

    @XmlElement(name = "RegistrationStatement", type = RCMRMT030101UKRegistrationStatement.class)
    protected org.hl7.v3.deprecated.RCMRMT030101UKRegistrationStatement registrationStatement;

    @XmlElement(name = "RequestStatement", type = RCMRMT030101UKRequestStatement.class)
    protected org.hl7.v3.deprecated.RCMRMT030101UKRequestStatement requestStatement;

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
     * Gets the value of the compoundStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKCompoundStatement }
     *     
     */
    @Override
    public org.hl7.v3.deprecated.RCMRMT030101UKCompoundStatement getCompoundStatement() {
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
    @Override
    public void setCompoundStatement(org.hl7.v3.deprecated.RCMRMT030101UKCompoundStatement value) {
        this.compoundStatement = value;
    }

    @Override
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
    @Override
    public org.hl7.v3.deprecated.RCMRMT030101UKEhrEmpty getEhrEmpty() {
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
    @Override
    public void setEhrEmpty(org.hl7.v3.deprecated.RCMRMT030101UKEhrEmpty value) {
        this.ehrEmpty = value;
    }

    /**
     * Gets the value of the linkSet property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKLinkSet }
     *     
     */
    @Override
    public org.hl7.v3.deprecated.RCMRMT030101UKLinkSet getLinkSet() {
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
    @Override
    public void setLinkSet(org.hl7.v3.deprecated.RCMRMT030101UKLinkSet value) {
        this.linkSet = value;
    }

    /**
     * Gets the value of the medicationStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKMedicationStatement }
     *     
     */
    @Override
    public org.hl7.v3.deprecated.RCMRMT030101UKMedicationStatement getMedicationStatement() {
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
    @Override
    public void setMedicationStatement(org.hl7.v3.deprecated.RCMRMT030101UKMedicationStatement value) {
        this.medicationStatement = value;
    }

    @Override
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
    @Override
    public org.hl7.v3.deprecated.RCMRMT030101UKNarrativeStatement getNarrativeStatement() {
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
    @Override
    public void setNarrativeStatement(org.hl7.v3.deprecated.RCMRMT030101UKNarrativeStatement value) {
        this.narrativeStatement = value;
    }

    /**
     * Gets the value of the observationStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKObservationStatement }
     *     
     */
    @Override
    public org.hl7.v3.deprecated.RCMRMT030101UKObservationStatement getObservationStatement() {
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
    @Override
    public void setObservationStatement(org.hl7.v3.deprecated.RCMRMT030101UKObservationStatement value) {
        this.observationStatement = value;
    }

    /**
     * Gets the value of the planStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKPlanStatement }
     *     
     */
    @Override
    public org.hl7.v3.deprecated.RCMRMT030101UKPlanStatement getPlanStatement() {
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
    @Override
    public void setPlanStatement(org.hl7.v3.deprecated.RCMRMT030101UKPlanStatement value) {
        this.planStatement = value;
    }

    /**
     * Gets the value of the registrationStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKRegistrationStatement }
     *     
     */
    @Override
    public org.hl7.v3.deprecated.RCMRMT030101UKRegistrationStatement getRegistrationStatement() {
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
    @Override
    public void setRegistrationStatement(org.hl7.v3.deprecated.RCMRMT030101UKRegistrationStatement value) {
        this.registrationStatement = value;
    }

    /**
     * Gets the value of the requestStatement property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKRequestStatement }
     *     
     */
    @Override
    public org.hl7.v3.deprecated.RCMRMT030101UKRequestStatement getRequestStatement() {
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
    @Override
    public void setRequestStatement(org.hl7.v3.deprecated.RCMRMT030101UKRequestStatement value) {
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
