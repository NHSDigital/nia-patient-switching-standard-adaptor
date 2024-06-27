
package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java class for RCMR_MT030101UK.ExternalDocument complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="RCMR_MT030101UK.ExternalDocument"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="id" type="{urn:hl7-org:v3}II"/&amp;gt;
 *         &amp;lt;element name="code" type="{urn:hl7-org:v3}CD"/&amp;gt;
 *         &amp;lt;element name="text" type="{urn:hl7-org:v3}ED"/&amp;gt;
 *         &amp;lt;element name="effectiveTime" type="{urn:hl7-org:v3}IVL_TS" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="setId" type="{urn:hl7-org:v3}II" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="versionNumber" type="{urn:hl7-org:v3}INT" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="author" type="{urn:hl7-org:v3}RCMR_MT030101UK.Author4" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="type" type="{urn:hl7-org:v3}Classes" default="Document" /&amp;gt;
 *       &amp;lt;attribute name="classCode" type="{urn:hl7-org:v3}ActClass" default="DOC" /&amp;gt;
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
@XmlType(name = "RCMR_MT030101UK.ExternalDocument", propOrder = {
    "id",
    "code",
    "text",
    "effectiveTime",
    "confidentialityCode",
    "setId",
    "versionNumber",
    "author"
})
public class RCMRMT030101UKExternalDocument {

    @XmlElement(required = true)
    protected II id;
    @XmlElement(required = true)
    protected CD code;
    @XmlElement(required = true)
    protected ED text;
    protected IVLTS effectiveTime;
    protected CV confidentialityCode;
    protected II setId;
    protected INT versionNumber;
    @XmlElement(type = RCMRMT030101UKAuthor4.class)
    protected List<RCMRMT030101UKAuthor4> author;
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

    public boolean hasCode() {
        return code != null;
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

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link ED }
     *     
     */
    public ED getText() {
        return text;
    }

    public boolean hasText() {
        return text != null;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link ED }
     *     
     */
    public void setText(ED value) {
        this.text = value;
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

    /**
     * Gets the value of the setId property.
     * 
     * @return
     *     possible object is
     *     {@link II }
     *     
     */
    public II getSetId() {
        return setId;
    }

    /**
     * Sets the value of the setId property.
     * 
     * @param value
     *     allowed object is
     *     {@link II }
     *     
     */
    public void setSetId(II value) {
        this.setId = value;
    }

    /**
     * Gets the value of the versionNumber property.
     * 
     * @return
     *     possible object is
     *     {@link INT }
     *     
     */
    public INT getVersionNumber() {
        return versionNumber;
    }

    /**
     * Sets the value of the versionNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link INT }
     *     
     */
    public void setVersionNumber(INT value) {
        this.versionNumber = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the author property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getAuthor().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link RCMRMT030101UKAuthor4 }
     * 
     * 
     */
    public List<RCMRMT030101UKAuthor4> getAuthor() {
        if (author == null) {
            author = new ArrayList<>();
        }
        return this.author;
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
        return Objects.requireNonNullElse(type, "Document");
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

    public Optional<CV> getConfidentialityCode() {
        return Optional.of(confidentialityCode);
    }

    public void setConfidentialityCode(CV confidentialityCode) {
        this.confidentialityCode = confidentialityCode;
    }

}
