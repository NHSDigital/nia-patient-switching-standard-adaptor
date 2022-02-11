
package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.w3c.dom.Element;


/**
 * 
 * Data that is primarily intended for human interpretation or for
 * further machine processing is outside the scope of HL7. This includes
 * unformatted or formatted written language, multimedia data, or
 * structured information as defined by a different standard (e.g.,
 * XML-signatures.)  Instead of the data itself, an ED
 * may contain only a reference (see TEL.) Note that
 * the ST data type is a specialization of the
 * ED data type when the ED
 * media type is text/plain.
 * 
 * 
 * &lt;p&gt;Java class for ED complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="ED"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}BIN"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="reference" type="{urn:hl7-org:v3}TEL" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="thumbnail" type="{urn:hl7-org:v3}thumbnail" minOccurs="0"/&amp;gt;
 *         &amp;lt;any processContents='skip' namespace='##other' maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="mediaType" type="{urn:hl7-org:v3}cs" default="text/plain" /&amp;gt;
 *       &amp;lt;attribute name="language" type="{urn:hl7-org:v3}cs" /&amp;gt;
 *       &amp;lt;attribute name="compression" type="{urn:hl7-org:v3}cs_CompressionAlgorithm" /&amp;gt;
 *       &amp;lt;attribute name="integrityCheck" type="{urn:hl7-org:v3}bin" /&amp;gt;
 *       &amp;lt;attribute name="integrityCheckAlgorithm" type="{urn:hl7-org:v3}cs_IntegrityCheckAlgorithm" default="SHA-1" /&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ED", propOrder = {
    "reference",
    "thumbnail",
    "any"
})
@XmlSeeAlso({
    EDNPfITTextXHTML.class,
    Thumbnail.class,
})
public class ED
    extends BIN
{

    protected TEL reference;
    protected Thumbnail thumbnail;
    @XmlAnyElement
    protected List<Element> any;
    @XmlAttribute(name = "mediaType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String mediaType;
    @XmlAttribute(name = "language")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String language;
    @XmlAttribute(name = "compression")
    protected CsCompressionAlgorithm compression;
    @XmlAttribute(name = "integrityCheck")
    protected byte[] integrityCheck;
    @XmlAttribute(name = "integrityCheckAlgorithm")
    protected CsIntegrityCheckAlgorithm integrityCheckAlgorithm;

    /**
     * Gets the value of the reference property.
     * 
     * @return
     *     possible object is
     *     {@link TEL }
     *     
     */
    public TEL getReference() {
        return reference;
    }

    /**
     * Sets the value of the reference property.
     * 
     * @param value
     *     allowed object is
     *     {@link TEL }
     *     
     */
    public void setReference(TEL value) {
        this.reference = value;
    }

    /**
     * Gets the value of the thumbnail property.
     * 
     * @return
     *     possible object is
     *     {@link Thumbnail }
     *     
     */
    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    /**
     * Sets the value of the thumbnail property.
     * 
     * @param value
     *     allowed object is
     *     {@link Thumbnail }
     *     
     */
    public void setThumbnail(Thumbnail value) {
        this.thumbnail = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the any property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getAny().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * 
     * 
     */
    public List<Element> getAny() {
        if (any == null) {
            any = new ArrayList<Element>();
        }
        return this.any;
    }

    /**
     * Gets the value of the mediaType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMediaType() {
        if (mediaType == null) {
            return "text/plain";
        } else {
            return mediaType;
        }
    }

    /**
     * Sets the value of the mediaType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMediaType(String value) {
        this.mediaType = value;
    }

    /**
     * Gets the value of the language property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguage(String value) {
        this.language = value;
    }

    /**
     * Gets the value of the compression property.
     * 
     * @return
     *     possible object is
     *     {@link CsCompressionAlgorithm }
     *     
     */
    public CsCompressionAlgorithm getCompression() {
        return compression;
    }

    /**
     * Sets the value of the compression property.
     * 
     * @param value
     *     allowed object is
     *     {@link CsCompressionAlgorithm }
     *     
     */
    public void setCompression(CsCompressionAlgorithm value) {
        this.compression = value;
    }

    /**
     * Gets the value of the integrityCheck property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getIntegrityCheck() {
        return integrityCheck;
    }

    /**
     * Sets the value of the integrityCheck property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setIntegrityCheck(byte[] value) {
        this.integrityCheck = value;
    }

    /**
     * Gets the value of the integrityCheckAlgorithm property.
     * 
     * @return
     *     possible object is
     *     {@link CsIntegrityCheckAlgorithm }
     *     
     */
    public CsIntegrityCheckAlgorithm getIntegrityCheckAlgorithm() {
        if (integrityCheckAlgorithm == null) {
            return CsIntegrityCheckAlgorithm.SHA_1;
        } else {
            return integrityCheckAlgorithm;
        }
    }

    /**
     * Sets the value of the integrityCheckAlgorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CsIntegrityCheckAlgorithm }
     *     
     */
    public void setIntegrityCheckAlgorithm(CsIntegrityCheckAlgorithm value) {
        this.integrityCheckAlgorithm = value;
    }

}
