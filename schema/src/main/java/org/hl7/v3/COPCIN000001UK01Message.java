
package org.hl7.v3;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * &lt;p&gt;Java class for COPC_IN000001UK01.MCCI_MT010101UK12.Message complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="COPC_IN000001UK01.MCCI_MT010101UK12.Message"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;group ref="{urn:hl7-org:v3}InfrastructureRootElements"/&amp;gt;
 *         &amp;lt;element name="id" type="{urn:hl7-org:v3}II.NPfIT.uuid.mandatory"/&amp;gt;
 *         &amp;lt;element name="creationTime"&amp;gt;
 *           &amp;lt;complexType&amp;gt;
 *             &amp;lt;complexContent&amp;gt;
 *               &amp;lt;restriction base="{urn:hl7-org:v3}TS"&amp;gt;
 *                 &amp;lt;attribute name="value" use="required" type="{urn:hl7-org:v3}ts" /&amp;gt;
 *               &amp;lt;/restriction&amp;gt;
 *             &amp;lt;/complexContent&amp;gt;
 *           &amp;lt;/complexType&amp;gt;
 *         &amp;lt;/element&amp;gt;
 *         &amp;lt;element name="versionCode"&amp;gt;
 *           &amp;lt;complexType&amp;gt;
 *             &amp;lt;complexContent&amp;gt;
 *               &amp;lt;restriction base="{urn:hl7-org:v3}CS"&amp;gt;
 *                 &amp;lt;attribute name="code" use="required" type="{urn:hl7-org:v3}HL7StandardVersionCode_code" /&amp;gt;
 *               &amp;lt;/restriction&amp;gt;
 *             &amp;lt;/complexContent&amp;gt;
 *           &amp;lt;/complexType&amp;gt;
 *         &amp;lt;/element&amp;gt;
 *         &amp;lt;element name="interactionId"&amp;gt;
 *           &amp;lt;complexType&amp;gt;
 *             &amp;lt;complexContent&amp;gt;
 *               &amp;lt;restriction base="{urn:hl7-org:v3}II"&amp;gt;
 *                 &amp;lt;attribute name="root" use="required" type="{urn:hl7-org:v3}II.NPfIT.Message.oid" /&amp;gt;
 *                 &amp;lt;attribute name="extension" use="required" type="{urn:hl7-org:v3}II.NPfIT.Message.extension" /&amp;gt;
 *               &amp;lt;/restriction&amp;gt;
 *             &amp;lt;/complexContent&amp;gt;
 *           &amp;lt;/complexType&amp;gt;
 *         &amp;lt;/element&amp;gt;
 *         &amp;lt;element name="processingCode"&amp;gt;
 *           &amp;lt;complexType&amp;gt;
 *             &amp;lt;complexContent&amp;gt;
 *               &amp;lt;restriction base="{urn:hl7-org:v3}CS"&amp;gt;
 *                 &amp;lt;attribute name="code" use="required" type="{urn:hl7-org:v3}ProcessingID_code" /&amp;gt;
 *               &amp;lt;/restriction&amp;gt;
 *             &amp;lt;/complexContent&amp;gt;
 *           &amp;lt;/complexType&amp;gt;
 *         &amp;lt;/element&amp;gt;
 *         &amp;lt;element name="processingModeCode"&amp;gt;
 *           &amp;lt;complexType&amp;gt;
 *             &amp;lt;complexContent&amp;gt;
 *               &amp;lt;restriction base="{urn:hl7-org:v3}CS"&amp;gt;
 *                 &amp;lt;attribute name="code" use="required" type="{urn:hl7-org:v3}ProcessingMode_code" /&amp;gt;
 *               &amp;lt;/restriction&amp;gt;
 *             &amp;lt;/complexContent&amp;gt;
 *           &amp;lt;/complexType&amp;gt;
 *         &amp;lt;/element&amp;gt;
 *         &amp;lt;element name="acceptAckCode" type="{urn:hl7-org:v3}CS"/&amp;gt;
 *         &amp;lt;element name="communicationFunctionRcv" type="{urn:hl7-org:v3}MCCI_MT010101UK12.CommunicationFunctionRcv" maxOccurs="unbounded"/&amp;gt;
 *         &amp;lt;element name="communicationFunctionSnd" type="{urn:hl7-org:v3}MCCI_MT010101UK12.CommunicationFunctionSnd"/&amp;gt;
 *         &amp;lt;element name="ControlActEvent" type="{urn:hl7-org:v3}COPC_IN000001UK01.MCAI_MT040101UK03.ControlActEvent"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attGroup ref="{urn:hl7-org:v3}InfrastructureRootAttributes"/&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "COPC_IN000001UK01.Message", propOrder = {
    "id",
    "creationTime",
    "versionCode",
    "interactionId",
    "processingCode",
    "processingModeCode",
    "acceptAckCode",
    "communicationFunctionRcv",
    "communicationFunctionSnd",
    "controlActEvent"
})
public class COPCIN000001UK01Message {

