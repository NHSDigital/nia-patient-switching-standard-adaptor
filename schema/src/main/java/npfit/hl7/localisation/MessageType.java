package npfit.hl7.localisation;

import org.hl7.v3.II;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for Message.Type complex type.
 *
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 *
 * &lt;pre&gt;
 * &amp;lt;complexType name="Message.Type"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{urn:hl7-org:v3}II"&amp;gt;
 *       &amp;lt;attribute name="root" use="required" type="{NPFIT:HL7:Localisation}messagetype.root" /&amp;gt;
 *       &amp;lt;attribute name="extension" use="required" type="{NPFIT:HL7:Localisation}messagetype.extension" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Message.Type")
public class MessageType
    extends II
{


}