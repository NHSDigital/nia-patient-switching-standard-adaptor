
package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java class for RCCT_MT120101UK01.Device complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="RCCT_MT120101UK01.Device"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="id" type="{urn:hl7-org:v3}II" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="code" type="{urn:hl7-org:v3}CV" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="name" type="{urn:hl7-org:v3}ST" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="desc" type="{urn:hl7-org:v3}ST" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="manufacturerModelName" type="{urn:hl7-org:v3}SC" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="softwareName" type="{urn:hl7-org:v3}SC" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="type" type="{urn:hl7-org:v3}Classes" default="Device" /&amp;gt;
 *       &amp;lt;attribute name="classCode" type="{urn:hl7-org:v3}EntityClass" default="DEV" /&amp;gt;
 *       &amp;lt;attribute name="determinerCode" type="{urn:hl7-org:v3}EntityDeterminer" default="INSTANCE" /&amp;gt;
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
@XmlType(name = "RCCT_MT120101UK01.Device", propOrder = {
    "id",
    "code",
    "name",
    "desc",
    "manufacturerModelName",
    "softwareName"
})
public class RCCTMT120101UK01Device {

    protected II id;
    protected CV code;
    protected ST name;
    protected ST desc;
    protected SC manufacturerModelName;
    protected SC softwareName;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "classCode")
    protected List<String> classCode;
    @XmlAttribute(name = "determinerCode")
    protected List<String> determinerCode;
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
     *     {@link CV }
     *     
     */
    public CV getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link CV }
     *     
     */
    public void setCode(CV value) {
        this.code = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link ST }
     *     
     */
    public ST getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link ST }
     *     
     */
    public void setName(ST value) {
        this.name = value;
    }

    /**
     * Gets the value of the desc property.
     * 
     * @return
     *     possible object is
     *     {@link ST }
     *     
     */
    public ST getDesc() {
        return desc;
    }

    /**
     * Sets the value of the desc property.
     * 
     * @param value
     *     allowed object is
     *     {@link ST }
     *     
     */
    public void setDesc(ST value) {
        this.desc = value;
    }

    /**
     * Gets the value of the manufacturerModelName property.
     * 
     * @return
     *     possible object is
     *     {@link SC }
     *     
     */
    public SC getManufacturerModelName() {
        return manufacturerModelName;
    }

    /**
     * Sets the value of the manufacturerModelName property.
     * 
     * @param value
     *     allowed object is
     *     {@link SC }
     *     
     */
    public void setManufacturerModelName(SC value) {
        this.manufacturerModelName = value;
    }

    /**
     * Gets the value of the softwareName property.
     * 
     * @return
     *     possible object is
     *     {@link SC }
     *     
     */
    public SC getSoftwareName() {
        return softwareName;
    }

    /**
     * Sets the value of the softwareName property.
     * 
     * @param value
     *     allowed object is
     *     {@link SC }
     *     
     */
    public void setSoftwareName(SC value) {
        this.softwareName = value;
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
            return "Device";
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
     * Gets the value of the determinerCode property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the determinerCode property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getDeterminerCode().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDeterminerCode() {
        if (determinerCode == null) {
            determinerCode = new ArrayList<String>();
        }
        return this.determinerCode;
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
