
package xhtml.npfit.presentationtext;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for anonymous complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;choice maxOccurs="unbounded" minOccurs="0"&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}li"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}ol"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}ul"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}a"/&amp;gt;
 *       &amp;lt;/choice&amp;gt;
 *       &amp;lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="rowspan" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="colspan" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="abbr" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="headers" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "content"
})
@XmlRootElement(name = "td")
public class Td {

    @XmlElementRefs({
        @XmlElementRef(name = "li", namespace = "xhtml:NPfIT:PresentationText", type = Li.class, required = false),
        @XmlElementRef(name = "ol", namespace = "xhtml:NPfIT:PresentationText", type = Ol.class, required = false),
        @XmlElementRef(name = "ul", namespace = "xhtml:NPfIT:PresentationText", type = Ul.class, required = false),
        @XmlElementRef(name = "a", namespace = "xhtml:NPfIT:PresentationText", type = A.class, required = false)
    })
    @XmlMixed
    protected List<Object> content;
    @XmlAttribute(name = "id")
    protected String id;
    @XmlAttribute(name = "rowspan")
    protected String rowspan;
    @XmlAttribute(name = "colspan")
    protected String colspan;
    @XmlAttribute(name = "abbr")
    protected String abbr;
    @XmlAttribute(name = "headers")
    protected String headers;

    /**
     * Gets the value of the content property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the content property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getContent().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link Li }
     * {@link Ol }
     * {@link Ul }
     * {@link A }
     * {@link String }
     * 
     * 
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<>();
        }
        return this.content;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the rowspan property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRowspan() {
        return rowspan;
    }

    /**
     * Sets the value of the rowspan property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRowspan(String value) {
        this.rowspan = value;
    }

    /**
     * Gets the value of the colspan property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColspan() {
        return colspan;
    }

    /**
     * Sets the value of the colspan property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColspan(String value) {
        this.colspan = value;
    }

    /**
     * Gets the value of the abbr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAbbr() {
        return abbr;
    }

    /**
     * Sets the value of the abbr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAbbr(String value) {
        this.abbr = value;
    }

    /**
     * Gets the value of the headers property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeaders() {
        return headers;
    }

    /**
     * Sets the value of the headers property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeaders(String value) {
        this.headers = value;
    }

}
