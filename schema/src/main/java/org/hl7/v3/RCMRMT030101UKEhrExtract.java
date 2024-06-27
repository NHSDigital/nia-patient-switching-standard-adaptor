
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
 * &lt;p&gt;Java class for RCMR_MT030101UK.EhrExtract complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="RCMR_MT030101UK.EhrExtract"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="id" type="{urn:hl7-org:v3}II"/&amp;gt;
 *         &amp;lt;element name="statusCode" type="{urn:hl7-org:v3}CS"/&amp;gt;
 *         &amp;lt;element name="availabilityTime" type="{urn:hl7-org:v3}TS"/&amp;gt;
 *         &amp;lt;element name="recordTarget" type="{urn:hl7-org:v3}RCMR_MT030101UK.PatientSubject"/&amp;gt;
 *         &amp;lt;element name="author" type="{urn:hl7-org:v3}RCMR_MT030101UK.Author3"/&amp;gt;
 *         &amp;lt;element name="destination" type="{urn:hl7-org:v3}RCMR_MT030101UK.Destination"/&amp;gt;
 *         &amp;lt;element name="component" type="{urn:hl7-org:v3}RCMR_MT030101UK.Component" maxOccurs="unbounded"/&amp;gt;
 *         &amp;lt;element name="inFulfillmentOf" type="{urn:hl7-org:v3}RCMR_MT030101UK.InFulfillmentOf2"/&amp;gt;
 *         &amp;lt;element name="limitation" type="{urn:hl7-org:v3}RCMR_MT030101UK.Limitation"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="type" type="{urn:hl7-org:v3}Classes" default="ActHeir" /&amp;gt;
 *       &amp;lt;attribute name="classCode" type="{urn:hl7-org:v3}ActClass" default="EXTRACT" /&amp;gt;
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
@XmlType(name = "RCMR_MT030101UK.EhrExtract", propOrder = {
    "id",
    "statusCode",
    "availabilityTime",
    "recordTarget",
    "author",
    "destination",
    "component",
    "inFulfillmentOf",
    "limitation"
})
public class RCMRMT030101UKEhrExtract {

    @XmlElement(required = true)
    protected II id;
    @XmlElement(required = true)
    protected CS statusCode;
    @XmlElement(required = true)
    protected TS availabilityTime;

    @XmlElement(required = true, type = RCMRMT030101UKPatientSubject.class)
    protected RCMRMT030101UKPatientSubject recordTarget;

    @XmlElement(required = true, type = RCMRMT030101UKAuthor3.class)
    protected RCMRMT030101UKAuthor3 author;

    @XmlElement(required = true, type = RCMRMT030101UKDestination.class)
    protected RCMRMT030101UKDestination destination;

    @XmlElement(required = true, type = RCMRMT030101UKComponent.class)
    protected List<RCMRMT030101UKComponent> component;

    @XmlElement(required = true, type = RCMRMT030101UKInFulfillmentOf2.class)
    protected RCMRMT030101UKInFulfillmentOf2 inFulfillmentOf;

    @XmlElement(required = true, type = RCMRMT030101UKLimitation.class)
    protected RCMRMT030101UKLimitation limitation;

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

    /**
     * Gets the value of the recordTarget property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKPatientSubject }
     *     
     */
    public RCMRMT030101UKPatientSubject getRecordTarget() {
        return recordTarget;
    }

    /**
     * Sets the value of the recordTarget property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKPatientSubject }
     *     
     */
    public void setRecordTarget(RCMRMT030101UKPatientSubject value) {
        this.recordTarget = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKAuthor3 }
     *     
     */
    public RCMRMT030101UKAuthor3 getAuthor() {
        return author;
    }
    
    public boolean hasAuthor() {
        return author != null;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKAuthor3 }
     *     
     */
    public void setAuthor(RCMRMT030101UKAuthor3 value) {
        this.author = value;
    }

    /**
     * Gets the value of the destination property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKDestination }
     *     
     */
    public RCMRMT030101UKDestination getDestination() {
        return destination;
    }

    /**
     * Sets the value of the destination property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKDestination }
     *     
     */
    public void setDestination(RCMRMT030101UKDestination value) {
        this.destination = value;
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
     * {@link RCMRMT030101UKComponent }
     * 
     * 
     */
    public List<RCMRMT030101UKComponent> getComponent() {
        if (component == null) {
            component = new ArrayList<>();
        }
        return this.component;
    }

    /**
     * Gets the value of the inFulfillmentOf property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKInFulfillmentOf2 }
     *     
     */
    public RCMRMT030101UKInFulfillmentOf2 getInFulfillmentOf() {
        return inFulfillmentOf;
    }

    /**
     * Sets the value of the inFulfillmentOf property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKInFulfillmentOf2 }
     *     
     */
    public void setInFulfillmentOf(RCMRMT030101UKInFulfillmentOf2 value) {
        this.inFulfillmentOf = value;
    }

    /**
     * Gets the value of the limitation property.
     * 
     * @return
     *     possible object is
     *     {@link RCMRMT030101UKLimitation }
     *     
     */
    public RCMRMT030101UKLimitation getLimitation() {
        return limitation;
    }

    /**
     * Sets the value of the limitation property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCMRMT030101UKLimitation }
     *     
     */
    public void setLimitation(RCMRMT030101UKLimitation value) {
        this.limitation = value;
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
        return Objects.requireNonNullElse(type, "ActHeir");
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
