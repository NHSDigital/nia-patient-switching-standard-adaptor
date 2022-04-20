
package org.hl7.v3;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * &lt;p&gt;Java class for COPC_IN000001UK01.MCAI_MT040101UK03.Subject complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="COPC_IN000001UK01.MCAI_MT040101UK03.Subject"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;group ref="{urn:hl7-org:v3}InfrastructureRootElements"/&amp;gt;
 *         &amp;lt;element name="PayloadInformation" type="{urn:hl7-org:v3}COPC_MT000001UK01.PayloadInformation"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attGroup ref="{urn:hl7-org:v3}InfrastructureRootAttributes"/&amp;gt;
 *       &amp;lt;attribute name="typeCode" use="required" type="{urn:hl7-org:v3}cs" fixed="SUBJ" /&amp;gt;
 *       &amp;lt;attribute name="contextConductionInd" use="required" type="{urn:hl7-org:v3}bl" fixed="false" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "COPC_IN000001UK01.MCAI_MT040101UK03.Subject", propOrder = {
    "payloadInformation"
})
public class COPCIN000001UK01MCAIMT040101UK03Subject {

    @XmlElement(name = "PayloadInformation", required = true, nillable = true)
    protected COPCMT000001UK01PayloadInformation payloadInformation;
    @XmlAttribute(name = "typeCode", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String typeCode;
    @XmlAttribute(name = "contextConductionInd", required = true)
    protected boolean contextConductionInd;
    @XmlAttribute(name = "nullFlavor")
    protected List<String> nullFlavor;
    @XmlAttribute(name = "updateMode")
    protected CsUpdateMode updateMode;

    /**
     * Gets the value of the payloadInformation property.
     * 
     * @return
     *     possible object is
     *     {@link COPCMT000001UK01PayloadInformation }
     *     
     */
    public COPCMT000001UK01PayloadInformation getPayloadInformation() {
        return payloadInformation;
    }

    /**
     * Sets the value of the payloadInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link COPCMT000001UK01PayloadInformation }
     *     
     */
    public void setPayloadInformation(COPCMT000001UK01PayloadInformation value) {
        this.payloadInformation = value;
    }

    /**
     * Gets the value of the typeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeCode() {
        if (typeCode == null) {
            return "SUBJ";
        } else {
            return typeCode;
        }
    }

    /**
     * Sets the value of the typeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeCode(String value) {
        this.typeCode = value;
    }

    /**
     * Gets the value of the contextConductionInd property.
     * 
     */
    public boolean isContextConductionInd() {
        return contextConductionInd;
    }

    /**
     * Sets the value of the contextConductionInd property.
     * 
     */
    public void setContextConductionInd(boolean value) {
        this.contextConductionInd = value;
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
