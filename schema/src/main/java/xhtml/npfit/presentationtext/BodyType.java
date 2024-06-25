
package xhtml.npfit.presentationtext;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for bodyType complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="bodyType"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;choice maxOccurs="unbounded" minOccurs="0"&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}h2"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}h3"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}h4"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}h5"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}h6"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}p"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}ol"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}ul"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}table"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}a"/&amp;gt;
 *         &amp;lt;element ref="{xhtml:NPfIT:PresentationText}pre"/&amp;gt;
 *         &amp;lt;element name="br" type="{xhtml:NPfIT:PresentationText}brType"/&amp;gt;
 *       &amp;lt;/choice&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "bodyType", propOrder = {
    "h2OrH3OrH4"
})
public class BodyType {

    @XmlElements({
        @XmlElement(name = "h2", type = H2 .class),
        @XmlElement(name = "h3", type = H3 .class),
        @XmlElement(name = "h4", type = H4 .class),
        @XmlElement(name = "h5", type = H5 .class),
        @XmlElement(name = "h6", type = H6 .class),
        @XmlElement(name = "p", type = P.class),
        @XmlElement(name = "ol", type = Ol.class),
        @XmlElement(name = "ul", type = Ul.class),
        @XmlElement(name = "table", type = Table.class),
        @XmlElement(name = "a", type = A.class),
        @XmlElement(name = "pre", type = Pre.class),
        @XmlElement(name = "br", type = BrType.class)
    })
    protected List<Object> h2OrH3OrH4;

    /**
     * Gets the value of the h2OrH3OrH4 property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the h2OrH3OrH4 property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getH2OrH3OrH4().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link H2 }
     * {@link H3 }
     * {@link H4 }
     * {@link H5 }
     * {@link H6 }
     * {@link P }
     * {@link Ol }
     * {@link Ul }
     * {@link Table }
     * {@link A }
     * {@link Pre }
     * {@link BrType }
     * 
     * 
     */
    public List<Object> getH2OrH3OrH4() {
        if (h2OrH3OrH4 == null) {
            h2OrH3OrH4 = new ArrayList<>();
        }
        return this.h2OrH3OrH4;
    }

}
