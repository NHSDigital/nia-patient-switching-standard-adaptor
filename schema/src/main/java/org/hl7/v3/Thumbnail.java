
package org.hl7.v3;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * A thumbnail is an abbreviated rendition of the full data. A thumbnail
 * requires significantly fewer resources than the full data, while still
 * maintaining some distinctive similarity with the full data. A
 * thumbnail is typically used with by-reference encapsulated data. It
 * allows a user to select data more efficiently before actually
 * downloading through the reference.
 * 
 * 
 * &lt;p&gt;Java class for thumbnail complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="thumbnail"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{urn:hl7-org:v3}ED"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="reference" type="{urn:hl7-org:v3}TEL" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="thumbnail" type="{urn:hl7-org:v3}thumbnail" maxOccurs="0" minOccurs="0"/&amp;gt;
 *         &amp;lt;any processContents='skip' namespace='##other' maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "thumbnail")
public class Thumbnail
    extends ED
{


}
