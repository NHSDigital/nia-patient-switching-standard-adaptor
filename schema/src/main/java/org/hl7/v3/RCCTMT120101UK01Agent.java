
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
 * &lt;p&gt;Java class for RCCT_MT120101UK01.Agent complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="RCCT_MT120101UK01.Agent"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="id" type="{urn:hl7-org:v3}II" maxOccurs="2"/&amp;gt;
 *         &amp;lt;element name="code" type="{urn:hl7-org:v3}CV" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="addr" type="{urn:hl7-org:v3}AD" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="telecom" type="{urn:hl7-org:v3}TEL" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;choice&amp;gt;
 *           &amp;lt;element name="agentOrganization" type="{urn:hl7-org:v3}RCCT_MT120101UK01.Organization" minOccurs="0"/&amp;gt;
 *           &amp;lt;element name="agentOrganizationSDS" type="{urn:hl7-org:v3}RCCT_MT120101UK01.OrganizationSDS" minOccurs="0"/&amp;gt;
 *           &amp;lt;element name="agentPersonSDS" type="{urn:hl7-org:v3}RCCT_MT120101UK01.PersonSDS" minOccurs="0"/&amp;gt;
 *           &amp;lt;element name="agentPerson" type="{urn:hl7-org:v3}RCCT_MT120101UK01.Person" minOccurs="0"/&amp;gt;
 *           &amp;lt;element name="agentDeviceSDS" type="{urn:hl7-org:v3}RCCT_MT120101UK01.DeviceSDS" minOccurs="0"/&amp;gt;
 *           &amp;lt;element name="agentDevice" type="{urn:hl7-org:v3}RCCT_MT120101UK01.Device" minOccurs="0"/&amp;gt;
 *         &amp;lt;/choice&amp;gt;
 *         &amp;lt;choice&amp;gt;
 *           &amp;lt;element name="representedOrganization" type="{urn:hl7-org:v3}RCCT_MT120101UK01.Organization" minOccurs="0"/&amp;gt;
 *           &amp;lt;element name="representedOrganizationSDS" type="{urn:hl7-org:v3}RCCT_MT120101UK01.OrganizationSDS" minOccurs="0"/&amp;gt;
 *           &amp;lt;element name="representedPersonSDS" type="{urn:hl7-org:v3}RCCT_MT120101UK01.PersonSDS" minOccurs="0"/&amp;gt;
 *           &amp;lt;element name="representedPerson" type="{urn:hl7-org:v3}RCCT_MT120101UK01.Person" minOccurs="0"/&amp;gt;
 *         &amp;lt;/choice&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="type" type="{urn:hl7-org:v3}Classes" default="RoleHeir" /&amp;gt;
 *       &amp;lt;attribute name="classCode" type="{urn:hl7-org:v3}RoleClass" default="AGNT" /&amp;gt;
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
@XmlType(name = "RCCT_MT120101UK01.Agent", propOrder = {
    "id",
    "code",
    "addr",
    "telecom",
    "agentOrganization",
    "agentOrganizationSDS",
    "agentPersonSDS",
    "agentPerson",
    "agentDeviceSDS",
    "agentDevice",
    "representedOrganization",
    "representedOrganizationSDS",
    "representedPersonSDS",
    "representedPerson"
})
public class RCCTMT120101UK01Agent {

