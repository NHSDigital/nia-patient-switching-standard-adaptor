
package xhtml.npfit.presentationtext;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}p"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}ol"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}ul"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}table"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}a"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}pre"/&amp;gt;
 *         &amp;lt;element name="br" type="{xhtml:NPfIT:PresentationText}brType"/&amp;gt;
 *       &amp;lt;/choice&amp;gt;
 *       &amp;lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" /&amp;gt;
 *       &amp;lt;attribute name="class" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" /&amp;gt;
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
@XmlRootElement(name = "p")
public class P {

    @XmlElementRefs({
        @XmlElementRef(name = "p", namespace = "xhtml:NPfIT:PresentationText", type = P.class, required = false),
        @XmlElementRef(name = "ol", namespace = "xhtml:NPfIT:PresentationText", type = Ol.class, required = false),
        @XmlElementRef(name = "ul", namespace = "xhtml:NPfIT:PresentationText", type = Ul.class, required = false),
        @XmlElementRef(name = "table", namespace = "xhtml:NPfIT:PresentationText", type = Table.class, required = false),
        @XmlElementRef(name = "a", namespace = "xhtml:NPfIT:PresentationText", type = A.class, required = false),
        @XmlElementRef(name = "pre", namespace = "xhtml:NPfIT:PresentationText", type = Pre.class, required = false),
        @XmlElementRef(name = "br", namespace = "xhtml:NPfIT:PresentationText", type = JAXBElement.class, required = false)
    })
    @XmlMixed
    protected List<Object> content;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String id;
    @XmlAttribute(name = "class")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String clazz;

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
     * {@link P }
     * {@link Ol }
     * {@link Ul }
     * {@link Table }
     * {@link A }
     * {@link Pre }
     * {@link JAXBElement }{@code <}{@link BrType }{@code >}
     * {@link String }
     * 
     * 
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<Object>();
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
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

}
