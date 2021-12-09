
package org.hl7.v3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for SXCM_TS complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="SXCM_TS"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}TS"&amp;gt;
 *       &amp;lt;attribute name="operator" type="{urn:hl7-org:v3}cs_SetOperator" default="I" /&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SXCM_TS")
@XmlSeeAlso({
    IVLTS.class
})
public class SXCMTS
    extends TS
{

    @XmlAttribute(name = "operator")
    protected CsSetOperator operator;

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

}
