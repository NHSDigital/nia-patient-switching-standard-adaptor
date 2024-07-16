
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * Coded data, consists of a code, display name, code system, and
 * original text. Used when a single code value must be sent.
 * 
 * 
 * &lt;p&gt;Java class for CS complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="CS"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{urn:hl7-org:v3}CV"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="code" type="{urn:hl7-org:v3}cs" /&amp;gt;
 *       &amp;lt;attribute name="codeSystem" type="{urn:hl7-org:v3}uid" /&amp;gt;
 *       &amp;lt;attribute name="codeSystemName" type="{urn:hl7-org:v3}st" /&amp;gt;
 *       &amp;lt;attribute name="codeSystemVersion" type="{urn:hl7-org:v3}st" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CS")
public class CS
    extends CV
{


}
