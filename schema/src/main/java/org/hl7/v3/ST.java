
package org.hl7.v3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * Data that is primarily intended for human interpretation or for
 * further machine processing is outside the scope of HL7. This includes
 * unformatted or formatted written language, multimedia data, or
 * structured information as defined by a different standard (e.g.,
 * XML-signatures.)  Instead of the data itself, an ED
 * may contain only a reference (see TEL.) Note that
 * the ST data type is a specialization of the
 * ED data type when the ED
 * media type is text/plain.
 * 
 * 
 * &lt;p&gt;Java class for ST complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="ST"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{urn:hl7-org:v3}ED"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="reference" type="{urn:hl7-org:v3}TEL" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="thumbnail" type="{urn:hl7-org:v3}thumbnail" minOccurs="0"/&amp;gt;
 *         &amp;lt;any processContents='skip' namespace='##other' maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="mediaType" type="{urn:hl7-org:v3}cs" default="text/plain" /&amp;gt;
 *       &amp;lt;attribute name="language" type="{urn:hl7-org:v3}cs" /&amp;gt;
 *       &amp;lt;attribute name="compression" type="{urn:hl7-org:v3}cs_CompressionAlgorithm" /&amp;gt;
 *       &amp;lt;attribute name="integrityCheck" type="{urn:hl7-org:v3}bin" /&amp;gt;
 *       &amp;lt;attribute name="integrityCheckAlgorithm" type="{urn:hl7-org:v3}cs_IntegrityCheckAlgorithm" default="SHA-1" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ST")
@XmlSeeAlso({
    SC.class,
    ADXP.class,
    ENXP.class
})
public class ST
    extends ED
{


}
