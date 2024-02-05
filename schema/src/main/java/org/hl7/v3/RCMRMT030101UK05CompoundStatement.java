
package org.hl7.v3;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * &lt;p&gt;Java class for RCMR_MT030101UK04.CompoundStatement complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="RCMR_MT030101UK04.CompoundStatement"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="id" type="{urn:hl7-org:v3}II" maxOccurs="2"/&amp;gt;
 *         &amp;lt;element name="code" type="{urn:hl7-org:v3}CD"/&amp;gt;
 *         &amp;lt;element name="statusCode" type="{urn:hl7-org:v3}CS"/&amp;gt;
 *         &amp;lt;element name="effectiveTime" type="{urn:hl7-org:v3}IVL_TS"/&amp;gt;
 *         &amp;lt;element name="availabilityTime" type="{urn:hl7-org:v3}TS"/&amp;gt;
 *         &amp;lt;element name="priorityCode" type="{urn:hl7-org:v3}CV" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="uncertaintyCode" type="{urn:hl7-org:v3}CV" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="specimen" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Specimen03" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="component" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Component02" maxOccurs="unbounded"/&amp;gt;
 *         &amp;lt;element name="informant" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Informant" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Participant" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Participant" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="replacementOf" type="{urn:hl7-org:v3}RCMR_MT030101UK04.ReplacementOf" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="reason" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Reason" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="reference" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Reference" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="sequelTo" type="{urn:hl7-org:v3}RCMR_MT030101UK04.SequelTo" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="type" type="{urn:hl7-org:v3}Classes" default="ActHeir" /&amp;gt;
 *       &amp;lt;attribute name="classCode" type="{urn:hl7-org:v3}ActClass" default="ACT" /&amp;gt;
 *       &amp;lt;attribute name="moodCode" type="{urn:hl7-org:v3}ActMood" default="EVN" /&amp;gt;
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
@XmlType(name = "RCMR_MT030101UK05.CompoundStatement", propOrder = {
    "id",
    "code",
    "statusCode",
    "effectiveTime",
    "availabilityTime",
    "priorityCode",
    "uncertaintyCode",
    "specimen",
    "component",
    "informant",
    "participant",
    "replacementOf",
    "reason",
    "reference",
    "sequelTo"
})
public class RCMRMT030101UK05CompoundStatement implements RCMRMT030101UKCompoundStatement {

    @XmlElement(required = true)
    protected List<II> id;

    @XmlElement(required = true)
    protected CD code;

    @XmlElement(required = true)
    protected CS statusCode;

    @XmlElement(required = true)
    protected IVLTS effectiveTime;

    @XmlElement(required = true)
    protected TS availabilityTime;
    protected CV priorityCode;
    protected CV uncertaintyCode;

    @XmlElement(type = RCMRMT030101UK04Specimen03.class)
    protected List<RCMRMT030101UKSpecimen03> specimen;

    @XmlElement(required = true, type = RCMRMT030101UK05Component02.class)
    protected List<RCMRMT030101UKComponent02> component;

    @XmlElement(type = RCMRMT030101UK05Informant.class)
    protected List<RCMRMT030101UKInformant> informant;

    @XmlElement(name = "Participant", type = RCMRMT030101UK05Participant.class)
    protected List<RCMRMT030101UKParticipant> participant;

    @XmlElement(type = RCMRMT030101UK05ReplacementOf.class)
    protected List<RCMRMT030101UKReplacementOf> replacementOf;

    @XmlElement(type = RCMRMT030101UK05Reason.class)
    protected List<RCMRMT030101UKReason> reason;

    @XmlElement(type = RCMRMT030101UK05Reference.class)
    protected List<RCMRMT030101UKReference> reference;

    @XmlElement(type = RCMRMT030101UK05SequelTo.class)
    protected List<RCMRMT030101UKSequelTo> sequelTo;

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;

    @XmlAttribute(name = "classCode")
    protected List<String> classCode;
    @XmlAttribute(name = "moodCode")
    protected List<String> moodCode;
    @XmlAttribute(name = "typeID")
    protected List<String> typeID;
    @XmlAttribute(name = "realmCode")
    protected List<String> realmCode;
    @XmlAttribute(name = "nullFlavor")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String nullFlavor;

