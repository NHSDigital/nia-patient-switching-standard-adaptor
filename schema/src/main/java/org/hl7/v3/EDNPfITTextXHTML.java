
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 			This is falvour ED data type to restrict only XHTML TEXT as content within the ED data. 
 * 			This falvour would be used in POC and GP2GP summart messages.
 * 
 * 
 * &lt;p&gt;Java class for ED.NPfIT.Text.XHTML complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="ED.NPfIT.Text.XHTML"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{urn:hl7-org:v3}ED"&amp;gt;
 *       &amp;lt;group ref="{xhtml:NPfIT:PresentationText}htmlRoot"/&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ED.NPfIT.Text.XHTML")
public class EDNPfITTextXHTML
    extends ED
{


}
