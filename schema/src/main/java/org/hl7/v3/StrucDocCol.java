
package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java class for StrucDoc.Col complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="StrucDoc.Col"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&amp;gt;
 *       &amp;lt;attribute name="language" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" /&amp;gt;
 *       &amp;lt;attribute name="styleCode" type="{http://www.w3.org/2001/XMLSchema}NMTOKENS" /&amp;gt;
 *       &amp;lt;attribute name="span" type="{http://www.w3.org/2001/XMLSchema}string" default="1" /&amp;gt;
 *       &amp;lt;attribute name="width" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="align"&amp;gt;
 *         &amp;lt;simpleType&amp;gt;
 *           &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&amp;gt;
 *             &amp;lt;enumeration value="left"/&amp;gt;
 *             &amp;lt;enumeration value="center"/&amp;gt;
 *             &amp;lt;enumeration value="right"/&amp;gt;
 *             &amp;lt;enumeration value="justify"/&amp;gt;
 *             &amp;lt;enumeration value="char"/&amp;gt;
 *           &amp;lt;/restriction&amp;gt;
 *         &amp;lt;/simpleType&amp;gt;
 *       &amp;lt;/attribute&amp;gt;
 *       &amp;lt;attribute name="char" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="charoff" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="valign"&amp;gt;
 *         &amp;lt;simpleType&amp;gt;
 *           &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&amp;gt;
 *             &amp;lt;enumeration value="top"/&amp;gt;
 *             &amp;lt;enumeration value="middle"/&amp;gt;
 *             &amp;lt;enumeration value="bottom"/&amp;gt;
 *             &amp;lt;enumeration value="baseline"/&amp;gt;
 *           &amp;lt;/restriction&amp;gt;
 *         &amp;lt;/simpleType&amp;gt;
 *       &amp;lt;/attribute&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StrucDoc.Col")
public class StrucDocCol {

    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "language")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String language;
    @XmlAttribute(name = "styleCode")
    @XmlSchemaType(name = "NMTOKENS")
    protected List<String> styleCode;
    @XmlAttribute(name = "span")
    protected String span;
    @XmlAttribute(name = "width")
    protected String width;
    @XmlAttribute(name = "align")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String align;
    @XmlAttribute(name = "char")
    protected String _char;
    @XmlAttribute(name = "charoff")
    protected String charoff;
    @XmlAttribute(name = "valign")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String valign;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getID() {
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
    public void setID(String value) {
        this.id = value;
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
     * Gets the value of the styleCode property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the styleCode property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getStyleCode().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getStyleCode() {
        if (styleCode == null) {
            styleCode = new  ArrayList<>();
        }
        return this.styleCode;
    }

    /**
     * Gets the value of the span property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpan() {
        if (span == null) {
            return "1";
        } else {
            return span;
        }
    }

    /**
     * Sets the value of the span property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpan(String value) {
        this.span = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidth(String value) {
        this.width = value;
    }

    /**
     * Gets the value of the align property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlign() {
        return align;
    }

    /**
     * Sets the value of the align property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlign(String value) {
        this.align = value;
    }

    /**
     * Gets the value of the char property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChar() {
        return _char;
    }

    /**
     * Sets the value of the char property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChar(String value) {
        this._char = value;
    }

    /**
     * Gets the value of the charoff property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCharoff() {
        return charoff;
    }

    /**
     * Sets the value of the charoff property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCharoff(String value) {
        this.charoff = value;
    }

    /**
     * Gets the value of the valign property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValign() {
        return valign;
    }

    /**
     * Sets the value of the valign property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValign(String value) {
        this.valign = value;
    }

}