    /**
     * Gets the value of the id property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the id property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getId().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link II }
     * 
     * 
     */
    @Override
    public List<II> getId() {
        if (id == null) {
            id = new ArrayList<II>();
        }
        return this.id;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link CD }
     *     
     */
    @Override
    public CD getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link CD }
     *     
     */
    @Override
    public void setCode(CD value) {
        this.code = value;
    }

    @Override
    public boolean hasCode() {
        return code != null;
    }

    /**
     * Gets the value of the statusCode property.
     * 
     * @return
     *     possible object is
     *     {@link CS }
     *     
     */
    @Override
    public CS getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the value of the statusCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CS }
     *     
     */
    @Override
    public void setStatusCode(CS value) {
        this.statusCode = value;
    }

    /**
     * Gets the value of the effectiveTime property.
     * 
     * @return
     *     possible object is
     *     {@link IVLTS }
     *     
     */
    @Override
    public IVLTS getEffectiveTime() {
        return effectiveTime;
    }

    @Override
    public boolean hasEffectiveTime() {
        return effectiveTime != null;
    }

    /**
     * Sets the value of the effectiveTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link IVLTS }
     *     
     */
    @Override
    public void setEffectiveTime(IVLTS value) {
        this.effectiveTime = value;
    }

    /**
     * Gets the value of the availabilityTime property.
     * 
     * @return
     *     possible object is
     *     {@link TS }
     *     
     */
    @Override
    public TS getAvailabilityTime() {
        return availabilityTime;
    }

    /**
     * Sets the value of the availabilityTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link TS }
     *     
     */
    @Override
    public void setAvailabilityTime(TS value) {
        this.availabilityTime = value;
    }

    /**
     * Gets the value of the priorityCode property.
     * 
     * @return
     *     possible object is
     *     {@link CV }
     *     
     */
    @Override
    public CV getPriorityCode() {
        return priorityCode;
    }

    /**
     * Sets the value of the priorityCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CV }
     *     
     */
    @Override
    public void setPriorityCode(CV value) {
        this.priorityCode = value;
    }

    /**
     * Gets the value of the uncertaintyCode property.
     * 
     * @return
     *     possible object is
     *     {@link CV }
     *     
     */
    @Override
    public CV getUncertaintyCode() {
        return uncertaintyCode;
    }

    /**
     * Sets the value of the uncertaintyCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CV }
     *     
     */
    @Override
    public void setUncertaintyCode(CV value) {
        this.uncertaintyCode = value;
    }

    /**
     * Gets the value of the specimen property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the specimen property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getSpecimen().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link RCMRMT030101UK04Specimen03 }
     * 
     * 
     */
    @Override
    public List<RCMRMT030101UKSpecimen03> getSpecimen() {
        if (specimen == null) {
            specimen = new ArrayList<>();
        }
        return this.specimen;
    }

    /**
     * Gets the value of the component property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the component property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getComponent().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link RCMRMT030101UK04Component02 }
     * 
     * 
     */
    @Override
    public List<RCMRMT030101UKComponent02> getComponent() {
        if (component == null) {
            component = new ArrayList<>();
        }
        return this.component;
    }

    /**
     * Gets the value of the informant property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the informant property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getInformant().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link RCMRMT030101UK04Informant }
     * 
     * 
     */
    @Override
    public List<RCMRMT030101UKInformant> getInformant() {
        if (informant == null) {
            informant = new ArrayList<>();
        }
        return this.informant;
    }

    /**
     * Gets the value of the participant property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the participant property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getParticipant().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link RCMRMT030101UK04Participant }
     * 
     * 
     */
    @Override
    public List<RCMRMT030101UKParticipant> getParticipant() {
        if (participant == null) {
            participant = new ArrayList<>();
        }
        return this.participant;
    }

    /**
     * Gets the value of the replacementOf property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the replacementOf property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getReplacementOf().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link RCMRMT030101UK04ReplacementOf }
     * 
     * 
     */
    @Override
    public List<RCMRMT030101UKReplacementOf> getReplacementOf() {
        if (replacementOf == null) {
            replacementOf = new ArrayList<>();
        }
        return this.replacementOf;
    }

    /**
     * Gets the value of the reason property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the reason property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getReason().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link RCMRMT030101UK04Reason }
     * 
     * 
     */
    @Override
    public List<RCMRMT030101UKReason> getReason() {
        if (reason == null) {
            reason = new ArrayList<>();
        }
        return this.reason;
    }

    /**
     * Gets the value of the reference property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the reference property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getReference().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link RCMRMT030101UK04Reference }
     * 
     * 
     */
    @Override
    public List<RCMRMT030101UKReference> getReference() {
        if (reference == null) {
            reference = new ArrayList<>();
        }
        return this.reference;
    }

    /**
     * Gets the value of the sequelTo property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the sequelTo property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getSequelTo().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link RCMRMT030101UK04SequelTo }
     * 
     * 
     */
    @Override
    public List<RCMRMT030101UKSequelTo> getSequelTo() {
        if (sequelTo == null) {
            sequelTo = new ArrayList<>();
        }
        return this.sequelTo;
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
            return "ActHeir";
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
     * Gets the value of the classCode property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the classCode property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getClassCode().add(newItem);
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
    public List<String> getClassCode() {
        if (classCode == null) {
            classCode = new ArrayList<>();
        }
        return this.classCode;
    }

    /**
     * Gets the value of the moodCode property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the moodCode property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getMoodCode().add(newItem);
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
    public List<String> getMoodCode() {
        if (moodCode == null) {
            moodCode = new ArrayList<>();
        }
        return this.moodCode;
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
