
package org.hl7.v3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * &lt;p&gt;Java class for IVL_TS complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="IVL_TS"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}SXCM_TS"&amp;gt;
 *       &amp;lt;choice minOccurs="0"&amp;gt;
 *         &amp;lt;sequence&amp;gt;
 *           &amp;lt;element name="low" type="{urn:hl7-org:v3}IVXB_TS"/&amp;gt;
 *           &amp;lt;choice minOccurs="0"&amp;gt;
 *             &amp;lt;element name="width" type="{urn:hl7-org:v3}PQ" minOccurs="0"/&amp;gt;
 *             &amp;lt;element name="high" type="{urn:hl7-org:v3}IVXB_TS" minOccurs="0"/&amp;gt;
 *           &amp;lt;/choice&amp;gt;
 *         &amp;lt;/sequence&amp;gt;
 *         &amp;lt;element name="high" type="{urn:hl7-org:v3}IVXB_TS"/&amp;gt;
 *         &amp;lt;sequence&amp;gt;
 *           &amp;lt;element name="width" type="{urn:hl7-org:v3}PQ"/&amp;gt;
 *           &amp;lt;element name="high" type="{urn:hl7-org:v3}IVXB_TS" minOccurs="0"/&amp;gt;
 *         &amp;lt;/sequence&amp;gt;
 *         &amp;lt;sequence&amp;gt;
 *           &amp;lt;element name="center" type="{urn:hl7-org:v3}TS"/&amp;gt;
 *           &amp;lt;element name="width" type="{urn:hl7-org:v3}PQ" minOccurs="0"/&amp;gt;
 *         &amp;lt;/sequence&amp;gt;
 *       &amp;lt;/choice&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IVL_TS", propOrder = {
    "low",
    "width",
    "high",
    "center"
})
public class IVLTS
    extends SXCMTS
{

    protected IVXBTS low;
    protected PQ width;
    protected IVXBTS high;
    protected TS center;

    public IVXBTS getLow() {
        return low;
    }

    public void setLow(IVXBTS low) {
        this.low = low;
    }

    public PQ getWidth() {
        return width;
    }

    public void setWidth(PQ width) {
        this.width = width;
    }

    public IVXBTS getHigh() {
        return high;
    }

    public void setHigh(IVXBTS high) {
        this.high = high;
    }

    public TS getCenter() {
        return center;
    }

    public void setCenter(TS center) {
        this.center = center;
    }
}