    @XmlElement(required = true)
    protected II id;
    @XmlElement(required = true)
    protected TS creationTime;
    @XmlElement(required = true)
    protected CS versionCode;
    @XmlElement(required = true)
    protected II interactionId;
    @XmlElement(required = true)
    protected CS processingCode;
    @XmlElement(required = true)
    protected CS processingModeCode;
    @XmlElement(required = true)
    protected CS acceptAckCode;
    @XmlElement(required = true)
    protected List<MCCIMT010101UK12CommunicationFunctionRcv> communicationFunctionRcv;
    @XmlElement(required = true)
    protected MCCIMT010101UK12CommunicationFunctionSnd communicationFunctionSnd;

    @XmlElement(name = "ControlActEvent", required = true, nillable = true)
    protected COPCIN000001UK01ControlActEvent controlActEvent;
    @XmlAttribute(name = "nullFlavor")
    protected List<String> nullFlavor;
    @XmlAttribute(name = "updateMode")
    protected CsUpdateMode updateMode;

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
     * Gets the value of the creationTime property.
     * 
     * @return
     *     possible object is
     *     {@link TS }
     *     
     */
    public TS getCreationTime() {
        return creationTime;
    }

    /**
     * Sets the value of the creationTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link TS }
     *     
     */
    public void setCreationTime(TS value) {
        this.creationTime = value;
    }

    /**
     * Gets the value of the versionCode property.
     * 
     * @return
     *     possible object is
     *     {@link CS }
     *     
     */
    public CS getVersionCode() {
        return versionCode;
    }

    /**
     * Sets the value of the versionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CS }
     *     
     */
    public void setVersionCode(CS value) {
        this.versionCode = value;
    }

    /**
     * Gets the value of the interactionId property.
     * 
     * @return
     *     possible object is
     *     {@link II }
     *     
     */
    public II getInteractionId() {
        return interactionId;
    }

    /**
     * Sets the value of the interactionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link II }
     *     
     */
    public void setInteractionId(II value) {
        this.interactionId = value;
    }

    /**
     * Gets the value of the processingCode property.
     * 
     * @return
     *     possible object is
     *     {@link CS }
     *     
     */
    public CS getProcessingCode() {
        return processingCode;
    }

    /**
     * Sets the value of the processingCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CS }
     *     
     */
    public void setProcessingCode(CS value) {
        this.processingCode = value;
    }

    /**
     * Gets the value of the processingModeCode property.
     * 
     * @return
     *     possible object is
     *     {@link CS }
     *     
     */
    public CS getProcessingModeCode() {
        return processingModeCode;
    }

    /**
     * Sets the value of the processingModeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CS }
     *     
     */
    public void setProcessingModeCode(CS value) {
        this.processingModeCode = value;
    }

    /**
     * Gets the value of the acceptAckCode property.
     * 
     * @return
     *     possible object is
     *     {@link CS }
     *     
     */
    public CS getAcceptAckCode() {
        return acceptAckCode;
    }

    /**
     * Sets the value of the acceptAckCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CS }
     *     
     */
    public void setAcceptAckCode(CS value) {
        this.acceptAckCode = value;
    }

    /**
     * Gets the value of the communicationFunctionRcv property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the communicationFunctionRcv property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getCommunicationFunctionRcv().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link MCCIMT010101UK12CommunicationFunctionRcv }
     * 
     * 
     */
    public List<MCCIMT010101UK12CommunicationFunctionRcv> getCommunicationFunctionRcv() {
        if (communicationFunctionRcv == null) {
            communicationFunctionRcv = new ArrayList<MCCIMT010101UK12CommunicationFunctionRcv>();
        }
        return this.communicationFunctionRcv;
    }

    /**
     * Gets the value of the communicationFunctionSnd property.
     * 
     * @return
     *     possible object is
     *     {@link MCCIMT010101UK12CommunicationFunctionSnd }
     *     
     */
    public MCCIMT010101UK12CommunicationFunctionSnd getCommunicationFunctionSnd() {
        return communicationFunctionSnd;
    }

    /**
     * Sets the value of the communicationFunctionSnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link MCCIMT010101UK12CommunicationFunctionSnd }
     *     
     */
    public void setCommunicationFunctionSnd(MCCIMT010101UK12CommunicationFunctionSnd value) {
        this.communicationFunctionSnd = value;
    }

    /**
     * Gets the value of the controlActEvent property.
     * 
     * @return
     *     possible object is
     *     {@link COPCIN000001UK01ControlActEvent }
     *     
     */
    public COPCIN000001UK01ControlActEvent getControlActEvent() {
        return controlActEvent;
    }

    /**
     * Sets the value of the controlActEvent property.
     * 
     * @param value
     *     allowed object is
     *     {@link COPCIN000001UK01ControlActEvent }
     *     
     */
    public void setControlActEvent(COPCIN000001UK01ControlActEvent value) {
        this.controlActEvent = value;
    }

    /**
     * Gets the value of the nullFlavor property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the nullFlavor property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getNullFlavor().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getNullFlavor() {
        if (nullFlavor == null) {
            nullFlavor = new ArrayList<String>();
        }
        return this.nullFlavor;
    }

    /**
     * Gets the value of the updateMode property.
     * 
     * @return
     *     possible object is
     *     {@link CsUpdateMode }
     *     
     */
    public CsUpdateMode getUpdateMode() {
        return updateMode;
    }

    /**
     * Sets the value of the updateMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CsUpdateMode }
     *     
     */
    public void setUpdateMode(CsUpdateMode value) {
        this.updateMode = value;
    }



}
