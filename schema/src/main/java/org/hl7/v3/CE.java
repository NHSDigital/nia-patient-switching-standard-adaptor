
package org.hl7.v3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * Coded data, consists of a coded value (CV) and, optionally, coded
 * value(s) from other coding systems that identify the same
 * concept. Used when alternative codes may exist.
 * 
 * 
 * &lt;p&gt;Java class for CE complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="CE"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{urn:hl7-org:v3}CD"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="qualifier" type="{urn:hl7-org:v3}CR" maxOccurs="0" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="originalText" type="{urn:hl7-org:v3}ED" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="translation" type="{urn:hl7-org:v3}CD" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="code" type="{urn:hl7-org:v3}cs" /&amp;gt;
 *       &amp;lt;attribute name="codeSystem" type="{urn:hl7-org:v3}uid" /&amp;gt;
 *       &amp;lt;attribute name="codeSystemName" type="{urn:hl7-org:v3}st" /&amp;gt;
 *       &amp;lt;attribute name="codeSystemVersion" type="{urn:hl7-org:v3}st" /&amp;gt;
 *       &amp;lt;attribute name="displayName" type="{urn:hl7-org:v3}st" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CE")
@XmlSeeAlso({
    CV.class
})
public class CE
    extends CD
{


}
