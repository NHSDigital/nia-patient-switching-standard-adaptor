
package org.hl7.v3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * 
 * Mailing and home or office addresses. A sequence of address parts,
 * such as street or post office Box, city, postal code, country, etc.
 * 
 * 
 * &lt;p&gt;Java class for AD complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="AD"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;extension base="{urn:hl7-org:v3}ANY"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;choice maxOccurs="unbounded" minOccurs="0"&amp;gt;
 *           &amp;lt;element name="delimiter"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="DEL" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="country"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="CNT" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="state"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="STA" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="county"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="CPA" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="city"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="CTY" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="postalCode"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="ZIP" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="streetAddressLine"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="SAL" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="houseNumber"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="BNR" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="houseNumberNumeric"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="BNN" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="direction"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="DIR" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="streetName"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="STR" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="streetNameBase"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="STB" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="streetNameType"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="STTYP" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="additionalLocator"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="ADL" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="unitID"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="UNID" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="unitType"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="UNIT" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="carrier"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="CAR" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="censusTract"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="CEN" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="addressKey"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="ADDRK" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *           &amp;lt;element name="desc"&amp;gt;
 *             &amp;lt;complexType&amp;gt;
 *               &amp;lt;complexContent&amp;gt;
 *                 &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *                   &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="DESC" /&amp;gt;
 *                 &amp;lt;/restriction&amp;gt;
 *               &amp;lt;/complexContent&amp;gt;
 *             &amp;lt;/complexType&amp;gt;
 *           &amp;lt;/element&amp;gt;
 *         &amp;lt;/choice&amp;gt;
 *         &amp;lt;element name="useablePeriod" type="{urn:hl7-org:v3}IVL_TS" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="id" type="{urn:hl7-org:v3}II" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="use" type="{urn:hl7-org:v3}set_cs_PostalAddressUse" /&amp;gt;
 *       &amp;lt;attribute name="isNotOrdered" type="{urn:hl7-org:v3}bl" /&amp;gt;
 *     &amp;lt;/extension&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AD", propOrder = {
    "content",
    "streetAddressLine",
    "postalCode",
})
public class AD {

    @XmlElementRefs({
        @XmlElementRef(name = "delimiter", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "country", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "state", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "county", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "city", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "postalCode", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "streetAddressLine", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "houseNumber", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "houseNumberNumeric", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "direction", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "streetName", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "streetNameBase", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "streetNameType", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "additionalLocator", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "unitID", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "unitType", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "carrier", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "censusTract", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "addressKey", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "desc", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "useablePeriod", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "id", namespace = "urn:hl7-org:v3", type = JAXBElement.class, required = false)
    })
    @XmlMixed
    protected List<Serializable> content;
    @XmlAttribute(name = "use")
    protected List<CsPostalAddressUse> use;
    @XmlAttribute(name = "isNotOrdered")
    protected Boolean isNotOrdered;

    /**
     * Custom-made AD attributes used when mapping Location elements
     */
    protected List<String> streetAddressLine;
    protected String postalCode;

    /**
     * 
     * Mailing and home or office addresses. A sequence of address parts,
     * such as street or post office Box, city, postal code, country, etc.
     * Gets the value of the content property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the content property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getContent().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link AD.Delimiter }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.Country }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.State }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.County }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.City }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.PostalCode }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.StreetAddressLine }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.HouseNumber }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.HouseNumberNumeric }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.Direction }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.StreetName }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.StreetNameBase }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.StreetNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.AdditionalLocator }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.UnitID }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.UnitType }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.Carrier }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.CensusTract }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.AddressKey }{@code >}
     * {@link JAXBElement }{@code <}{@link AD.Desc }{@code >}
     * {@link JAXBElement }{@code <}{@link IVLTS }{@code >}
     * {@link JAXBElement }{@code <}{@link II }{@code >}
     * {@link String }
     * 
     * 
     */
    public List<Serializable> getContent() {
        if (content == null) {
            content = new ArrayList<Serializable>();
        }
        return this.content;
    }

    /**
     * Gets the value of the use property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the use property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getUse().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link CsPostalAddressUse }
     * 
     * 
     */
    public List<CsPostalAddressUse> getUse() {
        if (use == null) {
            use = new ArrayList<CsPostalAddressUse>();
        }
        return this.use;
    }

    /**
     * Gets the value of the isNotOrdered property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsNotOrdered() {
        return isNotOrdered;
    }

    /**
     * Sets the value of the isNotOrdered property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsNotOrdered(Boolean value) {
        this.isNotOrdered = value;
    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="ADL" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class AdditionalLocator
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="ADDRK" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "content"
    })
    public static class AddressKey {

        @XmlValue
        protected String content;
        @XmlAttribute(name = "partType")
        protected CsAddressPartType partType;

        /**
         * Gets the value of the content property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getContent() {
            return content;
        }

        /**
         * Sets the value of the content property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setContent(String value) {
            this.content = value;
        }

        /**
         * Gets the value of the partType property.
         * 
         * @return
         *     possible object is
         *     {@link CsAddressPartType }
         *     
         */
        public CsAddressPartType getPartType() {
            if (partType == null) {
                return CsAddressPartType.ADDRK;
            } else {
                return partType;
            }
        }

        /**
         * Sets the value of the partType property.
         * 
         * @param value
         *     allowed object is
         *     {@link CsAddressPartType }
         *     
         */
        public void setPartType(CsAddressPartType value) {
            this.partType = value;
        }

    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="CAR" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Carrier
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="CEN" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class CensusTract
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="CTY" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class City
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="CNT" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Country
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="CPA" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class County
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="DEL" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Delimiter
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="DESC" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "content"
    })
    public static class Desc {

        @XmlValue
        protected String content;
        @XmlAttribute(name = "partType")
        protected CsAddressPartType partType;

        /**
         * Gets the value of the content property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getContent() {
            return content;
        }

        /**
         * Sets the value of the content property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setContent(String value) {
            this.content = value;
        }

        /**
         * Gets the value of the partType property.
         * 
         * @return
         *     possible object is
         *     {@link CsAddressPartType }
         *     
         */
        public CsAddressPartType getPartType() {
            if (partType == null) {
                return CsAddressPartType.DESC;
            } else {
                return partType;
            }
        }

        /**
         * Sets the value of the partType property.
         * 
         * @param value
         *     allowed object is
         *     {@link CsAddressPartType }
         *     
         */
        public void setPartType(CsAddressPartType value) {
            this.partType = value;
        }

    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="DIR" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Direction
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="BNR" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class HouseNumber
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="BNN" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class HouseNumberNumeric
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="ZIP" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class PostalCode
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="STA" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class State
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="SAL" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class StreetAddressLine
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="STR" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class StreetName
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="STB" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class StreetNameBase
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="STTYP" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class StreetNameType
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="UNID" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class UnitID
        extends ADXP
    {


    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;restriction base="{urn:hl7-org:v3}ADXP"&amp;gt;
     *       &amp;lt;attribute name="partType" type="{urn:hl7-org:v3}cs_AddressPartType" fixed="UNIT" /&amp;gt;
     *     &amp;lt;/restriction&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class UnitType
        extends ADXP
    {


    }

    public List<String> getStreetAddressLine() {
        return streetAddressLine;
    }

    public void setStreetAddressLine(List<String> streetAddressLine) {
        this.streetAddressLine = streetAddressLine;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

}
