
package org.hl7.v3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * A quantity constructed as the quotient of a numerator quantity divided
 * by a denominator quantity. Common factors in the numerator and
 * denominator are not automatically cancelled out.  The  data
 * type supports titers (e.g., "1:128") and other quantities produced by
 * laboratories that truly represent ratios. Ratios are not simply
 * "structured numerics", particularly blood pressure measurements
 * (e.g. "120/60") are not ratios. In many cases the 
 * REAL should be used instead of the 
 * .
 *   
 * 
 * &lt;p&gt;Java class for RTO complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="RTO"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}RTO_QTY_QTY"&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RTO")
public class RTO
    extends RTOQTYQTY
{


}
