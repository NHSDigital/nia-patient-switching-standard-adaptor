
package org.hl7.v3;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * &lt;p&gt;Java class for COPC_MT000001UK01.PertinentInformation complex type.
 *
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 *
 * &lt;pre&gt;
 * &amp;lt;complexType name="COPC_MT000001UK01.PertinentInformation"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;group ref="{urn:hl7-org:v3}InfrastructureRootElements"/&amp;gt;
 *         &amp;lt;element name="sequenceNumber" type="{urn:hl7-org:v3}INT" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="pertinentPayloadBody" type="{urn:hl7-org:v3}COPC_MT000001UK01.PayloadBody"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attGroup ref="{urn:hl7-org:v3}InfrastructureRootAttributes"/&amp;gt;
 *       &amp;lt;attribute name="typeCode" use="required" type="{urn:hl7-org:v3}cs" fixed="PERT" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "COPC_MT000001UK01.PertinentInformation", propOrder = {
    "sequenceNumber",
    "pertinentPayloadBody"
})
public class COPCMT000001UK01PertinentInformation {

    protected INT sequenceNumber;
    @XmlElement(required = true)
    protected COPCMT000001UK01PayloadBody pertinentPayloadBody;
    @XmlAttribute(name = "typeCode", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String typeCode;
    @XmlAttribute(name = "nullFlavor")
    protected List<String> nullFlavor;
    @XmlAttribute(name = "updateMode")
    protected CsUpdateMode updateMode;

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
     * Gets the value of the pertinentPayloadBody property.
     *
     * @return
     *     possible object is
     *     {@link COPCMT000001UK01PayloadBody }
     *
     */
    public COPCMT000001UK01PayloadBody getPertinentPayloadBody() {
        return pertinentPayloadBody;
    }

    /**
     * Sets the value of the pertinentPayloadBody property.
     *
     * @param value
     *     allowed object is
     *     {@link COPCMT000001UK01PayloadBody }
     *
     */
    public void setPertinentPayloadBody(COPCMT000001UK01PayloadBody value) {
        this.pertinentPayloadBody = value;
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
            return "PERT";
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