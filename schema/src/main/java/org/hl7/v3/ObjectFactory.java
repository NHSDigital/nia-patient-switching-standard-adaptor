
package org.hl7.v3;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.hl7.v3 package. 
 * &lt;p&gt;An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ControlActEvent_QNAME = new QName("urn:hl7-org:v3", "ControlActEvent");
    private final static QName _EhrExtract_QNAME = new QName("urn:hl7-org:v3", "EhrExtract");
    private final static QName _Message_QNAME = new QName("urn:hl7-org:v3", "Message");
    private final static QName _RCMRIN030000UK06_QNAME = new QName("urn:hl7-org:v3", "RCMR_IN030000UK06");
    private final static QName _IVLINTLow_QNAME = new QName("urn:hl7-org:v3", "low");
    private final static QName _IVLINTHigh_QNAME = new QName("urn:hl7-org:v3", "high");
    private final static QName _IVLPQWidth_QNAME = new QName("urn:hl7-org:v3", "width");
    private final static QName _IVLPQCenter_QNAME = new QName("urn:hl7-org:v3", "center");
    private final static QName _ENDelimiter_QNAME = new QName("urn:hl7-org:v3", "delimiter");
    private final static QName _ENFamily_QNAME = new QName("urn:hl7-org:v3", "family");
    private final static QName _ENGiven_QNAME = new QName("urn:hl7-org:v3", "given");
    private final static QName _ENPrefix_QNAME = new QName("urn:hl7-org:v3", "prefix");
    private final static QName _ENSuffix_QNAME = new QName("urn:hl7-org:v3", "suffix");
    private final static QName _ENValidTime_QNAME = new QName("urn:hl7-org:v3", "validTime");
    private final static QName _ENId_QNAME = new QName("urn:hl7-org:v3", "id");
    private final static QName _ADCountry_QNAME = new QName("urn:hl7-org:v3", "country");
    private final static QName _ADState_QNAME = new QName("urn:hl7-org:v3", "state");
    private final static QName _ADCounty_QNAME = new QName("urn:hl7-org:v3", "county");
    private final static QName _ADCity_QNAME = new QName("urn:hl7-org:v3", "city");
    private final static QName _ADPostalCode_QNAME = new QName("urn:hl7-org:v3", "postalCode");
    private final static QName _ADStreetAddressLine_QNAME = new QName("urn:hl7-org:v3", "streetAddressLine");
    private final static QName _ADHouseNumber_QNAME = new QName("urn:hl7-org:v3", "houseNumber");
    private final static QName _ADHouseNumberNumeric_QNAME = new QName("urn:hl7-org:v3", "houseNumberNumeric");
    private final static QName _ADDirection_QNAME = new QName("urn:hl7-org:v3", "direction");
    private final static QName _ADStreetName_QNAME = new QName("urn:hl7-org:v3", "streetName");
    private final static QName _ADStreetNameBase_QNAME = new QName("urn:hl7-org:v3", "streetNameBase");
    private final static QName _ADStreetNameType_QNAME = new QName("urn:hl7-org:v3", "streetNameType");
    private final static QName _ADAdditionalLocator_QNAME = new QName("urn:hl7-org:v3", "additionalLocator");
    private final static QName _ADUnitID_QNAME = new QName("urn:hl7-org:v3", "unitID");
    private final static QName _ADUnitType_QNAME = new QName("urn:hl7-org:v3", "unitType");
    private final static QName _ADCarrier_QNAME = new QName("urn:hl7-org:v3", "carrier");
    private final static QName _ADCensusTract_QNAME = new QName("urn:hl7-org:v3", "censusTract");
    private final static QName _ADAddressKey_QNAME = new QName("urn:hl7-org:v3", "addressKey");
    private final static QName _ADDesc_QNAME = new QName("urn:hl7-org:v3", "desc");
    private final static QName _ADUseablePeriod_QNAME = new QName("urn:hl7-org:v3", "useablePeriod");
    private final static QName _StrucDocThContent_QNAME = new QName("urn:hl7-org:v3", "content");
    private final static QName _StrucDocThLinkHtml_QNAME = new QName("urn:hl7-org:v3", "linkHtml");
    private final static QName _StrucDocThSub_QNAME = new QName("urn:hl7-org:v3", "sub");
    private final static QName _StrucDocThSup_QNAME = new QName("urn:hl7-org:v3", "sup");
    private final static QName _StrucDocThBr_QNAME = new QName("urn:hl7-org:v3", "br");
    private final static QName _StrucDocThFootnote_QNAME = new QName("urn:hl7-org:v3", "footnote");
    private final static QName _StrucDocThFootnoteRef_QNAME = new QName("urn:hl7-org:v3", "footnoteRef");
    private final static QName _StrucDocThRenderMultiMedia_QNAME = new QName("urn:hl7-org:v3", "renderMultiMedia");
    private final static QName _StrucDocTdParagraph_QNAME = new QName("urn:hl7-org:v3", "paragraph");
    private final static QName _StrucDocTdList_QNAME = new QName("urn:hl7-org:v3", "list");
    private final static QName _StrucDocParagraphCaption_QNAME = new QName("urn:hl7-org:v3", "caption");
    private final static QName _StrucDocItemTable_QNAME = new QName("urn:hl7-org:v3", "table");

    /**
     * List of custom-built QName objects that weren't pre-generated. These are being used in unit tests to test specific
     * deeply nested elements.
     *
     */
    private final static QName _CD_QNAME = new QName("urn:hl7-org:v3", "code");
    private final static QName _VALUE_QNAME = new QName("urn:hl7-org:v3", "value");
    private final static QName _EHR_COMPOSITION_QNAME = new QName("urn:hl7-org:v3", "ehrComposition");
    private final static QName _LINKSET_QNAME = new QName("urn:hl7-org:v3", "LinkSet");
    private final static QName _OBSERVATION_STATEMENT_QNAME = new QName("urn:hl7-org:v3", "ObservationStatement");
    private final static QName _PATIENT_QNAME = new QName("urn:hl7-org:v3", "patient");
    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.hl7.v3
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RTOQTYQTY }
     * 
     */
    public RTOQTYQTY createRTOQTYQTY() {
        return new RTOQTYQTY();
    }

    /**
     * Create an instance of {@link IVLINT }
     * 
     */
    public IVLINT createIVLINT() {
        return new IVLINT();
    }

    /**
     * Create an instance of {@link CD }
     * 
     */
    public CD createCD() {
        return new CD();
    }

    /**
     * Create an instance of {@link AD }
     * 
     */
    public AD createAD() {
        return new AD();
    }

    /**
     * Create an instance of {@link MCAIMT040101UK03ControlActEvent }
     * 
     */
    public MCAIMT040101UK03ControlActEvent createMCAIMT040101UK03ControlActEvent() {
        return new MCAIMT040101UK03ControlActEvent();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04EhrExtract }
     * 
     */
    public RCMRMT030101UK04EhrExtract createRCMRMT030101UK04EhrExtract() {
        return new RCMRMT030101UK04EhrExtract();
    }

    /**
     * Create an instance of {@link MCCIMT010101UK12Message }
     * 
     */
    public MCCIMT010101UK12Message createMCCIMT010101UK12Message() {
        return new MCCIMT010101UK12Message();
    }

    /**
     * Create an instance of {@link RCMRIN030000UK06Message }
     * 
     */
    public RCMRIN030000UK06Message createRCMRIN030000UK06Message() {
        return new RCMRIN030000UK06Message();
    }

    /**
     * Create an instance of {@link StrucDocText }
     * 
     */
    public StrucDocText createStrucDocText() {
        return new StrucDocText();
    }

    /**
     * Create an instance of {@link StrucDocTitle }
     * 
     */
    public StrucDocTitle createStrucDocTitle() {
        return new StrucDocTitle();
    }

    /**
     * Create an instance of {@link StrucDocBr }
     * 
     */
    public StrucDocBr createStrucDocBr() {
        return new StrucDocBr();
    }

    /**
     * Create an instance of {@link StrucDocCaption }
     * 
     */
    public StrucDocCaption createStrucDocCaption() {
        return new StrucDocCaption();
    }

    /**
     * Create an instance of {@link StrucDocCol }
     * 
     */
    public StrucDocCol createStrucDocCol() {
        return new StrucDocCol();
    }

    /**
     * Create an instance of {@link StrucDocColgroup }
     * 
     */
    public StrucDocColgroup createStrucDocColgroup() {
        return new StrucDocColgroup();
    }

    /**
     * Create an instance of {@link StrucDocContent }
     * 
     */
    public StrucDocContent createStrucDocContent() {
        return new StrucDocContent();
    }

    /**
     * Create an instance of {@link StrucDocTitleContent }
     * 
     */
    public StrucDocTitleContent createStrucDocTitleContent() {
        return new StrucDocTitleContent();
    }

    /**
     * Create an instance of {@link StrucDocFootnote }
     * 
     */
    public StrucDocFootnote createStrucDocFootnote() {
        return new StrucDocFootnote();
    }

    /**
     * Create an instance of {@link StrucDocTitleFootnote }
     * 
     */
    public StrucDocTitleFootnote createStrucDocTitleFootnote() {
        return new StrucDocTitleFootnote();
    }

    /**
     * Create an instance of {@link StrucDocFootnoteRef }
     * 
     */
    public StrucDocFootnoteRef createStrucDocFootnoteRef() {
        return new StrucDocFootnoteRef();
    }

    /**
     * Create an instance of {@link StrucDocItem }
     * 
     */
    public StrucDocItem createStrucDocItem() {
        return new StrucDocItem();
    }

    /**
     * Create an instance of {@link StrucDocLinkHtml }
     * 
     */
    public StrucDocLinkHtml createStrucDocLinkHtml() {
        return new StrucDocLinkHtml();
    }

    /**
     * Create an instance of {@link StrucDocList }
     * 
     */
    public StrucDocList createStrucDocList() {
        return new StrucDocList();
    }

    /**
     * Create an instance of {@link StrucDocParagraph }
     * 
     */
    public StrucDocParagraph createStrucDocParagraph() {
        return new StrucDocParagraph();
    }

    /**
     * Create an instance of {@link StrucDocRenderMultiMedia }
     * 
     */
    public StrucDocRenderMultiMedia createStrucDocRenderMultiMedia() {
        return new StrucDocRenderMultiMedia();
    }

    /**
     * Create an instance of {@link StrucDocSub }
     * 
     */
    public StrucDocSub createStrucDocSub() {
        return new StrucDocSub();
    }

    /**
     * Create an instance of {@link StrucDocSup }
     * 
     */
    public StrucDocSup createStrucDocSup() {
        return new StrucDocSup();
    }

    /**
     * Create an instance of {@link StrucDocTable }
     * 
     */
    public StrucDocTable createStrucDocTable() {
        return new StrucDocTable();
    }

    /**
     * Create an instance of {@link StrucDocTbody }
     * 
     */
    public StrucDocTbody createStrucDocTbody() {
        return new StrucDocTbody();
    }

    /**
     * Create an instance of {@link StrucDocTd }
     * 
     */
    public StrucDocTd createStrucDocTd() {
        return new StrucDocTd();
    }

    /**
     * Create an instance of {@link StrucDocTfoot }
     * 
     */
    public StrucDocTfoot createStrucDocTfoot() {
        return new StrucDocTfoot();
    }

    /**
     * Create an instance of {@link StrucDocTh }
     * 
     */
    public StrucDocTh createStrucDocTh() {
        return new StrucDocTh();
    }

    /**
     * Create an instance of {@link StrucDocThead }
     * 
     */
    public StrucDocThead createStrucDocThead() {
        return new StrucDocThead();
    }

    /**
     * Create an instance of {@link StrucDocTr }
     * 
     */
    public StrucDocTr createStrucDocTr() {
        return new StrucDocTr();
    }

    /**
     * Create an instance of {@link BL }
     * 
     */
    public BL createBL() {
        return new BL();
    }

    /**
     * Create an instance of {@link BN }
     * 
     */
    public BN createBN() {
        return new BN();
    }

    /**
     * Create an instance of {@link ED }
     * 
     */
    public ED createED() {
        return new ED();
    }

    /**
     * Create an instance of {@link EDNPfITTextXHTML }
     * 
     */
    public EDNPfITTextXHTML createEDNPfITTextXHTML() {
        return new EDNPfITTextXHTML();
    }

    /**
     * Create an instance of {@link Thumbnail }
     * 
     */
    public Thumbnail createThumbnail() {
        return new Thumbnail();
    }

    /**
     * Create an instance of {@link ST }
     * 
     */
    public ST createST() {
        return new ST();
    }

    /**
     * Create an instance of {@link CE }
     * 
     */
    public CE createCE() {
        return new CE();
    }

    /**
     * Create an instance of {@link CV }
     * 
     */
    public CV createCV() {
        return new CV();
    }

    /**
     * Create an instance of {@link CS }
     * 
     */
    public CS createCS() {
        return new CS();
    }

    /**
     * Create an instance of {@link CO }
     * 
     */
    public CO createCO() {
        return new CO();
    }

    /**
     * Create an instance of {@link CR }
     * 
     */
    public CR createCR() {
        return new CR();
    }

    /**
     * Create an instance of {@link SC }
     * 
     */
    public SC createSC() {
        return new SC();
    }

    /**
     * Create an instance of {@link II }
     * 
     */
    public II createII() {
        return new II();
    }

    /**
     * Create an instance of {@link URL }
     * 
     */
    public URL createURL() {
        return new URL();
    }

    /**
     * Create an instance of {@link TS }
     * 
     */
    public TS createTS() {
        return new TS();
    }

    /**
     * Create an instance of {@link TEL }
     * 
     */
    public TEL createTEL() {
        return new TEL();
    }

    /**
     * Create an instance of {@link ADXP }
     * 
     */
    public ADXP createADXP() {
        return new ADXP();
    }

    /**
     * Create an instance of {@link ENXP }
     * 
     */
    public ENXP createENXP() {
        return new ENXP();
    }

    /**
     * Create an instance of {@link EnDelimiter }
     * 
     */
    public EnDelimiter createEnDelimiter() {
        return new EnDelimiter();
    }

    /**
     * Create an instance of {@link EnFamily }
     * 
     */
    public EnFamily createEnFamily() {
        return new EnFamily();
    }

    /**
     * Create an instance of {@link EnGiven }
     * 
     */
    public EnGiven createEnGiven() {
        return new EnGiven();
    }

    /**
     * Create an instance of {@link EnPrefix }
     * 
     */
    public EnPrefix createEnPrefix() {
        return new EnPrefix();
    }

    /**
     * Create an instance of {@link EnSuffix }
     * 
     */
    public EnSuffix createEnSuffix() {
        return new EnSuffix();
    }

    /**
     * Create an instance of {@link EN }
     * 
     */
    public EN createEN() {
        return new EN();
    }

    /**
     * Create an instance of {@link PN }
     * 
     */
    public PN createPN() {
        return new PN();
    }

    /**
     * Create an instance of {@link ON }
     * 
     */
    public ON createON() {
        return new ON();
    }

    /**
     * Create an instance of {@link TN }
     * 
     */
    public TN createTN() {
        return new TN();
    }

    /**
     * Create an instance of {@link INT }
     * 
     */
    public INT createINT() {
        return new INT();
    }

    /**
     * Create an instance of {@link REAL }
     * 
     */
    public REAL createREAL() {
        return new REAL();
    }

    /**
     * Create an instance of {@link PQR }
     * 
     */
    public PQR createPQR() {
        return new PQR();
    }

    /**
     * Create an instance of {@link PQ }
     * 
     */
    public PQ createPQ() {
        return new PQ();
    }

    /**
     * Create an instance of {@link MO }
     * 
     */
    public MO createMO() {
        return new MO();
    }

    /**
     * Create an instance of {@link RTO }
     * 
     */
    public RTO createRTO() {
        return new RTO();
    }

    /**
     * Create an instance of {@link SXCMTS }
     * 
     */
    public SXCMTS createSXCMTS() {
        return new SXCMTS();
    }

    /**
     * Create an instance of {@link IVLTS }
     * 
     */
    public IVLTS createIVLTS() {
        return new IVLTS();
    }

    /**
     * Create an instance of {@link PQInc }
     * 
     */
    public PQInc createPQInc() {
        return new PQInc();
    }

    /**
     * Create an instance of {@link IVLPQ }
     * 
     */
    public IVLPQ createIVLPQ() {
        return new IVLPQ();
    }

    /**
     * Create an instance of {@link IVXBTS }
     * 
     */
    public IVXBTS createIVXBTS() {
        return new IVXBTS();
    }

    /**
     * Create an instance of {@link UKCTMT120301UK02AgentPersonSDS }
     * 
     */
    public UKCTMT120301UK02AgentPersonSDS createUKCTMT120301UK02AgentPersonSDS() {
        return new UKCTMT120301UK02AgentPersonSDS();
    }

    /**
     * Create an instance of {@link UKCTMT120301UK02PersonSDS }
     * 
     */
    public UKCTMT120301UK02PersonSDS createUKCTMT120301UK02PersonSDS() {
        return new UKCTMT120301UK02PersonSDS();
    }

    /**
     * Create an instance of {@link UKCTMT120901UK01AgentPersonSDS }
     * 
     */
    public UKCTMT120901UK01AgentPersonSDS createUKCTMT120901UK01AgentPersonSDS() {
        return new UKCTMT120901UK01AgentPersonSDS();
    }

    /**
     * Create an instance of {@link UKCTMT120901UK01PersonSDS }
     * 
     */
    public UKCTMT120901UK01PersonSDS createUKCTMT120901UK01PersonSDS() {
        return new UKCTMT120901UK01PersonSDS();
    }

    /**
     * Create an instance of {@link UKCTMT120901UK01Part }
     * 
     */
    public UKCTMT120901UK01Part createUKCTMT120901UK01Part() {
        return new UKCTMT120901UK01Part();
    }

    /**
     * Create an instance of {@link UKCTMT120901UK01SDSRole }
     * 
     */
    public UKCTMT120901UK01SDSRole createUKCTMT120901UK01SDSRole() {
        return new UKCTMT120901UK01SDSRole();
    }

    /**
     * Create an instance of {@link UKCTMT121001UK01AgentSystemSDS }
     * 
     */
    public UKCTMT121001UK01AgentSystemSDS createUKCTMT121001UK01AgentSystemSDS() {
        return new UKCTMT121001UK01AgentSystemSDS();
    }

    /**
     * Create an instance of {@link UKCTMT121001UK01SystemSDS }
     * 
     */
    public UKCTMT121001UK01SystemSDS createUKCTMT121001UK01SystemSDS() {
        return new UKCTMT121001UK01SystemSDS();
    }

    /**
     * Create an instance of {@link MCAIMT040101UK03Author }
     * 
     */
    public MCAIMT040101UK03Author createMCAIMT040101UK03Author() {
        return new MCAIMT040101UK03Author();
    }

    /**
     * Create an instance of {@link MCAIMT040101UK03Author2 }
     * 
     */
    public MCAIMT040101UK03Author2 createMCAIMT040101UK03Author2() {
        return new MCAIMT040101UK03Author2();
    }

    /**
     * Create an instance of {@link MCAIMT040101UK03Reason }
     * 
     */
    public MCAIMT040101UK03Reason createMCAIMT040101UK03Reason() {
        return new MCAIMT040101UK03Reason();
    }

    /**
     * Create an instance of {@link MCAIMT040101UK03DetectedIssueEvent }
     * 
     */
    public MCAIMT040101UK03DetectedIssueEvent createMCAIMT040101UK03DetectedIssueEvent() {
        return new MCAIMT040101UK03DetectedIssueEvent();
    }

    /**
     * Create an instance of {@link MCAIMT040101UK03Subject }
     * 
     */
    public MCAIMT040101UK03Subject createMCAIMT040101UK03Subject() {
        return new MCAIMT040101UK03Subject();
    }

    /**
     * Create an instance of {@link MCAIMT040101UK03Act }
     * 
     */
    public MCAIMT040101UK03Act createMCAIMT040101UK03Act() {
        return new MCAIMT040101UK03Act();
    }

    /**
     * Create an instance of {@link UKCTMT120501UK03AgentOrgSDS }
     * 
     */
    public UKCTMT120501UK03AgentOrgSDS createUKCTMT120501UK03AgentOrgSDS() {
        return new UKCTMT120501UK03AgentOrgSDS();
    }

    /**
     * Create an instance of {@link UKCTMT120501UK03OrganizationSDS }
     * 
     */
    public UKCTMT120501UK03OrganizationSDS createUKCTMT120501UK03OrganizationSDS() {
        return new UKCTMT120501UK03OrganizationSDS();
    }

    /**
     * Create an instance of {@link UKCTMT120501UK03ServiceDeliveryLocation }
     * 
     */
    public UKCTMT120501UK03ServiceDeliveryLocation createUKCTMT120501UK03ServiceDeliveryLocation() {
        return new UKCTMT120501UK03ServiceDeliveryLocation();
    }

    /**
     * Create an instance of {@link UKCTMT120501UK03Place }
     * 
     */
    public UKCTMT120501UK03Place createUKCTMT120501UK03Place() {
        return new UKCTMT120501UK03Place();
    }

    /**
     * Create an instance of {@link RCCTMT120101UK01Agent }
     * 
     */
    public RCCTMT120101UK01Agent createRCCTMT120101UK01Agent() {
        return new RCCTMT120101UK01Agent();
    }

    /**
     * Create an instance of {@link RCCTMT120101UK01Organization }
     * 
     */
    public RCCTMT120101UK01Organization createRCCTMT120101UK01Organization() {
        return new RCCTMT120101UK01Organization();
    }

    /**
     * Create an instance of {@link RCCTMT120101UK01OrganizationSDS }
     * 
     */
    public RCCTMT120101UK01OrganizationSDS createRCCTMT120101UK01OrganizationSDS() {
        return new RCCTMT120101UK01OrganizationSDS();
    }

    /**
     * Create an instance of {@link RCCTMT120101UK01PersonSDS }
     * 
     */
    public RCCTMT120101UK01PersonSDS createRCCTMT120101UK01PersonSDS() {
        return new RCCTMT120101UK01PersonSDS();
    }

    /**
     * Create an instance of {@link RCCTMT120101UK01Person }
     * 
     */
    public RCCTMT120101UK01Person createRCCTMT120101UK01Person() {
        return new RCCTMT120101UK01Person();
    }

    /**
     * Create an instance of {@link RCCTMT120101UK01DeviceSDS }
     * 
     */
    public RCCTMT120101UK01DeviceSDS createRCCTMT120101UK01DeviceSDS() {
        return new RCCTMT120101UK01DeviceSDS();
    }

    /**
     * Create an instance of {@link RCCTMT120101UK01Device }
     * 
     */
    public RCCTMT120101UK01Device createRCCTMT120101UK01Device() {
        return new RCCTMT120101UK01Device();
    }

    /**
     * Create an instance of {@link RCCTMT120101UK01AgentSDS }
     * 
     */
    public RCCTMT120101UK01AgentSDS createRCCTMT120101UK01AgentSDS() {
        return new RCCTMT120101UK01AgentSDS();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04PatientSubject }
     * 
     */
    public RCMRMT030101UK04PatientSubject createRCMRMT030101UK04PatientSubject() {
        return new RCMRMT030101UK04PatientSubject();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Patient }
     * 
     */
    public RCMRMT030101UK04Patient createRCMRMT030101UK04Patient() {
        return new RCMRMT030101UK04Patient();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Author3 }
     * 
     */
    public RCMRMT030101UK04Author3 createRCMRMT030101UK04Author3() {
        return new RCMRMT030101UK04Author3();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Destination }
     * 
     */
    public RCMRMT030101UK04Destination createRCMRMT030101UK04Destination() {
        return new RCMRMT030101UK04Destination();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Component }
     * 
     */
    public RCMRMT030101UK04Component createRCMRMT030101UK04Component() {
        return new RCMRMT030101UK04Component();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04EhrFolder }
     * 
     */
    public RCMRMT030101UK04EhrFolder createRCMRMT030101UK04EhrFolder() {
        return new RCMRMT030101UK04EhrFolder();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Author2 }
     * 
     */
    public RCMRMT030101UK04Author2 createRCMRMT030101UK04Author2() {
        return new RCMRMT030101UK04Author2();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ResponsibleParty }
     * 
     */
    public RCMRMT030101UK04ResponsibleParty createRCMRMT030101UK04ResponsibleParty() {
        return new RCMRMT030101UK04ResponsibleParty();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04AgentDirectory }
     * 
     */
    public RCMRMT030101UK04AgentDirectory createRCMRMT030101UK04AgentDirectory() {
        return new RCMRMT030101UK04AgentDirectory();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Part }
     * 
     */
    public RCMRMT030101UK04Part createRCMRMT030101UK04Part() {
        return new RCMRMT030101UK04Part();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Component3 }
     * 
     */
    public RCMRMT030101UK04Component3 createRCMRMT030101UK04Component3() {
        return new RCMRMT030101UK04Component3();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04EhrComposition }
     * 
     */
    public RCMRMT030101UK04EhrComposition createRCMRMT030101UK04EhrComposition() {
        return new RCMRMT030101UK04EhrComposition();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Author }
     * 
     */
    public RCMRMT030101UK04Author createRCMRMT030101UK04Author() {
        return new RCMRMT030101UK04Author();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04AgentRef }
     * 
     */
    public RCMRMT030101UK04AgentRef createRCMRMT030101UK04AgentRef() {
        return new RCMRMT030101UK04AgentRef();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Location }
     * 
     */
    public RCMRMT030101UK04Location createRCMRMT030101UK04Location() {
        return new RCMRMT030101UK04Location();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04LocatedEntity }
     * 
     */
    public RCMRMT030101UK04LocatedEntity createRCMRMT030101UK04LocatedEntity() {
        return new RCMRMT030101UK04LocatedEntity();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Place }
     * 
     */
    public RCMRMT030101UK04Place createRCMRMT030101UK04Place() {
        return new RCMRMT030101UK04Place();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Participant2 }
     * 
     */
    public RCMRMT030101UK04Participant2 createRCMRMT030101UK04Participant2() {
        return new RCMRMT030101UK04Participant2();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Component4 }
     * 
     */
    public RCMRMT030101UK04Component4 createRCMRMT030101UK04Component4() {
        return new RCMRMT030101UK04Component4();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Informant }
     * 
     */
    public RCMRMT030101UK04Informant createRCMRMT030101UK04Informant() {
        return new RCMRMT030101UK04Informant();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04InformantRole }
     * 
     */
    public RCMRMT030101UK04InformantRole createRCMRMT030101UK04InformantRole() {
        return new RCMRMT030101UK04InformantRole();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Person }
     * 
     */
    public RCMRMT030101UK04Person createRCMRMT030101UK04Person() {
        return new RCMRMT030101UK04Person();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Participant }
     * 
     */
    public RCMRMT030101UK04Participant createRCMRMT030101UK04Participant() {
        return new RCMRMT030101UK04Participant();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ReplacementOf }
     * 
     */
    public RCMRMT030101UK04ReplacementOf createRCMRMT030101UK04ReplacementOf() {
        return new RCMRMT030101UK04ReplacementOf();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04StatementRef }
     * 
     */
    public RCMRMT030101UK04StatementRef createRCMRMT030101UK04StatementRef() {
        return new RCMRMT030101UK04StatementRef();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Reason }
     * 
     */
    public RCMRMT030101UK04Reason createRCMRMT030101UK04Reason() {
        return new RCMRMT030101UK04Reason();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Reference }
     * 
     */
    public RCMRMT030101UK04Reference createRCMRMT030101UK04Reference() {
        return new RCMRMT030101UK04Reference();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ExternalDocument }
     * 
     */
    public RCMRMT030101UK04ExternalDocument createRCMRMT030101UK04ExternalDocument() {
        return new RCMRMT030101UK04ExternalDocument();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Author4 }
     * 
     */
    public RCMRMT030101UK04Author4 createRCMRMT030101UK04Author4() {
        return new RCMRMT030101UK04Author4();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04SequelTo }
     * 
     */
    public RCMRMT030101UK04SequelTo createRCMRMT030101UK04SequelTo() {
        return new RCMRMT030101UK04SequelTo();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04CompoundStatement }
     * 
     */
    public RCMRMT030101UK04CompoundStatement createRCMRMT030101UK04CompoundStatement() {
        return new RCMRMT030101UK04CompoundStatement();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Specimen03 }
     * 
     */
    public RCMRMT030101UK04Specimen03 createRCMRMT030101UK04Specimen03() {
        return new RCMRMT030101UK04Specimen03();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04SpecimenRole }
     * 
     */
    public RCMRMT030101UK04SpecimenRole createRCMRMT030101UK04SpecimenRole() {
        return new RCMRMT030101UK04SpecimenRole();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04SpecimenMaterial }
     * 
     */
    public RCMRMT030101UK04SpecimenMaterial createRCMRMT030101UK04SpecimenMaterial() {
        return new RCMRMT030101UK04SpecimenMaterial();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Component02 }
     * 
     */
    public RCMRMT030101UK04Component02 createRCMRMT030101UK04Component02() {
        return new RCMRMT030101UK04Component02();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04EhrEmpty }
     * 
     */
    public RCMRMT030101UK04EhrEmpty createRCMRMT030101UK04EhrEmpty() {
        return new RCMRMT030101UK04EhrEmpty();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04LinkSet }
     * 
     */
    public RCMRMT030101UK04LinkSet createRCMRMT030101UK04LinkSet() {
        return new RCMRMT030101UK04LinkSet();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Component6 }
     * 
     */
    public RCMRMT030101UK04Component6 createRCMRMT030101UK04Component6() {
        return new RCMRMT030101UK04Component6();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ConditionNamed }
     * 
     */
    public RCMRMT030101UK04ConditionNamed createRCMRMT030101UK04ConditionNamed() {
        return new RCMRMT030101UK04ConditionNamed();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04MedicationStatement }
     * 
     */
    public RCMRMT030101UK04MedicationStatement createRCMRMT030101UK04MedicationStatement() {
        return new RCMRMT030101UK04MedicationStatement();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Consumable }
     * 
     */
    public RCMRMT030101UK04Consumable createRCMRMT030101UK04Consumable() {
        return new RCMRMT030101UK04Consumable();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ManufacturedProduct }
     * 
     */
    public RCMRMT030101UK04ManufacturedProduct createRCMRMT030101UK04ManufacturedProduct() {
        return new RCMRMT030101UK04ManufacturedProduct();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Material }
     * 
     */
    public RCMRMT030101UK04Material createRCMRMT030101UK04Material() {
        return new RCMRMT030101UK04Material();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Component2 }
     * 
     */
    public RCMRMT030101UK04Component2 createRCMRMT030101UK04Component2() {
        return new RCMRMT030101UK04Component2();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Performer }
     * 
     */
    public RCMRMT030101UK04Performer createRCMRMT030101UK04Performer() {
        return new RCMRMT030101UK04Performer();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Product }
     * 
     */
    public RCMRMT030101UK04Product createRCMRMT030101UK04Product() {
        return new RCMRMT030101UK04Product();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04PertinentInformation2 }
     * 
     */
    public RCMRMT030101UK04PertinentInformation2 createRCMRMT030101UK04PertinentInformation2() {
        return new RCMRMT030101UK04PertinentInformation2();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04SupplyAnnotation }
     * 
     */
    public RCMRMT030101UK04SupplyAnnotation createRCMRMT030101UK04SupplyAnnotation() {
        return new RCMRMT030101UK04SupplyAnnotation();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Authorise }
     * 
     */
    public RCMRMT030101UK04Authorise createRCMRMT030101UK04Authorise() {
        return new RCMRMT030101UK04Authorise();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Predecessor }
     * 
     */
    public RCMRMT030101UK04Predecessor createRCMRMT030101UK04Predecessor() {
        return new RCMRMT030101UK04Predecessor();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04MedicationRef }
     * 
     */
    public RCMRMT030101UK04MedicationRef createRCMRMT030101UK04MedicationRef() {
        return new RCMRMT030101UK04MedicationRef();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Discontinue }
     * 
     */
    public RCMRMT030101UK04Discontinue createRCMRMT030101UK04Discontinue() {
        return new RCMRMT030101UK04Discontinue();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ReversalOf }
     * 
     */
    public RCMRMT030101UK04ReversalOf createRCMRMT030101UK04ReversalOf() {
        return new RCMRMT030101UK04ReversalOf();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Dispense }
     * 
     */
    public RCMRMT030101UK04Dispense createRCMRMT030101UK04Dispense() {
        return new RCMRMT030101UK04Dispense();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04InFulfillmentOf }
     * 
     */
    public RCMRMT030101UK04InFulfillmentOf createRCMRMT030101UK04InFulfillmentOf() {
        return new RCMRMT030101UK04InFulfillmentOf();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Prescribe }
     * 
     */
    public RCMRMT030101UK04Prescribe createRCMRMT030101UK04Prescribe() {
        return new RCMRMT030101UK04Prescribe();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04InFulfillmentOf02 }
     * 
     */
    public RCMRMT030101UK04InFulfillmentOf02 createRCMRMT030101UK04InFulfillmentOf02() {
        return new RCMRMT030101UK04InFulfillmentOf02();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04PertinentInformation }
     * 
     */
    public RCMRMT030101UK04PertinentInformation createRCMRMT030101UK04PertinentInformation() {
        return new RCMRMT030101UK04PertinentInformation();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04MedicationDosage }
     * 
     */
    public RCMRMT030101UK04MedicationDosage createRCMRMT030101UK04MedicationDosage() {
        return new RCMRMT030101UK04MedicationDosage();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04NarrativeStatement }
     * 
     */
    public RCMRMT030101UK04NarrativeStatement createRCMRMT030101UK04NarrativeStatement() {
        return new RCMRMT030101UK04NarrativeStatement();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ObservationStatement }
     * 
     */
    public RCMRMT030101UK04ObservationStatement createRCMRMT030101UK04ObservationStatement() {
        return new RCMRMT030101UK04ObservationStatement();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Subject }
     * 
     */
    public RCMRMT030101UK04Subject createRCMRMT030101UK04Subject() {
        return new RCMRMT030101UK04Subject();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04PersonalRelationship }
     * 
     */
    public RCMRMT030101UK04PersonalRelationship createRCMRMT030101UK04PersonalRelationship() {
        return new RCMRMT030101UK04PersonalRelationship();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Specimen }
     * 
     */
    public RCMRMT030101UK04Specimen createRCMRMT030101UK04Specimen() {
        return new RCMRMT030101UK04Specimen();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04PertinentInformation02 }
     * 
     */
    public RCMRMT030101UK04PertinentInformation02 createRCMRMT030101UK04PertinentInformation02() {
        return new RCMRMT030101UK04PertinentInformation02();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Annotation }
     * 
     */
    public RCMRMT030101UK04Annotation createRCMRMT030101UK04Annotation() {
        return new RCMRMT030101UK04Annotation();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ReferenceRange }
     * 
     */
    public RCMRMT030101UK04ReferenceRange createRCMRMT030101UK04ReferenceRange() {
        return new RCMRMT030101UK04ReferenceRange();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04InterpretationRange }
     * 
     */
    public RCMRMT030101UK04InterpretationRange createRCMRMT030101UK04InterpretationRange() {
        return new RCMRMT030101UK04InterpretationRange();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04PlanStatement }
     * 
     */
    public RCMRMT030101UK04PlanStatement createRCMRMT030101UK04PlanStatement() {
        return new RCMRMT030101UK04PlanStatement();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04RegistrationStatement }
     * 
     */
    public RCMRMT030101UK04RegistrationStatement createRCMRMT030101UK04RegistrationStatement() {
        return new RCMRMT030101UK04RegistrationStatement();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ResponsibleParty2 }
     * 
     */
    public RCMRMT030101UK04ResponsibleParty2 createRCMRMT030101UK04ResponsibleParty2() {
        return new RCMRMT030101UK04ResponsibleParty2();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04RequestStatement }
     * 
     */
    public RCMRMT030101UK04RequestStatement createRCMRMT030101UK04RequestStatement() {
        return new RCMRMT030101UK04RequestStatement();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ResponsibleParty3 }
     * 
     */
    public RCMRMT030101UK04ResponsibleParty3 createRCMRMT030101UK04ResponsibleParty3() {
        return new RCMRMT030101UK04ResponsibleParty3();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ReplacementOf2 }
     * 
     */
    public RCMRMT030101UK04ReplacementOf2 createRCMRMT030101UK04ReplacementOf2() {
        return new RCMRMT030101UK04ReplacementOf2();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04CompositionRef }
     * 
     */
    public RCMRMT030101UK04CompositionRef createRCMRMT030101UK04CompositionRef() {
        return new RCMRMT030101UK04CompositionRef();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04InFulfillmentOf2 }
     * 
     */
    public RCMRMT030101UK04InFulfillmentOf2 createRCMRMT030101UK04InFulfillmentOf2() {
        return new RCMRMT030101UK04InFulfillmentOf2();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04EhrRequest }
     * 
     */
    public RCMRMT030101UK04EhrRequest createRCMRMT030101UK04EhrRequest() {
        return new RCMRMT030101UK04EhrRequest();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Limitation }
     * 
     */
    public RCMRMT030101UK04Limitation createRCMRMT030101UK04Limitation() {
        return new RCMRMT030101UK04Limitation();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04EhrExtractSpecification }
     * 
     */
    public RCMRMT030101UK04EhrExtractSpecification createRCMRMT030101UK04EhrExtractSpecification() {
        return new RCMRMT030101UK04EhrExtractSpecification();
    }

    /**
     * Create an instance of {@link MCCIMT010101UK12CommunicationFunctionRcv }
     * 
     */
    public MCCIMT010101UK12CommunicationFunctionRcv createMCCIMT010101UK12CommunicationFunctionRcv() {
        return new MCCIMT010101UK12CommunicationFunctionRcv();
    }

    /**
     * Create an instance of {@link MCCIMT010101UK12Device }
     * 
     */
    public MCCIMT010101UK12Device createMCCIMT010101UK12Device() {
        return new MCCIMT010101UK12Device();
    }

    /**
     * Create an instance of {@link MCCIMT010101UK12Agent }
     * 
     */
    public MCCIMT010101UK12Agent createMCCIMT010101UK12Agent() {
        return new MCCIMT010101UK12Agent();
    }

    /**
     * Create an instance of {@link MCCIMT010101UK12Organization }
     * 
     */
    public MCCIMT010101UK12Organization createMCCIMT010101UK12Organization() {
        return new MCCIMT010101UK12Organization();
    }

    /**
     * Create an instance of {@link MCCIMT010101UK12CommunicationFunctionSnd }
     * 
     */
    public MCCIMT010101UK12CommunicationFunctionSnd createMCCIMT010101UK12CommunicationFunctionSnd() {
        return new MCCIMT010101UK12CommunicationFunctionSnd();
    }

    /**
     * Create an instance of {@link MCCIMT010101UK12ControlActEvent }
     * 
     */
    public MCCIMT010101UK12ControlActEvent createMCCIMT010101UK12ControlActEvent() {
        return new MCCIMT010101UK12ControlActEvent();
    }

    /**
     * Create an instance of {@link RCMRIN030000UK06ControlActEvent }
     * 
     */
    public RCMRIN030000UK06ControlActEvent createRCMRIN030000UK06ControlActEvent() {
        return new RCMRIN030000UK06ControlActEvent();
    }

    /**
     * Create an instance of {@link RCMRIN030000UK06Subject }
     * 
     */
    public RCMRIN030000UK06Subject createRCMRIN030000UK06Subject() {
        return new RCMRIN030000UK06Subject();
    }

    /**
     * Create an instance of {@link RTOQTYQTY.Numerator }
     * 
     */
    public RTOQTYQTY.Numerator createRTOQTYQTYNumerator() {
        return new RTOQTYQTY.Numerator();
    }

    /**
     * Create an instance of {@link RTOQTYQTY.Denominator }
     * 
     */
    public RTOQTYQTY.Denominator createRTOQTYQTYDenominator() {
        return new RTOQTYQTY.Denominator();
    }

    /**
     * Create an instance of {@link IVLINT.Low }
     * 
     */
    public IVLINT.Low createIVLINTLow() {
        return new IVLINT.Low();
    }

    /**
     * Create an instance of {@link IVLINT.High }
     * 
     */
    public IVLINT.High createIVLINTHigh() {
        return new IVLINT.High();
    }

    /**
     * Create an instance of {@link CD.Group }
     * 
     */
    public CD.Group createCDGroup() {
        return new CD.Group();
    }

    /**
     * Create an instance of {@link AD.Delimiter }
     * 
     */
    public AD.Delimiter createADDelimiter() {
        return new AD.Delimiter();
    }

    /**
     * Create an instance of {@link AD.Country }
     * 
     */
    public AD.Country createADCountry() {
        return new AD.Country();
    }

    /**
     * Create an instance of {@link AD.State }
     * 
     */
    public AD.State createADState() {
        return new AD.State();
    }

    /**
     * Create an instance of {@link AD.County }
     * 
     */
    public AD.County createADCounty() {
        return new AD.County();
    }

    /**
     * Create an instance of {@link AD.City }
     * 
     */
    public AD.City createADCity() {
        return new AD.City();
    }

    /**
     * Create an instance of {@link AD.PostalCode }
     * 
     */
    public AD.PostalCode createADPostalCode() {
        return new AD.PostalCode();
    }

    /**
     * Create an instance of {@link AD.StreetAddressLine }
     * 
     */
    public AD.StreetAddressLine createADStreetAddressLine() {
        return new AD.StreetAddressLine();
    }

    /**
     * Create an instance of {@link AD.HouseNumber }
     * 
     */
    public AD.HouseNumber createADHouseNumber() {
        return new AD.HouseNumber();
    }

    /**
     * Create an instance of {@link AD.HouseNumberNumeric }
     * 
     */
    public AD.HouseNumberNumeric createADHouseNumberNumeric() {
        return new AD.HouseNumberNumeric();
    }

    /**
     * Create an instance of {@link AD.Direction }
     * 
     */
    public AD.Direction createADDirection() {
        return new AD.Direction();
    }

    /**
     * Create an instance of {@link AD.StreetName }
     * 
     */
    public AD.StreetName createADStreetName() {
        return new AD.StreetName();
    }

    /**
     * Create an instance of {@link AD.StreetNameBase }
     * 
     */
    public AD.StreetNameBase createADStreetNameBase() {
        return new AD.StreetNameBase();
    }

    /**
     * Create an instance of {@link AD.StreetNameType }
     * 
     */
    public AD.StreetNameType createADStreetNameType() {
        return new AD.StreetNameType();
    }

    /**
     * Create an instance of {@link AD.AdditionalLocator }
     * 
     */
    public AD.AdditionalLocator createADAdditionalLocator() {
        return new AD.AdditionalLocator();
    }

    /**
     * Create an instance of {@link AD.UnitID }
     * 
     */
    public AD.UnitID createADUnitID() {
        return new AD.UnitID();
    }

    /**
     * Create an instance of {@link AD.UnitType }
     * 
     */
    public AD.UnitType createADUnitType() {
        return new AD.UnitType();
    }

    /**
     * Create an instance of {@link AD.Carrier }
     * 
     */
    public AD.Carrier createADCarrier() {
        return new AD.Carrier();
    }

    /**
     * Create an instance of {@link AD.CensusTract }
     * 
     */
    public AD.CensusTract createADCensusTract() {
        return new AD.CensusTract();
    }

    /**
     * Create an instance of {@link AD.AddressKey }
     * 
     */
    public AD.AddressKey createADAddressKey() {
        return new AD.AddressKey();
    }

    /**
     * Create an instance of {@link AD.Desc }
     * 
     */
    public AD.Desc createADDesc() {
        return new AD.Desc();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MCAIMT040101UK03ControlActEvent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link MCAIMT040101UK03ControlActEvent }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "ControlActEvent")
    public JAXBElement<MCAIMT040101UK03ControlActEvent> createControlActEvent(MCAIMT040101UK03ControlActEvent value) {
        return new JAXBElement<MCAIMT040101UK03ControlActEvent>(_ControlActEvent_QNAME, MCAIMT040101UK03ControlActEvent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RCMRMT030101UK04EhrExtract }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link RCMRMT030101UK04EhrExtract }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "EhrExtract")
    public JAXBElement<RCMRMT030101UK04EhrExtract> createEhrExtract(RCMRMT030101UK04EhrExtract value) {
        return new JAXBElement<RCMRMT030101UK04EhrExtract>(_EhrExtract_QNAME, RCMRMT030101UK04EhrExtract.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MCCIMT010101UK12Message }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link MCCIMT010101UK12Message }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "Message")
    public JAXBElement<MCCIMT010101UK12Message> createMessage(MCCIMT010101UK12Message value) {
        return new JAXBElement<MCCIMT010101UK12Message>(_Message_QNAME, MCCIMT010101UK12Message.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RCMRIN030000UK06Message }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link RCMRIN030000UK06Message }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "RCMR_IN030000UK06")
    public JAXBElement<RCMRIN030000UK06Message> createRCMRIN030000UK06(RCMRIN030000UK06Message value) {
        return new JAXBElement<RCMRIN030000UK06Message>(_RCMRIN030000UK06_QNAME, RCMRIN030000UK06Message.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IVLINT.Low }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IVLINT.Low }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "low", scope = IVLINT.class)
    public JAXBElement<IVLINT.Low> createIVLINTLow(IVLINT.Low value) {
        return new JAXBElement<IVLINT.Low>(_IVLINTLow_QNAME, IVLINT.Low.class, IVLINT.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IVLINT.High }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IVLINT.High }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "high", scope = IVLINT.class)
    public JAXBElement<IVLINT.High> createIVLINTHigh(IVLINT.High value) {
        return new JAXBElement<IVLINT.High>(_IVLINTHigh_QNAME, IVLINT.High.class, IVLINT.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PQInc }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PQInc }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "low", scope = IVLPQ.class)
    public JAXBElement<PQInc> createIVLPQLow(PQInc value) {
        return new JAXBElement<PQInc>(_IVLINTLow_QNAME, PQInc.class, IVLPQ.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PQ }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PQ }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "width", scope = IVLPQ.class)
    public JAXBElement<PQ> createIVLPQWidth(PQ value) {
        return new JAXBElement<PQ>(_IVLPQWidth_QNAME, PQ.class, IVLPQ.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PQInc }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PQInc }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "high", scope = IVLPQ.class)
    public JAXBElement<PQInc> createIVLPQHigh(PQInc value) {
        return new JAXBElement<PQInc>(_IVLINTHigh_QNAME, PQInc.class, IVLPQ.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PQ }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PQ }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "center", scope = IVLPQ.class)
    public JAXBElement<PQ> createIVLPQCenter(PQ value) {
        return new JAXBElement<PQ>(_IVLPQCenter_QNAME, PQ.class, IVLPQ.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IVXBTS }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IVXBTS }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "low", scope = IVLTS.class)
    public JAXBElement<IVXBTS> createIVLTSLow(IVXBTS value) {
        return new JAXBElement<IVXBTS>(_IVLINTLow_QNAME, IVXBTS.class, IVLTS.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PQ }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PQ }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "width", scope = IVLTS.class)
    public JAXBElement<PQ> createIVLTSWidth(PQ value) {
        return new JAXBElement<PQ>(_IVLPQWidth_QNAME, PQ.class, IVLTS.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IVXBTS }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IVXBTS }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "high", scope = IVLTS.class)
    public JAXBElement<IVXBTS> createIVLTSHigh(IVXBTS value) {
        return new JAXBElement<IVXBTS>(_IVLINTHigh_QNAME, IVXBTS.class, IVLTS.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TS }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link TS }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "center", scope = IVLTS.class)
    public JAXBElement<TS> createIVLTSCenter(TS value) {
        return new JAXBElement<TS>(_IVLPQCenter_QNAME, TS.class, IVLTS.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnDelimiter }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EnDelimiter }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "delimiter", scope = EN.class)
    public JAXBElement<EnDelimiter> createENDelimiter(EnDelimiter value) {
        return new JAXBElement<EnDelimiter>(_ENDelimiter_QNAME, EnDelimiter.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnFamily }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EnFamily }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "family", scope = EN.class)
    public JAXBElement<EnFamily> createENFamily(EnFamily value) {
        return new JAXBElement<EnFamily>(_ENFamily_QNAME, EnFamily.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnGiven }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EnGiven }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "given", scope = EN.class)
    public JAXBElement<EnGiven> createENGiven(EnGiven value) {
        return new JAXBElement<EnGiven>(_ENGiven_QNAME, EnGiven.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnPrefix }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EnPrefix }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "prefix", scope = EN.class)
    public JAXBElement<EnPrefix> createENPrefix(EnPrefix value) {
        return new JAXBElement<EnPrefix>(_ENPrefix_QNAME, EnPrefix.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnSuffix }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EnSuffix }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "suffix", scope = EN.class)
    public JAXBElement<EnSuffix> createENSuffix(EnSuffix value) {
        return new JAXBElement<EnSuffix>(_ENSuffix_QNAME, EnSuffix.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IVLTS }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IVLTS }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "validTime", scope = EN.class)
    public JAXBElement<IVLTS> createENValidTime(IVLTS value) {
        return new JAXBElement<IVLTS>(_ENValidTime_QNAME, IVLTS.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link II }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link II }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "id", scope = EN.class)
    public JAXBElement<II> createENId(II value) {
        return new JAXBElement<II>(_ENId_QNAME, II.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.Delimiter }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.Delimiter }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "delimiter", scope = AD.class)
    public JAXBElement<AD.Delimiter> createADDelimiter(AD.Delimiter value) {
        return new JAXBElement<AD.Delimiter>(_ENDelimiter_QNAME, AD.Delimiter.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.Country }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.Country }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "country", scope = AD.class)
    public JAXBElement<AD.Country> createADCountry(AD.Country value) {
        return new JAXBElement<AD.Country>(_ADCountry_QNAME, AD.Country.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.State }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.State }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "state", scope = AD.class)
    public JAXBElement<AD.State> createADState(AD.State value) {
        return new JAXBElement<AD.State>(_ADState_QNAME, AD.State.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.County }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.County }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "county", scope = AD.class)
    public JAXBElement<AD.County> createADCounty(AD.County value) {
        return new JAXBElement<AD.County>(_ADCounty_QNAME, AD.County.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.City }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.City }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "city", scope = AD.class)
    public JAXBElement<AD.City> createADCity(AD.City value) {
        return new JAXBElement<AD.City>(_ADCity_QNAME, AD.City.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.PostalCode }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.PostalCode }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "postalCode", scope = AD.class)
    public JAXBElement<AD.PostalCode> createADPostalCode(AD.PostalCode value) {
        return new JAXBElement<AD.PostalCode>(_ADPostalCode_QNAME, AD.PostalCode.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.StreetAddressLine }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.StreetAddressLine }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "streetAddressLine", scope = AD.class)
    public JAXBElement<AD.StreetAddressLine> createADStreetAddressLine(AD.StreetAddressLine value) {
        return new JAXBElement<AD.StreetAddressLine>(_ADStreetAddressLine_QNAME, AD.StreetAddressLine.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.HouseNumber }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.HouseNumber }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "houseNumber", scope = AD.class)
    public JAXBElement<AD.HouseNumber> createADHouseNumber(AD.HouseNumber value) {
        return new JAXBElement<AD.HouseNumber>(_ADHouseNumber_QNAME, AD.HouseNumber.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.HouseNumberNumeric }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.HouseNumberNumeric }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "houseNumberNumeric", scope = AD.class)
    public JAXBElement<AD.HouseNumberNumeric> createADHouseNumberNumeric(AD.HouseNumberNumeric value) {
        return new JAXBElement<AD.HouseNumberNumeric>(_ADHouseNumberNumeric_QNAME, AD.HouseNumberNumeric.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.Direction }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.Direction }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "direction", scope = AD.class)
    public JAXBElement<AD.Direction> createADDirection(AD.Direction value) {
        return new JAXBElement<AD.Direction>(_ADDirection_QNAME, AD.Direction.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.StreetName }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.StreetName }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "streetName", scope = AD.class)
    public JAXBElement<AD.StreetName> createADStreetName(AD.StreetName value) {
        return new JAXBElement<AD.StreetName>(_ADStreetName_QNAME, AD.StreetName.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.StreetNameBase }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.StreetNameBase }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "streetNameBase", scope = AD.class)
    public JAXBElement<AD.StreetNameBase> createADStreetNameBase(AD.StreetNameBase value) {
        return new JAXBElement<AD.StreetNameBase>(_ADStreetNameBase_QNAME, AD.StreetNameBase.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.StreetNameType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.StreetNameType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "streetNameType", scope = AD.class)
    public JAXBElement<AD.StreetNameType> createADStreetNameType(AD.StreetNameType value) {
        return new JAXBElement<AD.StreetNameType>(_ADStreetNameType_QNAME, AD.StreetNameType.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.AdditionalLocator }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.AdditionalLocator }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "additionalLocator", scope = AD.class)
    public JAXBElement<AD.AdditionalLocator> createADAdditionalLocator(AD.AdditionalLocator value) {
        return new JAXBElement<AD.AdditionalLocator>(_ADAdditionalLocator_QNAME, AD.AdditionalLocator.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.UnitID }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.UnitID }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "unitID", scope = AD.class)
    public JAXBElement<AD.UnitID> createADUnitID(AD.UnitID value) {
        return new JAXBElement<AD.UnitID>(_ADUnitID_QNAME, AD.UnitID.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.UnitType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.UnitType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "unitType", scope = AD.class)
    public JAXBElement<AD.UnitType> createADUnitType(AD.UnitType value) {
        return new JAXBElement<AD.UnitType>(_ADUnitType_QNAME, AD.UnitType.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.Carrier }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.Carrier }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "carrier", scope = AD.class)
    public JAXBElement<AD.Carrier> createADCarrier(AD.Carrier value) {
        return new JAXBElement<AD.Carrier>(_ADCarrier_QNAME, AD.Carrier.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.CensusTract }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.CensusTract }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "censusTract", scope = AD.class)
    public JAXBElement<AD.CensusTract> createADCensusTract(AD.CensusTract value) {
        return new JAXBElement<AD.CensusTract>(_ADCensusTract_QNAME, AD.CensusTract.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.AddressKey }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.AddressKey }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "addressKey", scope = AD.class)
    public JAXBElement<AD.AddressKey> createADAddressKey(AD.AddressKey value) {
        return new JAXBElement<AD.AddressKey>(_ADAddressKey_QNAME, AD.AddressKey.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.Desc }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.Desc }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "desc", scope = AD.class)
    public JAXBElement<AD.Desc> createADDesc(AD.Desc value) {
        return new JAXBElement<AD.Desc>(_ADDesc_QNAME, AD.Desc.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IVLTS }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IVLTS }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "useablePeriod", scope = AD.class)
    public JAXBElement<IVLTS> createADUseablePeriod(IVLTS value) {
        return new JAXBElement<IVLTS>(_ADUseablePeriod_QNAME, IVLTS.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link II }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link II }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "id", scope = AD.class)
    public JAXBElement<II> createADId(II value) {
        return new JAXBElement<II>(_ENId_QNAME, II.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "content", scope = StrucDocTh.class)
    public JAXBElement<StrucDocContent> createStrucDocThContent(StrucDocContent value) {
        return new JAXBElement<StrucDocContent>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "linkHtml", scope = StrucDocTh.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocThLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<StrucDocLinkHtml>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sub", scope = StrucDocTh.class)
    public JAXBElement<StrucDocSub> createStrucDocThSub(StrucDocSub value) {
        return new JAXBElement<StrucDocSub>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sup", scope = StrucDocTh.class)
    public JAXBElement<StrucDocSup> createStrucDocThSup(StrucDocSup value) {
        return new JAXBElement<StrucDocSup>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "br", scope = StrucDocTh.class)
    public JAXBElement<StrucDocBr> createStrucDocThBr(StrucDocBr value) {
        return new JAXBElement<StrucDocBr>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnote", scope = StrucDocTh.class)
    public JAXBElement<StrucDocFootnote> createStrucDocThFootnote(StrucDocFootnote value) {
        return new JAXBElement<StrucDocFootnote>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnoteRef", scope = StrucDocTh.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocThFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<StrucDocFootnoteRef>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "renderMultiMedia", scope = StrucDocTh.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocThRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<StrucDocRenderMultiMedia>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "content", scope = StrucDocTd.class)
    public JAXBElement<StrucDocContent> createStrucDocTdContent(StrucDocContent value) {
        return new JAXBElement<StrucDocContent>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "linkHtml", scope = StrucDocTd.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocTdLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<StrucDocLinkHtml>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sub", scope = StrucDocTd.class)
    public JAXBElement<StrucDocSub> createStrucDocTdSub(StrucDocSub value) {
        return new JAXBElement<StrucDocSub>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sup", scope = StrucDocTd.class)
    public JAXBElement<StrucDocSup> createStrucDocTdSup(StrucDocSup value) {
        return new JAXBElement<StrucDocSup>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "br", scope = StrucDocTd.class)
    public JAXBElement<StrucDocBr> createStrucDocTdBr(StrucDocBr value) {
        return new JAXBElement<StrucDocBr>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnote", scope = StrucDocTd.class)
    public JAXBElement<StrucDocFootnote> createStrucDocTdFootnote(StrucDocFootnote value) {
        return new JAXBElement<StrucDocFootnote>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnoteRef", scope = StrucDocTd.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocTdFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<StrucDocFootnoteRef>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "renderMultiMedia", scope = StrucDocTd.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocTdRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<StrucDocRenderMultiMedia>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "paragraph", scope = StrucDocTd.class)
    public JAXBElement<StrucDocParagraph> createStrucDocTdParagraph(StrucDocParagraph value) {
        return new JAXBElement<StrucDocParagraph>(_StrucDocTdParagraph_QNAME, StrucDocParagraph.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "list", scope = StrucDocTd.class)
    public JAXBElement<StrucDocList> createStrucDocTdList(StrucDocList value) {
        return new JAXBElement<StrucDocList>(_StrucDocTdList_QNAME, StrucDocList.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocCaption }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocCaption }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "caption", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocCaption> createStrucDocParagraphCaption(StrucDocCaption value) {
        return new JAXBElement<StrucDocCaption>(_StrucDocParagraphCaption_QNAME, StrucDocCaption.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "content", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocContent> createStrucDocParagraphContent(StrucDocContent value) {
        return new JAXBElement<StrucDocContent>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "linkHtml", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocParagraphLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<StrucDocLinkHtml>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sub", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocSub> createStrucDocParagraphSub(StrucDocSub value) {
        return new JAXBElement<StrucDocSub>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sup", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocSup> createStrucDocParagraphSup(StrucDocSup value) {
        return new JAXBElement<StrucDocSup>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "br", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocBr> createStrucDocParagraphBr(StrucDocBr value) {
        return new JAXBElement<StrucDocBr>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnote", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocFootnote> createStrucDocParagraphFootnote(StrucDocFootnote value) {
        return new JAXBElement<StrucDocFootnote>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnoteRef", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocParagraphFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<StrucDocFootnoteRef>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "renderMultiMedia", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocParagraphRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<StrucDocRenderMultiMedia>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnote", scope = StrucDocLinkHtml.class)
    public JAXBElement<StrucDocFootnote> createStrucDocLinkHtmlFootnote(StrucDocFootnote value) {
        return new JAXBElement<StrucDocFootnote>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocLinkHtml.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnoteRef", scope = StrucDocLinkHtml.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocLinkHtmlFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<StrucDocFootnoteRef>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocLinkHtml.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocCaption }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocCaption }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "caption", scope = StrucDocItem.class)
    public JAXBElement<StrucDocCaption> createStrucDocItemCaption(StrucDocCaption value) {
        return new JAXBElement<StrucDocCaption>(_StrucDocParagraphCaption_QNAME, StrucDocCaption.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "content", scope = StrucDocItem.class)
    public JAXBElement<StrucDocContent> createStrucDocItemContent(StrucDocContent value) {
        return new JAXBElement<StrucDocContent>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "linkHtml", scope = StrucDocItem.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocItemLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<StrucDocLinkHtml>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sub", scope = StrucDocItem.class)
    public JAXBElement<StrucDocSub> createStrucDocItemSub(StrucDocSub value) {
        return new JAXBElement<StrucDocSub>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sup", scope = StrucDocItem.class)
    public JAXBElement<StrucDocSup> createStrucDocItemSup(StrucDocSup value) {
        return new JAXBElement<StrucDocSup>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "br", scope = StrucDocItem.class)
    public JAXBElement<StrucDocBr> createStrucDocItemBr(StrucDocBr value) {
        return new JAXBElement<StrucDocBr>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnote", scope = StrucDocItem.class)
    public JAXBElement<StrucDocFootnote> createStrucDocItemFootnote(StrucDocFootnote value) {
        return new JAXBElement<StrucDocFootnote>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnoteRef", scope = StrucDocItem.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocItemFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<StrucDocFootnoteRef>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "renderMultiMedia", scope = StrucDocItem.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocItemRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<StrucDocRenderMultiMedia>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "paragraph", scope = StrucDocItem.class)
    public JAXBElement<StrucDocParagraph> createStrucDocItemParagraph(StrucDocParagraph value) {
        return new JAXBElement<StrucDocParagraph>(_StrucDocTdParagraph_QNAME, StrucDocParagraph.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "list", scope = StrucDocItem.class)
    public JAXBElement<StrucDocList> createStrucDocItemList(StrucDocList value) {
        return new JAXBElement<StrucDocList>(_StrucDocTdList_QNAME, StrucDocList.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTable }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTable }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "table", scope = StrucDocItem.class)
    public JAXBElement<StrucDocTable> createStrucDocItemTable(StrucDocTable value) {
        return new JAXBElement<StrucDocTable>(_StrucDocItemTable_QNAME, StrucDocTable.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTitleContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTitleContent }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "content", scope = StrucDocTitleFootnote.class)
    public JAXBElement<StrucDocTitleContent> createStrucDocTitleFootnoteContent(StrucDocTitleContent value) {
        return new JAXBElement<StrucDocTitleContent>(_StrucDocThContent_QNAME, StrucDocTitleContent.class, StrucDocTitleFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sub", scope = StrucDocTitleFootnote.class)
    public JAXBElement<StrucDocSub> createStrucDocTitleFootnoteSub(StrucDocSub value) {
        return new JAXBElement<StrucDocSub>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocTitleFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sup", scope = StrucDocTitleFootnote.class)
    public JAXBElement<StrucDocSup> createStrucDocTitleFootnoteSup(StrucDocSup value) {
        return new JAXBElement<StrucDocSup>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocTitleFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "br", scope = StrucDocTitleFootnote.class)
    public JAXBElement<StrucDocBr> createStrucDocTitleFootnoteBr(StrucDocBr value) {
        return new JAXBElement<StrucDocBr>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocTitleFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "content", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocContent> createStrucDocFootnoteContent(StrucDocContent value) {
        return new JAXBElement<StrucDocContent>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "linkHtml", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocFootnoteLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<StrucDocLinkHtml>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sub", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocSub> createStrucDocFootnoteSub(StrucDocSub value) {
        return new JAXBElement<StrucDocSub>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sup", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocSup> createStrucDocFootnoteSup(StrucDocSup value) {
        return new JAXBElement<StrucDocSup>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "br", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocBr> createStrucDocFootnoteBr(StrucDocBr value) {
        return new JAXBElement<StrucDocBr>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "renderMultiMedia", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocFootnoteRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<StrucDocRenderMultiMedia>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "paragraph", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocParagraph> createStrucDocFootnoteParagraph(StrucDocParagraph value) {
        return new JAXBElement<StrucDocParagraph>(_StrucDocTdParagraph_QNAME, StrucDocParagraph.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "list", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocList> createStrucDocFootnoteList(StrucDocList value) {
        return new JAXBElement<StrucDocList>(_StrucDocTdList_QNAME, StrucDocList.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTable }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTable }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "table", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocTable> createStrucDocFootnoteTable(StrucDocTable value) {
        return new JAXBElement<StrucDocTable>(_StrucDocItemTable_QNAME, StrucDocTable.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTitleContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTitleContent }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "content", scope = StrucDocTitleContent.class)
    public JAXBElement<StrucDocTitleContent> createStrucDocTitleContentContent(StrucDocTitleContent value) {
        return new JAXBElement<StrucDocTitleContent>(_StrucDocThContent_QNAME, StrucDocTitleContent.class, StrucDocTitleContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sub", scope = StrucDocTitleContent.class)
    public JAXBElement<StrucDocSub> createStrucDocTitleContentSub(StrucDocSub value) {
        return new JAXBElement<StrucDocSub>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocTitleContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sup", scope = StrucDocTitleContent.class)
    public JAXBElement<StrucDocSup> createStrucDocTitleContentSup(StrucDocSup value) {
        return new JAXBElement<StrucDocSup>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocTitleContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "br", scope = StrucDocTitleContent.class)
    public JAXBElement<StrucDocBr> createStrucDocTitleContentBr(StrucDocBr value) {
        return new JAXBElement<StrucDocBr>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocTitleContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTitleFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTitleFootnote }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnote", scope = StrucDocTitleContent.class)
    public JAXBElement<StrucDocTitleFootnote> createStrucDocTitleContentFootnote(StrucDocTitleFootnote value) {
        return new JAXBElement<StrucDocTitleFootnote>(_StrucDocThFootnote_QNAME, StrucDocTitleFootnote.class, StrucDocTitleContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnoteRef", scope = StrucDocTitleContent.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocTitleContentFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<StrucDocFootnoteRef>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocTitleContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "content", scope = StrucDocContent.class)
    public JAXBElement<StrucDocContent> createStrucDocContentContent(StrucDocContent value) {
        return new JAXBElement<StrucDocContent>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "linkHtml", scope = StrucDocContent.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocContentLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<StrucDocLinkHtml>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sub", scope = StrucDocContent.class)
    public JAXBElement<StrucDocSub> createStrucDocContentSub(StrucDocSub value) {
        return new JAXBElement<StrucDocSub>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sup", scope = StrucDocContent.class)
    public JAXBElement<StrucDocSup> createStrucDocContentSup(StrucDocSup value) {
        return new JAXBElement<StrucDocSup>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "br", scope = StrucDocContent.class)
    public JAXBElement<StrucDocBr> createStrucDocContentBr(StrucDocBr value) {
        return new JAXBElement<StrucDocBr>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnote", scope = StrucDocContent.class)
    public JAXBElement<StrucDocFootnote> createStrucDocContentFootnote(StrucDocFootnote value) {
        return new JAXBElement<StrucDocFootnote>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnoteRef", scope = StrucDocContent.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocContentFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<StrucDocFootnoteRef>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "renderMultiMedia", scope = StrucDocContent.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocContentRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<StrucDocRenderMultiMedia>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "linkHtml", scope = StrucDocCaption.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocCaptionLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<StrucDocLinkHtml>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocCaption.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sub", scope = StrucDocCaption.class)
    public JAXBElement<StrucDocSub> createStrucDocCaptionSub(StrucDocSub value) {
        return new JAXBElement<StrucDocSub>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocCaption.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sup", scope = StrucDocCaption.class)
    public JAXBElement<StrucDocSup> createStrucDocCaptionSup(StrucDocSup value) {
        return new JAXBElement<StrucDocSup>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocCaption.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnote", scope = StrucDocCaption.class)
    public JAXBElement<StrucDocFootnote> createStrucDocCaptionFootnote(StrucDocFootnote value) {
        return new JAXBElement<StrucDocFootnote>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocCaption.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnoteRef", scope = StrucDocCaption.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocCaptionFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<StrucDocFootnoteRef>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocCaption.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTitleContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTitleContent }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "content", scope = StrucDocTitle.class)
    public JAXBElement<StrucDocTitleContent> createStrucDocTitleContent(StrucDocTitleContent value) {
        return new JAXBElement<StrucDocTitleContent>(_StrucDocThContent_QNAME, StrucDocTitleContent.class, StrucDocTitle.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sub", scope = StrucDocTitle.class)
    public JAXBElement<StrucDocSub> createStrucDocTitleSub(StrucDocSub value) {
        return new JAXBElement<StrucDocSub>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocTitle.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sup", scope = StrucDocTitle.class)
    public JAXBElement<StrucDocSup> createStrucDocTitleSup(StrucDocSup value) {
        return new JAXBElement<StrucDocSup>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocTitle.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "br", scope = StrucDocTitle.class)
    public JAXBElement<StrucDocBr> createStrucDocTitleBr(StrucDocBr value) {
        return new JAXBElement<StrucDocBr>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocTitle.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTitleFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTitleFootnote }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnote", scope = StrucDocTitle.class)
    public JAXBElement<StrucDocTitleFootnote> createStrucDocTitleFootnote(StrucDocTitleFootnote value) {
        return new JAXBElement<StrucDocTitleFootnote>(_StrucDocThFootnote_QNAME, StrucDocTitleFootnote.class, StrucDocTitle.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnoteRef", scope = StrucDocTitle.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocTitleFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<StrucDocFootnoteRef>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocTitle.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "content", scope = StrucDocText.class)
    public JAXBElement<StrucDocContent> createStrucDocTextContent(StrucDocContent value) {
        return new JAXBElement<StrucDocContent>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "linkHtml", scope = StrucDocText.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocTextLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<StrucDocLinkHtml>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sub", scope = StrucDocText.class)
    public JAXBElement<StrucDocSub> createStrucDocTextSub(StrucDocSub value) {
        return new JAXBElement<StrucDocSub>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "sup", scope = StrucDocText.class)
    public JAXBElement<StrucDocSup> createStrucDocTextSup(StrucDocSup value) {
        return new JAXBElement<StrucDocSup>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "br", scope = StrucDocText.class)
    public JAXBElement<StrucDocBr> createStrucDocTextBr(StrucDocBr value) {
        return new JAXBElement<StrucDocBr>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnote", scope = StrucDocText.class)
    public JAXBElement<StrucDocFootnote> createStrucDocTextFootnote(StrucDocFootnote value) {
        return new JAXBElement<StrucDocFootnote>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "footnoteRef", scope = StrucDocText.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocTextFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<StrucDocFootnoteRef>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "renderMultiMedia", scope = StrucDocText.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocTextRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<StrucDocRenderMultiMedia>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "paragraph", scope = StrucDocText.class)
    public JAXBElement<StrucDocParagraph> createStrucDocTextParagraph(StrucDocParagraph value) {
        return new JAXBElement<StrucDocParagraph>(_StrucDocTdParagraph_QNAME, StrucDocParagraph.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "list", scope = StrucDocText.class)
    public JAXBElement<StrucDocList> createStrucDocTextList(StrucDocList value) {
        return new JAXBElement<StrucDocList>(_StrucDocTdList_QNAME, StrucDocList.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTable }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTable }{@code >}
     */
    @XmlElementDecl(namespace = "urn:hl7-org:v3", name = "table", scope = StrucDocText.class)
    public JAXBElement<StrucDocTable> createStrucDocTextTable(StrucDocTable value) {
        return new JAXBElement<StrucDocTable>(_StrucDocItemTable_QNAME, StrucDocTable.class, StrucDocText.class, value);
    }


    /**
     * List of custom built JAXB elements that weren't pre-generated. These are being used in unit tests to test specific
     * deeply nested elements.
     *
     */
    @XmlElementDecl(namespace = "", name = "code")
    public JAXBElement<CD> createCode(CD value) {
        return new JAXBElement<CD>(_CD_QNAME, CD.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "value")
    public JAXBElement<Value> createValue(Value value) {
        return new JAXBElement<Value>(_VALUE_QNAME, Value.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "ehrComposition")
    public JAXBElement<RCMRMT030101UK04EhrComposition> createEhrComposition(RCMRMT030101UK04EhrComposition value) {
        return new JAXBElement<RCMRMT030101UK04EhrComposition>(_EHR_COMPOSITION_QNAME, RCMRMT030101UK04EhrComposition.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "LinkSet")
    public JAXBElement<RCMRMT030101UK04LinkSet> createLinkset(RCMRMT030101UK04LinkSet value) {
        return new JAXBElement<RCMRMT030101UK04LinkSet>(_LINKSET_QNAME, RCMRMT030101UK04LinkSet.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "ObservationStatement")
    public JAXBElement<RCMRMT030101UK04ObservationStatement> createLinkset(RCMRMT030101UK04ObservationStatement value) {
        return new JAXBElement<RCMRMT030101UK04ObservationStatement>(_LINKSET_QNAME, RCMRMT030101UK04ObservationStatement.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "patient")
    public JAXBElement<RCMRMT030101UK04Patient> createPatient(RCMRMT030101UK04Patient patient) {
        return new JAXBElement<RCMRMT030101UK04Patient>(_PATIENT_QNAME, RCMRMT030101UK04Patient.class, null, patient);
    }
}
