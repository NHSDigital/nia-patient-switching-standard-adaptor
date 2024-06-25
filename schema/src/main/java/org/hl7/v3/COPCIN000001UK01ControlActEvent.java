
package org.hl7.v3;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * &lt;p&gt;Java class for COPC_IN000001UK01.MCAI_MT040101UK03.ControlActEvent complex type.
 *
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 *
 * &lt;pre&gt;
 * &amp;lt;complexType name="COPC_IN000001UK01.MCAI_MT040101UK03.ControlActEvent"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;group ref="{urn:hl7-org:v3}InfrastructureRootElements"/&amp;gt;
 *         &amp;lt;element name="author" type="{urn:hl7-org:v3}MCAI_MT040101UK03.Author" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="author1" type="{urn:hl7-org:v3}MCAI_MT040101UK03.Author2" maxOccurs="2"/&amp;gt;
 *         &amp;lt;element name="reason" type="{urn:hl7-org:v3}MCAI_MT040101UK03.Reason" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="subject" type="{urn:hl7-org:v3}COPC_IN000001UK01.MCAI_MT040101UK03.Subject"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attGroup ref="{urn:hl7-org:v3}InfrastructureRootAttributes"/&amp;gt;
 *       &amp;lt;attribute name="classCode" use="required" type="{urn:hl7-org:v3}cs" /&amp;gt;
 *       &amp;lt;attribute name="moodCode" use="required" type="{urn:hl7-org:v3}cs" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "COPC_IN000001UK01.ControlActEvent", propOrder = {
    "author",
    "author1",
    "reason",
    "subject"
})
public class COPCIN000001UK01ControlActEvent {

    MCAIMT040101UK03Author author;
    @XmlElement(required = true)
    protected List<MCAIMT040101UK03Author2> author1;
    @XmlElement(nillable = true)
    protected List<MCAIMT040101UK03Reason> reason;
    @XmlElement(required = true, nillable = true)
    protected COPCIN000001UK01Subject subject;
    @XmlAttribute(name = "classCode", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String classCode;
    @XmlAttribute(name = "moodCode", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String moodCode;
    @XmlAttribute(name = "nullFlavor")
    protected List<String> nullFlavor;
    @XmlAttribute(name = "updateMode")
    protected CsUpdateMode updateMode;

    /**
     * Gets the value of the author property.
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link MCAIMT040101UK03Author }{@code >}
     *
     */
    public MCAIMT040101UK03Author getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link MCAIMT040101UK03Author }{@code >}
     *
     */
    public void setAuthor(MCAIMT040101UK03Author value) {
        this.author = value;
    }

    /**
     * Gets the value of the author1 property.
     *
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the author1 property.
     *
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getAuthor1().add(newItem);
     * &lt;/pre&gt;
     *
     *
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link MCAIMT040101UK03Author2 }
     *
     *
     */
    public List<MCAIMT040101UK03Author2> getAuthor1() {
        if (author1 == null) {
            author1 = new ArrayList<>();
        }
        return this.author1;
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
     * {@link MCAIMT040101UK03Reason }
     *
     *
     */
    public List<MCAIMT040101UK03Reason> getReason() {
        if (reason == null) {
            reason = new ArrayList<>();
        }
        return this.reason;
    }

    /**
     * Gets the value of the subject property.
     *
     * @return
     *     possible object is
     *     {@link COPCIN000001UK01Subject }
     *
     */
    public COPCIN000001UK01Subject getSubject() {
        return subject;
    }

    /**
     * Sets the value of the subject property.
     *
     * @param value
     *     allowed object is
     *     {@link COPCIN000001UK01Subject }
     *
     */
    public void setSubject(COPCIN000001UK01Subject value) {
        this.subject = value;
    }

    /**
     * Gets the value of the classCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getClassCode() {
        return classCode;
    }

    /**
     * Sets the value of the classCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setClassCode(String value) {
        this.classCode = value;
    }

    /**
     * Gets the value of the moodCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMoodCode() {
        return moodCode;
    }

    /**
     * Sets the value of the moodCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMoodCode(String value) {
        this.moodCode = value;
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
            nullFlavor = new ArrayList<>();
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