
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
 * &lt;p&gt;Java class for RCMR_MT030101UK04.ObservationStatement complex type.
 *
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 *
 * &lt;pre&gt;
 * &amp;lt;complexType name="RCMR_MT030101UK04.ObservationStatement"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="id" type="{urn:hl7-org:v3}II"/&amp;gt;
 *         &amp;lt;element name="code" type="{urn:hl7-org:v3}CD"/&amp;gt;
 *         &amp;lt;element name="statusCode" type="{urn:hl7-org:v3}CS"/&amp;gt;
 *         &amp;lt;element name="effectiveTime" type="{urn:hl7-org:v3}IVL_TS"/&amp;gt;
 *         &amp;lt;element name="availabilityTime" type="{urn:hl7-org:v3}TS"/&amp;gt;
 *         &amp;lt;element name="priorityCode" type="{urn:hl7-org:v3}CV" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="uncertaintyCode" type="{urn:hl7-org:v3}CV" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="value" type="{urn:hl7-org:v3}ANY" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="interpretationCode" type="{urn:hl7-org:v3}CV" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="subject" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Subject" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="specimen" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Specimen" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="pertinentInformation" type="{urn:hl7-org:v3}RCMR_MT030101UK04.PertinentInformation02" maxOccurs="3" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="referenceRange" type="{urn:hl7-org:v3}RCMR_MT030101UK04.ReferenceRange" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="informant" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Informant" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Participant" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Participant" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="replacementOf" type="{urn:hl7-org:v3}RCMR_MT030101UK04.ReplacementOf" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="reason" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Reason" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="reference" type="{urn:hl7-org:v3}RCMR_MT030101UK04.Reference" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="sequelTo" type="{urn:hl7-org:v3}RCMR_MT030101UK04.SequelTo" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="type" type="{urn:hl7-org:v3}Classes" default="Observation" /&amp;gt;
 *       &amp;lt;attribute name="classCode" type="{urn:hl7-org:v3}ActClass" default="OBS" /&amp;gt;
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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RCMR_MT030101UK04.ObservationStatement", propOrder = {
    "id",
    "code",
    "statusCode",
    "effectiveTime",
    "availabilityTime",
    "priorityCode",
    "uncertaintyCode",
    "value",
    "interpretationCode",
    "subject",
    "specimen",
    "pertinentInformation",
    "referenceRange",
    "informant",
    "participant",
    "replacementOf",
    "reason",
    "reference",
    "sequelTo"
})
public class RCMRMT030101UK04ObservationStatement {

    @XmlElement(required = true)
    protected II id;
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
    @XmlJavaTypeAdapter(ValueAdapter.class)
    protected Object value;
    protected CV interpretationCode;
    protected RCMRMT030101UK04Subject subject;
    protected List<RCMRMT030101UK04Specimen> specimen;
    protected List<RCMRMT030101UK04PertinentInformation02> pertinentInformation;
    protected List<RCMRMT030101UK04ReferenceRange> referenceRange;
    protected List<RCMRMT030101UK04Informant> informant;
    @XmlElement(name = "Participant")
    protected List<RCMRMT030101UK04Participant> participant;
    protected List<RCMRMT030101UK04ReplacementOf> replacementOf;
    protected List<RCMRMT030101UK04Reason> reason;
    protected List<RCMRMT030101UK04Reference> reference;
    protected List<RCMRMT030101UK04SequelTo> sequelTo;
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
     * Gets the value of the code property.
     *
     * @return
     *     possible object is
     *     {@link CD }
     *
     */
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
    public void setCode(CD value) {
        this.code = value;
    }

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
    public IVLTS getEffectiveTime() {
        return effectiveTime;
    }

    /**
     * Sets the value of the effectiveTime property.
     *
     * @param value
     *     allowed object is
     *     {@link IVLTS }
     *
     */
    public void setEffectiveTime(IVLTS value) {
        this.effectiveTime = value;
    }

    public boolean hasEffectiveTime() {
        return effectiveTime != null;
    }

    /**
     * Gets the value of the availabilityTime property.
     *
     * @return
     *     possible object is
     *     {@link TS }
     *
     */
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
    public void setAvailabilityTime(TS value) {
        this.availabilityTime = value;
    }

