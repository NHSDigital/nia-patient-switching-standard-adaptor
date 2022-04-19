package org.hl7.v3;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;


/**
 * &lt;p&gt;Java class for COPC_IN000001UK01.Message complex type.
 *
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 *
 * &lt;pre&gt;
 * &amp;lt;complexType name="COPC_IN000001UK01.Message"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="id" type="{urn:hl7-org:v3}II"/&amp;gt;
 *         &amp;lt;element name="creationTime" type="{urn:hl7-org:v3}TS"/&amp;gt;
 *         &amp;lt;element name="versionCode" type="{urn:hl7-org:v3}CS"/&amp;gt;
 *         &amp;lt;element name="interactionId" type="{urn:hl7-org:v3}II"/&amp;gt;
 *         &amp;lt;element name="processingCode" type="{urn:hl7-org:v3}CS"/&amp;gt;
 *         &amp;lt;element name="processingModeCode" type="{urn:hl7-org:v3}CS"/&amp;gt;
 *         &amp;lt;element name="acceptAckCode" type="{urn:hl7-org:v3}CS"/&amp;gt;
 *         &amp;lt;element name="communicationFunctionRcv" type="{urn:hl7-org:v3}MCCI_MT010101UK12.CommunicationFunctionRcv" maxOccurs="unbounded"/&amp;gt;
 *         &amp;lt;element name="communicationFunctionSnd" type="{urn:hl7-org:v3}MCCI_MT010101UK12.CommunicationFunctionSnd"/&amp;gt;
 *         &amp;lt;element name="ControlActEvent" type="{urn:hl7-org:v3}RCMR_IN030000UK06.ControlActEvent"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="type" type="{urn:hl7-org:v3}Classes" default="Message" /&amp;gt;
 *       &amp;lt;attribute name="typeID"&amp;gt;
 *         &amp;lt;simpleType&amp;gt;
 *           &amp;lt;list itemType="{urn:hl7-org:v3}oid" /&amp;gt;
 *         &amp;lt;/simpleType&amp;gt;
 *       &amp;lt;/attribute&amp;gt;
 *       &amp;lt;attribute name="realmCode"&amp;gt;
 *         &amp;lt;simpleType&amp;gt;
 *           &amp;lt;list itemType="{urn:hl7-org:v3}cs" /&amp;gt;
 *         &amp;lt;/simpleType&amp;gt;
 *       &amp;lt;/attribute&amp;gt;
 *       &amp;lt;attribute name="nullFlavor" type="{urn:hl7-org:v3}cs" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "COPC_IN000001UK01.Message", propOrder = {
        "id",
        "creationTime",
        "versionCode",
        "interactionId",
        "processingCode",
        "processingModeCode",
        "acceptAckCode",
        "communicationFunctionRcv",
        "communicationFunctionSnd",
        "controlActEvent"
})

@Getter
@Setter
public class COPCIN000001UK01Message {

    @XmlElement(required = true)
    protected II id;
    @XmlElement(required = true)
    protected TS creationTime;
    @XmlElement(required = true)
    protected CS versionCode;
    @XmlElement(required = true)
    protected II interactionId;
    @XmlElement(required = true)
    protected CS processingCode;
    @XmlElement(required = true)
    protected CS processingModeCode;
    @XmlElement(required = true)
    protected CS acceptAckCode;
    @XmlElement(required = true)
    protected List<MCCIMT010101UK12CommunicationFunctionRcv> communicationFunctionRcv;
    @XmlElement(required = true)
    protected MCCIMT010101UK12CommunicationFunctionSnd communicationFunctionSnd;
    @XmlElement(name = "ControlActEvent", required = true)
    protected RCMRIN030000UK06ControlActEvent controlActEvent;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "typeID")
    protected List<String> typeID;
    @XmlAttribute(name = "realmCode")
    protected List<String> realmCode;
    @XmlAttribute(name = "nullFlavor")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String nullFlavor;
}
