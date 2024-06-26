
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * Coded data, where the domain from which the codeset comes is ordered. The
 * Coded Ordinal data type adds semantics related to ordering so that models
 * that make use of such domains may introduce model elements that involve
 * statements about the order of the terms in a domain. The representation is
 * exactly the same as CV, but the type still needs to be defined.
 * 
 * 
 * &lt;p&gt;Java class for CO complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="CO"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}CV"&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CO")
public class CO
    extends CV
{


}