    public boolean hasAvailabilityTime() {
        return availabilityTime != null;
    }

    /**
     * Gets the value of the priorityCode property.
     *
     * @return
     *     possible object is
     *     {@link CV }
     *
     */
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
    public void setUncertaintyCode(CV value) {
        this.uncertaintyCode = value;
    }

    public boolean hasUncertaintyCode() {
        return uncertaintyCode != null;
    }
    /**
     * Gets the value of the interpretationCode property.
     *
     * @return
     *     possible object is
     *     {@link CV }
     *
     */
    public CV getInterpretationCode() {
        return interpretationCode;
    }

    /**
     * Sets the value of the interpretationCode property.
     *
     * @param value
     *     allowed object is
     *     {@link CV }
     *
     */
    public void setInterpretationCode(CV value) {
        this.interpretationCode = value;
    }

    /**
     * Gets the value of the subject property.
     *
     * @return
     *     possible object is
     *     {@link RCMRMT030101UK04Subject }
     *
     */
    public RCMRMT030101UK04Subject getSubject() {
        return subject;
    }

    /**
     * Sets the value of the subject property.
     *
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UK04Subject }
     *
     */
    public void setSubject(RCMRMT030101UK04Subject value) {
        this.subject = value;
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
     * {@link RCMRMT030101UK04Specimen }
     *
     *
     */
    public List<RCMRMT030101UK04Specimen> getSpecimen() {
        if (specimen == null) {
            specimen = new ArrayList<RCMRMT030101UK04Specimen>();
        }
        return this.specimen;
    }

    /**
     * Gets the value of the pertinentInformation property.
     *
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the pertinentInformation property.
     *
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getPertinentInformation().add(newItem);
     * &lt;/pre&gt;
     *
     *
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link RCMRMT030101UK04PertinentInformation02 }
     *
     *
     */
    public List<RCMRMT030101UK04PertinentInformation02> getPertinentInformation() {
        if (pertinentInformation == null) {
            pertinentInformation = new ArrayList<RCMRMT030101UK04PertinentInformation02>();
        }
        return this.pertinentInformation;
    }

    /**
     * Gets the value of the referenceRange property.
     *
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the referenceRange property.
     *
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getReferenceRange().add(newItem);
     * &lt;/pre&gt;
     *
     *
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link RCMRMT030101UK04ReferenceRange }
     *
     *
     */
    public List<RCMRMT030101UK04ReferenceRange> getReferenceRange() {
        if (referenceRange == null) {
            referenceRange = new ArrayList<RCMRMT030101UK04ReferenceRange>();
        }
        return this.referenceRange;
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
    public List<RCMRMT030101UK04Informant> getInformant() {
        if (informant == null) {
            informant = new ArrayList<RCMRMT030101UK04Informant>();
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
    public List<RCMRMT030101UK04Participant> getParticipant() {
        if (participant == null) {
            participant = new ArrayList<RCMRMT030101UK04Participant>();
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
    public List<RCMRMT030101UK04ReplacementOf> getReplacementOf() {
        if (replacementOf == null) {
            replacementOf = new ArrayList<RCMRMT030101UK04ReplacementOf>();
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
    public List<RCMRMT030101UK04Reason> getReason() {
        if (reason == null) {
            reason = new ArrayList<RCMRMT030101UK04Reason>();
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
    public List<RCMRMT030101UK04Reference> getReference() {
        if (reference == null) {
            reference = new ArrayList<RCMRMT030101UK04Reference>();
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
    public List<RCMRMT030101UK04SequelTo> getSequelTo() {
        if (sequelTo == null) {
            sequelTo = new ArrayList<RCMRMT030101UK04SequelTo>();
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
    public String getType() {
        if (type == null) {
            return "Observation";
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
    public List<String> getClassCode() {
        if (classCode == null) {
            classCode = new ArrayList<String>();
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
    public List<String> getMoodCode() {
        if (moodCode == null) {
            moodCode = new ArrayList<String>();
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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean hasValue() {
        return value != null;
    }
}