    @XmlElement(required = true)
    protected List<II> id;
    protected CV code;
    protected List<AD> addr;
    protected List<TEL> telecom;
    protected RCCTMT120101UK01Organization agentOrganization;
    protected RCCTMT120101UK01OrganizationSDS agentOrganizationSDS;
    protected RCCTMT120101UK01PersonSDS agentPersonSDS;
    protected RCCTMT120101UK01Person agentPerson;
    protected RCCTMT120101UK01DeviceSDS agentDeviceSDS;
    protected RCCTMT120101UK01Device agentDevice;
    protected RCCTMT120101UK01Organization representedOrganization;
    protected RCCTMT120101UK01OrganizationSDS representedOrganizationSDS;
    protected RCCTMT120101UK01PersonSDS representedPersonSDS;
    protected RCCTMT120101UK01Person representedPerson;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "classCode")
    protected List<String> classCode;
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
    public List<II> getId() {
        if (id == null) {
            id = new ArrayList<>();
        }
        return this.id;
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
     * Gets the value of the addr property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the addr property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getAddr().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link AD }
     * 
     * 
     */
    public List<AD> getAddr() {
        if (addr == null) {
            addr = new ArrayList<>();
        }
        return this.addr;
    }

    /**
     * Gets the value of the telecom property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the telecom property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getTelecom().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link TEL }
     * 
     * 
     */
    public List<TEL> getTelecom() {
        if (telecom == null) {
            telecom = new ArrayList<>();
        }
        return this.telecom;
    }

    /**
     * Gets the value of the agentOrganization property.
     * 
     * @return
     *     possible object is
     *     {@link RCCTMT120101UK01Organization }
     *     
     */
    public RCCTMT120101UK01Organization getAgentOrganization() {
        return agentOrganization;
    }

    /**
     * Sets the value of the agentOrganization property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCCTMT120101UK01Organization }
     *     
     */
    public void setAgentOrganization(RCCTMT120101UK01Organization value) {
        this.agentOrganization = value;
    }

    /**
     * Gets the value of the agentOrganizationSDS property.
     * 
     * @return
     *     possible object is
     *     {@link RCCTMT120101UK01OrganizationSDS }
     *     
     */
    public RCCTMT120101UK01OrganizationSDS getAgentOrganizationSDS() {
        return agentOrganizationSDS;
    }

    /**
     * Sets the value of the agentOrganizationSDS property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCCTMT120101UK01OrganizationSDS }
     *     
     */
    public void setAgentOrganizationSDS(RCCTMT120101UK01OrganizationSDS value) {
        this.agentOrganizationSDS = value;
    }

    /**
     * Gets the value of the agentPersonSDS property.
     * 
     * @return
     *     possible object is
     *     {@link RCCTMT120101UK01PersonSDS }
     *     
     */
    public RCCTMT120101UK01PersonSDS getAgentPersonSDS() {
        return agentPersonSDS;
    }

    /**
     * Sets the value of the agentPersonSDS property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCCTMT120101UK01PersonSDS }
     *     
     */
    public void setAgentPersonSDS(RCCTMT120101UK01PersonSDS value) {
        this.agentPersonSDS = value;
    }

    /**
     * Gets the value of the agentPerson property.
     * 
     * @return
     *     possible object is
     *     {@link RCCTMT120101UK01Person }
     *     
     */
    public RCCTMT120101UK01Person getAgentPerson() {
        return agentPerson;
    }

    /**
     * Sets the value of the agentPerson property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCCTMT120101UK01Person }
     *     
     */
    public void setAgentPerson(RCCTMT120101UK01Person value) {
        this.agentPerson = value;
    }

    /**
     * Gets the value of the agentDeviceSDS property.
     * 
     * @return
     *     possible object is
     *     {@link RCCTMT120101UK01DeviceSDS }
     *     
     */
    public RCCTMT120101UK01DeviceSDS getAgentDeviceSDS() {
        return agentDeviceSDS;
    }

    /**
     * Sets the value of the agentDeviceSDS property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCCTMT120101UK01DeviceSDS }
     *     
     */
    public void setAgentDeviceSDS(RCCTMT120101UK01DeviceSDS value) {
        this.agentDeviceSDS = value;
    }

    /**
     * Gets the value of the agentDevice property.
     * 
     * @return
     *     possible object is
     *     {@link RCCTMT120101UK01Device }
     *     
     */
    public RCCTMT120101UK01Device getAgentDevice() {
        return agentDevice;
    }

    /**
     * Sets the value of the agentDevice property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCCTMT120101UK01Device }
     *     
     */
    public void setAgentDevice(RCCTMT120101UK01Device value) {
        this.agentDevice = value;
    }

    /**
     * Gets the value of the representedOrganization property.
     * 
     * @return
     *     possible object is
     *     {@link RCCTMT120101UK01Organization }
     *     
     */
    public RCCTMT120101UK01Organization getRepresentedOrganization() {
        return representedOrganization;
    }

    /**
     * Sets the value of the representedOrganization property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCCTMT120101UK01Organization }
     *     
     */
    public void setRepresentedOrganization(RCCTMT120101UK01Organization value) {
        this.representedOrganization = value;
    }

    /**
     * Gets the value of the representedOrganizationSDS property.
     * 
     * @return
     *     possible object is
     *     {@link RCCTMT120101UK01OrganizationSDS }
     *     
     */
    public RCCTMT120101UK01OrganizationSDS getRepresentedOrganizationSDS() {
        return representedOrganizationSDS;
    }

    /**
     * Sets the value of the representedOrganizationSDS property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCCTMT120101UK01OrganizationSDS }
     *     
     */
    public void setRepresentedOrganizationSDS(RCCTMT120101UK01OrganizationSDS value) {
        this.representedOrganizationSDS = value;
    }

    /**
     * Gets the value of the representedPersonSDS property.
     * 
     * @return
     *     possible object is
     *     {@link RCCTMT120101UK01PersonSDS }
     *     
     */
    public RCCTMT120101UK01PersonSDS getRepresentedPersonSDS() {
        return representedPersonSDS;
    }

    /**
     * Sets the value of the representedPersonSDS property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCCTMT120101UK01PersonSDS }
     *     
     */
    public void setRepresentedPersonSDS(RCCTMT120101UK01PersonSDS value) {
        this.representedPersonSDS = value;
    }

    /**
     * Gets the value of the representedPerson property.
     * 
     * @return
     *     possible object is
     *     {@link RCCTMT120101UK01Person }
     *     
     */
    public RCCTMT120101UK01Person getRepresentedPerson() {
        return representedPerson;
    }

    /**
     * Sets the value of the representedPerson property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCCTMT120101UK01Person }
     *     
     */
    public void setRepresentedPerson(RCCTMT120101UK01Person value) {
        this.representedPerson = value;
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
            return "RoleHeir";
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
            classCode = new ArrayList<>();
        }
        return this.classCode;
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
