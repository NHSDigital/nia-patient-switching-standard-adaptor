package org.hl7.v3;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "translation",
    "rest",
    "low",
    "width",
    "high",
    "center"
})
public class Value extends ANY {
    // PQ fields:
    protected List<PQR> translation;
    @XmlAttribute(name = "value")
    protected String value;
    @XmlAttribute(name = "unit")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String unit;

    // IVLPQ fields:
    protected List<JAXBElement<?>> rest;
    @XmlAttribute(name = "operator")
    protected CsSetOperator operator;
    protected PQInc low;
    protected PQInc width;
    protected PQInc high;
    protected PQInc center;

    /**
     * Gets the value of the translation property.
     *
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the translation property.
     *
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getTranslation().add(newItem);
     * &lt;/pre&gt;
     *
     *
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link PQR }
     *
     *
     */
    public List<PQR> getTranslation() {
        if (translation == null) {
            translation = new ArrayList<PQR>();
        }
        return this.translation;
    }

    /**
     * Gets the value of the value property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the unit property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUnit() {
        if (unit == null) {
            return "1";
        } else {
            return unit;
        }
    }

    /**
     * Sets the value of the unit property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    /**
     * Gets the rest of the content model.
     *
     * &lt;p&gt;
     * You are getting this "catch-all" property because of the following reason:
     * The field name "High" is used by two different parts of a schema. See:
     * line 2116 of file:/Users/malgorzatar/nhs/nia-patient-switching-standard-adaptor/schema/src/main/resources/dt/datatypes.xsd
     * line 2113 of file:/Users/malgorzatar/nhs/nia-patient-switching-standard-adaptor/schema/src/main/resources/dt/datatypes.xsd
     * &lt;p&gt;
     * To get rid of this property, apply a property customization to one
     * of both of the following declarations to change their names:
     * Gets the value of the rest property.
     *
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the rest property.
     *
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getRest().add(newItem);
     * &lt;/pre&gt;
     *
     *
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link PQInc }{@code >}
     * {@link JAXBElement }{@code <}{@link PQ }{@code >}
     * {@link JAXBElement }{@code <}{@link PQInc }{@code >}
     * {@link JAXBElement }{@code <}{@link PQ }{@code >}
     *
     *
     */
    public List<JAXBElement<?>> getRest() {
        if (rest == null) {
            rest = new ArrayList<JAXBElement<?>>();
        }
        return this.rest;
    }

    /**
     * Gets the value of the operator property.
     *
     * @return
     *     possible object is
     *     {@link CsSetOperator }
     *
     */
    public CsSetOperator getOperator() {
        if (operator == null) {
            return CsSetOperator.I;
        } else {
            return operator;
        }
    }

    /**
     * Sets the value of the operator property.
     *
     * @param value
     *     allowed object is
     *     {@link CsSetOperator }
     *
     */
    public void setOperator(CsSetOperator value) {
        this.operator = value;
    }

    public PQInc getLow() {
        return low;
    }

    public void setLow(PQInc low) {
        this.low = low;
    }

    public PQInc getWidth() {
        return width;
    }

    public void setWidth(PQInc width) {
        this.width = width;
    }

    public PQInc getHigh() {
        return high;
    }

    public void setHigh(PQInc high) {
        this.high = high;
    }

    public PQInc getCenter() {
        return center;
    }

    public void setCenter(PQInc center) {
        this.center = center;
    }
}
