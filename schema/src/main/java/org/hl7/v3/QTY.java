
package org.hl7.v3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * The quantity data type is an abstract generalization for all data
 * types (1) whose value set has an order relation (less-or-equal) and
 * (2) where difference is defined in all of the data type's totally
 * ordered value subsets.  The quantity type abstraction is needed in
 * defining certain other types, such as the interval and the probability
 * distribution.
 * 
 * 
 * &lt;p&gt;Java class for QTY complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="QTY"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}ANY"&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QTY")
@XmlSeeAlso({
    REAL.class,
    PQ.class,
    MO.class,
    TS.class,
    INT.class,
    RTOQTYQTY.Numerator.class,
    RTOQTYQTY.Denominator.class,
    RTOQTYQTY.class
})
public abstract class QTY
    extends ANY
{


}
