
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * A name for an organization. A sequence of name parts.
 * 
 * 
 * &lt;p&gt;Java class for ON complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="ON"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{urn:hl7-org:v3}EN"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;choice maxOccurs="unbounded" minOccurs="0"&amp;gt;
 *           &amp;lt;element name="delimiter" type="{urn:hl7-org:v3}en.delimiter"/&amp;gt;
 *           &amp;lt;element name="prefix" type="{urn:hl7-org:v3}en.prefix"/&amp;gt;
 *           &amp;lt;element name="suffix" type="{urn:hl7-org:v3}en.suffix"/&amp;gt;
 *         &amp;lt;/choice&amp;gt;
 *         &amp;lt;element name="validTime" type="{urn:hl7-org:v3}IVL_TS" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="use" type="{urn:hl7-org:v3}set_cs_EntityNameUse" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ON")
public class ON
    extends EN
{


}
