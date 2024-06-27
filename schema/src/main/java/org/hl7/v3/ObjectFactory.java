
package org.hl7.v3;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
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

    public static final String NAMESPACEURI = "urn:hl7-org:v3";

    private final static QName _ControlActEvent_QNAME = new QName(NAMESPACEURI, "ControlActEvent");
    private final static QName _EhrExtract_QNAME = new QName(NAMESPACEURI, "EhrExtract");
    private final static QName _Message_QNAME = new QName(NAMESPACEURI, "Message");
    private final static QName _RCMRIN030000UK06_QNAME = new QName(NAMESPACEURI, "RCMR_IN030000UK06");
    private final static QName _RCMRIN030000UK07_QNAME = new QName(NAMESPACEURI, "RCMR_IN030000UK07");
    private final static QName _COPCIN000001UK01_QNAME = new QName(NAMESPACEURI, "COPC_IN000001UK01");
    private final static QName _IVLINTLow_QNAME = new QName(NAMESPACEURI, "low");
    private final static QName _IVLINTHigh_QNAME = new QName(NAMESPACEURI, "high");
    private final static QName _IVLPQWidth_QNAME = new QName(NAMESPACEURI, "width");
    private final static QName _IVLPQCenter_QNAME = new QName(NAMESPACEURI, "center");
    private final static QName _ENDelimiter_QNAME = new QName(NAMESPACEURI, "delimiter");
    private final static QName _ENFamily_QNAME = new QName(NAMESPACEURI, "family");
    private final static QName _ENGiven_QNAME = new QName(NAMESPACEURI, "given");
    private final static QName _ENPrefix_QNAME = new QName(NAMESPACEURI, "prefix");
    private final static QName _ENSuffix_QNAME = new QName(NAMESPACEURI, "suffix");
    private final static QName _ENValidTime_QNAME = new QName(NAMESPACEURI, "validTime");
    private final static QName _ENId_QNAME = new QName(NAMESPACEURI, "id");
    private final static QName _ADCountry_QNAME = new QName(NAMESPACEURI, "country");
    private final static QName _ADState_QNAME = new QName(NAMESPACEURI, "state");
    private final static QName _ADCounty_QNAME = new QName(NAMESPACEURI, "county");
    private final static QName _ADCity_QNAME = new QName(NAMESPACEURI, "city");
    private final static QName _ADPostalCode_QNAME = new QName(NAMESPACEURI, "postalCode");
    private final static QName _ADStreetAddressLine_QNAME = new QName(NAMESPACEURI, "streetAddressLine");
    private final static QName _ADHouseNumber_QNAME = new QName(NAMESPACEURI, "houseNumber");
    private final static QName _ADHouseNumberNumeric_QNAME = new QName(NAMESPACEURI, "houseNumberNumeric");
    private final static QName _ADDirection_QNAME = new QName(NAMESPACEURI, "direction");
    private final static QName _ADStreetName_QNAME = new QName(NAMESPACEURI, "streetName");
    private final static QName _ADStreetNameBase_QNAME = new QName(NAMESPACEURI, "streetNameBase");
    private final static QName _ADStreetNameType_QNAME = new QName(NAMESPACEURI, "streetNameType");
    private final static QName _ADAdditionalLocator_QNAME = new QName(NAMESPACEURI, "additionalLocator");
    private final static QName _ADUnitID_QNAME = new QName(NAMESPACEURI, "unitID");
    private final static QName _ADUnitType_QNAME = new QName(NAMESPACEURI, "unitType");
    private final static QName _ADCarrier_QNAME = new QName(NAMESPACEURI, "carrier");
    private final static QName _ADCensusTract_QNAME = new QName(NAMESPACEURI, "censusTract");
    private final static QName _ADAddressKey_QNAME = new QName(NAMESPACEURI, "addressKey");
    private final static QName _ADDesc_QNAME = new QName(NAMESPACEURI, "desc");
    private final static QName _ADUseablePeriod_QNAME = new QName(NAMESPACEURI, "useablePeriod");
    private final static QName _StrucDocThContent_QNAME = new QName(NAMESPACEURI, "content");
    private final static QName _StrucDocThLinkHtml_QNAME = new QName(NAMESPACEURI, "linkHtml");
    private final static QName _StrucDocThSub_QNAME = new QName(NAMESPACEURI, "sub");
    private final static QName _StrucDocThSup_QNAME = new QName(NAMESPACEURI, "sup");
    private final static QName _StrucDocThBr_QNAME = new QName(NAMESPACEURI, "br");
    private final static QName _StrucDocThFootnote_QNAME = new QName(NAMESPACEURI, "footnote");
    private final static QName _StrucDocThFootnoteRef_QNAME = new QName(NAMESPACEURI, "footnoteRef");
    private final static QName _StrucDocThRenderMultiMedia_QNAME = new QName(NAMESPACEURI, "renderMultiMedia");
    private final static QName _StrucDocTdParagraph_QNAME = new QName(NAMESPACEURI, "paragraph");
    private final static QName _StrucDocTdList_QNAME = new QName(NAMESPACEURI, "list");
    private final static QName _StrucDocParagraphCaption_QNAME = new QName(NAMESPACEURI, "caption");
    private final static QName _StrucDocItemTable_QNAME = new QName(NAMESPACEURI, "table");

    /**
     * List of custom-built QName objects that weren't pre-generated. These are being used in unit tests to test specific
     * deeply nested elements.
     *
     */
    private final static QName _CD_QNAME = new QName(NAMESPACEURI, "code");
    private final static QName _EHR_COMPOSITION_QNAME = new QName(NAMESPACEURI, "ehrComposition");
    private final static QName _LINKSET_QNAME = new QName(NAMESPACEURI, "LinkSet");
    private final static QName _OBSERVATION_STATEMENT_QNAME = new QName(NAMESPACEURI, "ObservationStatement");
    private final static QName _PATIENT_QNAME = new QName(NAMESPACEURI, "patient");
    private final static QName _AGENT_DIRECTORY_QNAME = new QName(NAMESPACEURI, "agentDirectory");
    private final static QName _ADDRESS_QNAME = new QName(NAMESPACEURI, "addr");
    private final static QName _TELECOM_QNAME = new QName(NAMESPACEURI, "telecom");
    private final static QName _EHR_SUPPLY_AUTHORISE = new QName(NAMESPACEURI, "ehrSupplyAuthorise");
    private final static QName _MEDICATION_STATEMENT = new QName(NAMESPACEURI, "MedicationStatement");
    private final static QName _CONSUMABLE = new QName(NAMESPACEURI, "Consumable");
    private final static QName _COMPOUND_STATEMENT = new QName(NAMESPACEURI, "CompoundStatement");
    private final static QName _NARRATIVE_STATEMENT_QNAME = new QName(NAMESPACEURI, "NarrativeStatement");

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
     * Create an instance of {@link RCMRMT030101UKEhrExtract }
     * 
     */
    public RCMRMT030101UKEhrExtract createRCMRMT030101UK04EhrExtract() {
        return new RCMRMT030101UKEhrExtract();
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
     * Create an instance of {@link COPCIN000001UK01Message }
     *
     */
    public COPCIN000001UK01Message createCOPCIN000001UK01Message() {
        return new COPCIN000001UK01Message();
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
     * Create an instance of {@link RCMRMT030101UKAuthor3 }
     * 
     */
    public RCMRMT030101UKAuthor3 createRCMRMT030101UK04Author3() {
        return new RCMRMT030101UKAuthor3();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKDestination }
     * 
     */
    public RCMRMT030101UKDestination createRCMRMT030101UK04Destination() {
        return new RCMRMT030101UKDestination();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKComponent }
     * 
     */
    public RCMRMT030101UKComponent createRCMRMT030101UK04Component() {
        return new RCMRMT030101UKComponent();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKEhrFolder }
     * 
     */
    public RCMRMT030101UKEhrFolder createRCMRMT030101UK04EhrFolder() {
        return new RCMRMT030101UKEhrFolder();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKAuthor2 }
     * 
     */
    public RCMRMT030101UKAuthor2 createRCMRMT030101UK04Author2() {
        return new RCMRMT030101UKAuthor2();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ResponsibleParty }
     * 
     */
    public RCMRMT030101UK04ResponsibleParty createRCMRMT030101UK04ResponsibleParty() {
        return new RCMRMT030101UK04ResponsibleParty();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKAgentDirectory }
     * 
     */
    public RCMRMT030101UKAgentDirectory createRCMRMT030101UK04AgentDirectory() {
        return new RCMRMT030101UKAgentDirectory();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKPart }
     * 
     */
    public RCMRMT030101UKPart createRCMRMT030101UK04Part() {
        return new RCMRMT030101UKPart();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKComponent3 }
     * 
     */
    public RCMRMT030101UKComponent3 createRCMRMT030101UK04Component3() {
        return new RCMRMT030101UKComponent3();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKEhrComposition }
     * 
     */
    public RCMRMT030101UKEhrComposition createRCMRMT030101UK04EhrComposition() {
        return new RCMRMT030101UKEhrComposition();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKAuthor }
     * 
     */
    public RCMRMT030101UKAuthor createRCMRMT030101UK04Author() {
        return new RCMRMT030101UKAuthor();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKAgentRef }
     * 
     */
    public RCMRMT030101UKAgentRef createRCMRMT030101UK04AgentRef() {
        return new RCMRMT030101UKAgentRef();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKLocation }
     * 
     */
    public RCMRMT030101UKLocation createRCMRMT030101UK04Location() {
        return new RCMRMT030101UKLocation();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKLocatedEntity }
     * 
     */
    public RCMRMT030101UKLocatedEntity createRCMRMT030101UK04LocatedEntity() {
        return new RCMRMT030101UKLocatedEntity();
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
     * Create an instance of {@link RCMRMT030101UKComponent4 }
     * 
     */
    public RCMRMT030101UKComponent4 createRCMRMT030101UK04Component4() {
        return new RCMRMT030101UKComponent4();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKInformant }
     * 
     */
    public RCMRMT030101UKInformant createRCMRMT030101UK04Informant() {
        return new RCMRMT030101UKInformant();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKInformantRole }
     * 
     */
    public RCMRMT030101UKInformantRole createRCMRMT030101UK04InformantRole() {
        return new RCMRMT030101UKInformantRole();
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
     * Create an instance of {@link RCMRMT030101UKExternalDocument }
     * 
     */
    public RCMRMT030101UKExternalDocument createRCMRMT030101UK04ExternalDocument() {
        return new RCMRMT030101UKExternalDocument();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKAuthor4 }
     * 
     */
    public RCMRMT030101UKAuthor4 createRCMRMT030101UK04Author4() {
        return new RCMRMT030101UKAuthor4();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04SequelTo }
     * 
     */
    public RCMRMT030101UK04SequelTo createRCMRMT030101UK04SequelTo() {
        return new RCMRMT030101UK04SequelTo();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKCompoundStatement }
     * 
     */
    public RCMRMT030101UKCompoundStatement createRCMRMT030101UK04CompoundStatement() {
        return new RCMRMT030101UKCompoundStatement();
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
     * Create an instance of {@link RCMRMT030101UKComponent02 }
     * 
     */
    public RCMRMT030101UKComponent02 createRCMRMT030101UK04Component02() {
        return new RCMRMT030101UKComponent02();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKEhrEmpty }
     * 
     */
    public RCMRMT030101UKEhrEmpty createRCMRMT030101UK04EhrEmpty() {
        return new RCMRMT030101UKEhrEmpty();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKLinkSet }
     * 
     */
    public RCMRMT030101UKLinkSet createRCMRMT030101UK04LinkSet() {
        return new RCMRMT030101UKLinkSet();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKComponent6 }
     * 
     */
    public RCMRMT030101UKComponent6 createRCMRMT030101UK04Component6() {
        return new RCMRMT030101UKComponent6();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKConditionNamed }
     * 
     */
    public RCMRMT030101UKConditionNamed createRCMRMT030101UK04ConditionNamed() {
        return new RCMRMT030101UKConditionNamed();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKMedicationStatement }
     * 
     */
    public RCMRMT030101UKMedicationStatement createRCMRMT030101UK04MedicationStatement() {
        return new RCMRMT030101UKMedicationStatement();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKConsumable }
     * 
     */
    public RCMRMT030101UKConsumable createRCMRMT030101UK04Consumable() {
        return new RCMRMT030101UKConsumable();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKManufacturedProduct }
     * 
     */
    public RCMRMT030101UKManufacturedProduct createRCMRMT030101UK04ManufacturedProduct() {
        return new RCMRMT030101UKManufacturedProduct();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKMaterial }
     * 
     */
    public RCMRMT030101UKMaterial createRCMRMT030101UK04Material() {
        return new RCMRMT030101UKMaterial();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKComponent2 }
     * 
     */
    public RCMRMT030101UKComponent2 createRCMRMT030101UK04Component2() {
        return new RCMRMT030101UKComponent2();
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
     * Create an instance of {@link RCMRMT030101UKAuthorise }
     * 
     */
    public RCMRMT030101UKAuthorise createRCMRMT030101UK04Authorise() {
        return new RCMRMT030101UKAuthorise();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Predecessor }
     * 
     */
    public RCMRMT030101UK04Predecessor createRCMRMT030101UK04Predecessor() {
        return new RCMRMT030101UK04Predecessor();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKMedicationRef }
     * 
     */
    public RCMRMT030101UKMedicationRef createRCMRMT030101UK04MedicationRef() {
        return new RCMRMT030101UKMedicationRef();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKDiscontinue }
     * 
     */
    public RCMRMT030101UKDiscontinue createRCMRMT030101UK04Discontinue() {
        return new RCMRMT030101UKDiscontinue();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ReversalOf }
     * 
     */
    public RCMRMT030101UK04ReversalOf createRCMRMT030101UK04ReversalOf() {
        return new RCMRMT030101UK04ReversalOf();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKDispense }
     * 
     */
    public RCMRMT030101UKDispense createRCMRMT030101UK04Dispense() {
        return new RCMRMT030101UKDispense();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKInFulfillmentOf }
     * 
     */
    public RCMRMT030101UKInFulfillmentOf createRCMRMT030101UK04InFulfillmentOf() {
        return new RCMRMT030101UKInFulfillmentOf();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04Prescribe }
     * 
     */
    public RCMRMT030101UK04Prescribe createRCMRMT030101UK04Prescribe() {
        return new RCMRMT030101UK04Prescribe();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKInFulfillmentOf02 }
     * 
     */
    public RCMRMT030101UKInFulfillmentOf02 createRCMRMT030101UK04InFulfillmentOf02() {
        return new RCMRMT030101UKInFulfillmentOf02();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04PertinentInformation }
     * 
     */
    public RCMRMT030101UK04PertinentInformation createRCMRMT030101UK04PertinentInformation() {
        return new RCMRMT030101UK04PertinentInformation();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKMedicationDosage }
     * 
     */
    public RCMRMT030101UKMedicationDosage createRCMRMT030101UK04MedicationDosage() {
        return new RCMRMT030101UKMedicationDosage();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKNarrativeStatement }
     * 
     */
    public RCMRMT030101UKNarrativeStatement createRCMRMT030101UK04NarrativeStatement() {
        return new RCMRMT030101UKNarrativeStatement();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKObservationStatement }
     * 
     */
    public RCMRMT030101UKObservationStatement createRCMRMT030101UK04ObservationStatement() {
        return new RCMRMT030101UKObservationStatement();
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
     * Create an instance of {@link RCMRMT030101UKAnnotation }
     * 
     */
    public RCMRMT030101UKAnnotation createRCMRMT030101UK04Annotation() {
        return new RCMRMT030101UKAnnotation();
    }

    /**
     * Create an instance of {@link RCMRMT030101UK04ReferenceRange }
     * 
     */
    public RCMRMT030101UK04ReferenceRange createRCMRMT030101UK04ReferenceRange() {
        return new RCMRMT030101UK04ReferenceRange();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKInterpretationRange }
     * 
     */
    public RCMRMT030101UKInterpretationRange createRCMRMT030101UK04InterpretationRange() {
        return new RCMRMT030101UKInterpretationRange();
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
     * Create an instance of {@link RCMRMT030101UKCompositionRef }
     * 
     */
    public RCMRMT030101UKCompositionRef createRCMRMT030101UK04CompositionRef() {
        return new RCMRMT030101UKCompositionRef();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKInFulfillmentOf2 }
     * 
     */
    public RCMRMT030101UKInFulfillmentOf2 createRCMRMT030101UK04InFulfillmentOf2() {
        return new RCMRMT030101UKInFulfillmentOf2();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKEhrRequest }
     * 
     */
    public RCMRMT030101UKEhrRequest createRCMRMT030101UK04EhrRequest() {
        return new RCMRMT030101UKEhrRequest();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKLimitation }
     * 
     */
    public RCMRMT030101UKLimitation createRCMRMT030101UK04Limitation() {
        return new RCMRMT030101UKLimitation();
    }

    /**
     * Create an instance of {@link RCMRMT030101UKEhrExtractSpecification }
     * 
     */
    public RCMRMT030101UKEhrExtractSpecification createRCMRMT030101UK04EhrExtractSpecification() {
        return new RCMRMT030101UKEhrExtractSpecification();
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
     * Create an instance of {@link COPCIN000001UK01ControlActEvent }
     *
     */
    public COPCIN000001UK01ControlActEvent createCOPCIN000001UK01ControlActEvent() {
        return new COPCIN000001UK01ControlActEvent();
    }

    /**
     * Create an instance of {@link RCMRIN030000UK06Subject }
     *
     */
    public COPCIN000001UK01Subject createRCMRIN030000UK06Subject() {
        return new COPCIN000001UK01Subject();
    }

    /**
     * Create an instance of {@link COPCIN000001UK01Subject }
     *
     */
    public COPCIN000001UK01Subject createCOPCIN000001UK01Subject() {
        return new COPCIN000001UK01Subject();
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
    @XmlElementDecl(namespace = NAMESPACEURI, name = "ControlActEvent")
    public JAXBElement<MCAIMT040101UK03ControlActEvent> createControlActEvent(MCAIMT040101UK03ControlActEvent value) {
        return new JAXBElement<>(_ControlActEvent_QNAME, MCAIMT040101UK03ControlActEvent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RCMRMT030101UKEhrExtract }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link RCMRMT030101UKEhrExtract }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "EhrExtract")
    public JAXBElement<RCMRMT030101UKEhrExtract> createEhrExtract(RCMRMT030101UKEhrExtract value) {
        return new JAXBElement<>(_EhrExtract_QNAME, RCMRMT030101UKEhrExtract.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MCCIMT010101UK12Message }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link MCCIMT010101UK12Message }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "Message")
    public JAXBElement<MCCIMT010101UK12Message> createMessage(MCCIMT010101UK12Message value) {
        return new JAXBElement<>(_Message_QNAME, MCCIMT010101UK12Message.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RCMRIN030000UK06Message }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link RCMRIN030000UK06Message }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "RCMR_IN030000UK06")
    public JAXBElement<RCMRIN030000UK06Message> createRCMRIN030000UK06(RCMRIN030000UK06Message value) {
        return new JAXBElement<>(_RCMRIN030000UK06_QNAME, RCMRIN030000UK06Message.class, null, value);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "RCMR_IN030000UK07")
    public JAXBElement<RCMRIN030000UK07Message> createRCMRIN030000UK07(RCMRIN030000UK07Message value) {
        return new JAXBElement<>(_RCMRIN030000UK07_QNAME, RCMRIN030000UK07Message.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link COPCIN000001UK01Message }{@code >}
     *
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link COPCIN000001UK01Message }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "COPC_IN000001UK01")
    public JAXBElement<COPCIN000001UK01Message> createCOPCIN000001UK01(COPCIN000001UK01Message value) {
        return new JAXBElement<>(_COPCIN000001UK01_QNAME, COPCIN000001UK01Message.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IVLINT.Low }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IVLINT.Low }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "low", scope = IVLINT.class)
    public JAXBElement<IVLINT.Low> createIVLINTLow(IVLINT.Low value) {
        return new JAXBElement<>(_IVLINTLow_QNAME, IVLINT.Low.class, IVLINT.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IVLINT.High }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IVLINT.High }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "high", scope = IVLINT.class)
    public JAXBElement<IVLINT.High> createIVLINTHigh(IVLINT.High value) {
        return new JAXBElement<>(_IVLINTHigh_QNAME, IVLINT.High.class, IVLINT.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PQInc }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PQInc }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "low", scope = IVLPQ.class)
    public JAXBElement<PQInc> createIVLPQLow(PQInc value) {
        return new JAXBElement<>(_IVLINTLow_QNAME, PQInc.class, IVLPQ.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PQ }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PQ }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "width", scope = IVLPQ.class)
    public JAXBElement<PQ> createIVLPQWidth(PQ value) {
        return new JAXBElement<>(_IVLPQWidth_QNAME, PQ.class, IVLPQ.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PQInc }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PQInc }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "high", scope = IVLPQ.class)
    public JAXBElement<PQInc> createIVLPQHigh(PQInc value) {
        return new JAXBElement<>(_IVLINTHigh_QNAME, PQInc.class, IVLPQ.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PQ }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PQ }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "center", scope = IVLPQ.class)
    public JAXBElement<PQ> createIVLPQCenter(PQ value) {
        return new JAXBElement<>(_IVLPQCenter_QNAME, PQ.class, IVLPQ.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IVXBTS }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IVXBTS }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "low", scope = IVLTS.class)
    public JAXBElement<IVXBTS> createIVLTSLow(IVXBTS value) {
        return new JAXBElement<>(_IVLINTLow_QNAME, IVXBTS.class, IVLTS.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PQ }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link PQ }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "width", scope = IVLTS.class)
    public JAXBElement<PQ> createIVLTSWidth(PQ value) {
        return new JAXBElement<>(_IVLPQWidth_QNAME, PQ.class, IVLTS.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IVXBTS }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IVXBTS }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "high", scope = IVLTS.class)
    public JAXBElement<IVXBTS> createIVLTSHigh(IVXBTS value) {
        return new JAXBElement<>(_IVLINTHigh_QNAME, IVXBTS.class, IVLTS.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TS }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link TS }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "center", scope = IVLTS.class)
    public JAXBElement<TS> createIVLTSCenter(TS value) {
        return new JAXBElement<>(_IVLPQCenter_QNAME, TS.class, IVLTS.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnDelimiter }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EnDelimiter }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "delimiter", scope = EN.class)
    public JAXBElement<EnDelimiter> createENDelimiter(EnDelimiter value) {
        return new JAXBElement<>(_ENDelimiter_QNAME, EnDelimiter.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnFamily }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EnFamily }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "family", scope = EN.class)
    public JAXBElement<EnFamily> createENFamily(EnFamily value) {
        return new JAXBElement<>(_ENFamily_QNAME, EnFamily.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnGiven }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EnGiven }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "given", scope = EN.class)
    public JAXBElement<EnGiven> createENGiven(EnGiven value) {
        return new JAXBElement<>(_ENGiven_QNAME, EnGiven.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnPrefix }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EnPrefix }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "prefix", scope = EN.class)
    public JAXBElement<EnPrefix> createENPrefix(EnPrefix value) {
        return new JAXBElement<>(_ENPrefix_QNAME, EnPrefix.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnSuffix }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EnSuffix }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "suffix", scope = EN.class)
    public JAXBElement<EnSuffix> createENSuffix(EnSuffix value) {
        return new JAXBElement<>(_ENSuffix_QNAME, EnSuffix.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IVLTS }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IVLTS }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "validTime", scope = EN.class)
    public JAXBElement<IVLTS> createENValidTime(IVLTS value) {
        return new JAXBElement<>(_ENValidTime_QNAME, IVLTS.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link II }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link II }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "id", scope = EN.class)
    public JAXBElement<II> createENId(II value) {
        return new JAXBElement<>(_ENId_QNAME, II.class, EN.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.Delimiter }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.Delimiter }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "delimiter", scope = AD.class)
    public JAXBElement<AD.Delimiter> createADDelimiter(AD.Delimiter value) {
        return new JAXBElement<>(_ENDelimiter_QNAME, AD.Delimiter.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.Country }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.Country }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "country", scope = AD.class)
    public JAXBElement<AD.Country> createADCountry(AD.Country value) {
        return new JAXBElement<>(_ADCountry_QNAME, AD.Country.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.State }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.State }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "state", scope = AD.class)
    public JAXBElement<AD.State> createADState(AD.State value) {
        return new JAXBElement<>(_ADState_QNAME, AD.State.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.County }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.County }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "county", scope = AD.class)
    public JAXBElement<AD.County> createADCounty(AD.County value) {
        return new JAXBElement<>(_ADCounty_QNAME, AD.County.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.City }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.City }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "city", scope = AD.class)
    public JAXBElement<AD.City> createADCity(AD.City value) {
        return new JAXBElement<>(_ADCity_QNAME, AD.City.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.PostalCode }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.PostalCode }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "postalCode", scope = AD.class)
    public JAXBElement<AD.PostalCode> createADPostalCode(AD.PostalCode value) {
        return new JAXBElement<>(_ADPostalCode_QNAME, AD.PostalCode.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.StreetAddressLine }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.StreetAddressLine }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "streetAddressLine", scope = AD.class)
    public JAXBElement<AD.StreetAddressLine> createADStreetAddressLine(AD.StreetAddressLine value) {
        return new JAXBElement<>(_ADStreetAddressLine_QNAME, AD.StreetAddressLine.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.HouseNumber }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.HouseNumber }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "houseNumber", scope = AD.class)
    public JAXBElement<AD.HouseNumber> createADHouseNumber(AD.HouseNumber value) {
        return new JAXBElement<>(_ADHouseNumber_QNAME, AD.HouseNumber.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.HouseNumberNumeric }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.HouseNumberNumeric }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "houseNumberNumeric", scope = AD.class)
    public JAXBElement<AD.HouseNumberNumeric> createADHouseNumberNumeric(AD.HouseNumberNumeric value) {
        return new JAXBElement<>(_ADHouseNumberNumeric_QNAME, AD.HouseNumberNumeric.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.Direction }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.Direction }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "direction", scope = AD.class)
    public JAXBElement<AD.Direction> createADDirection(AD.Direction value) {
        return new JAXBElement<>(_ADDirection_QNAME, AD.Direction.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.StreetName }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.StreetName }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "streetName", scope = AD.class)
    public JAXBElement<AD.StreetName> createADStreetName(AD.StreetName value) {
        return new JAXBElement<>(_ADStreetName_QNAME, AD.StreetName.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.StreetNameBase }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.StreetNameBase }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "streetNameBase", scope = AD.class)
    public JAXBElement<AD.StreetNameBase> createADStreetNameBase(AD.StreetNameBase value) {
        return new JAXBElement<>(_ADStreetNameBase_QNAME, AD.StreetNameBase.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.StreetNameType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.StreetNameType }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "streetNameType", scope = AD.class)
    public JAXBElement<AD.StreetNameType> createADStreetNameType(AD.StreetNameType value) {
        return new JAXBElement<>(_ADStreetNameType_QNAME, AD.StreetNameType.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.AdditionalLocator }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.AdditionalLocator }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "additionalLocator", scope = AD.class)
    public JAXBElement<AD.AdditionalLocator> createADAdditionalLocator(AD.AdditionalLocator value) {
        return new JAXBElement<>(_ADAdditionalLocator_QNAME, AD.AdditionalLocator.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.UnitID }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.UnitID }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "unitID", scope = AD.class)
    public JAXBElement<AD.UnitID> createADUnitID(AD.UnitID value) {
        return new JAXBElement<>(_ADUnitID_QNAME, AD.UnitID.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.UnitType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.UnitType }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "unitType", scope = AD.class)
    public JAXBElement<AD.UnitType> createADUnitType(AD.UnitType value) {
        return new JAXBElement<>(_ADUnitType_QNAME, AD.UnitType.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.Carrier }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.Carrier }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "carrier", scope = AD.class)
    public JAXBElement<AD.Carrier> createADCarrier(AD.Carrier value) {
        return new JAXBElement<>(_ADCarrier_QNAME, AD.Carrier.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.CensusTract }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.CensusTract }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "censusTract", scope = AD.class)
    public JAXBElement<AD.CensusTract> createADCensusTract(AD.CensusTract value) {
        return new JAXBElement<>(_ADCensusTract_QNAME, AD.CensusTract.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.AddressKey }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.AddressKey }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "addressKey", scope = AD.class)
    public JAXBElement<AD.AddressKey> createADAddressKey(AD.AddressKey value) {
        return new JAXBElement<>(_ADAddressKey_QNAME, AD.AddressKey.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AD.Desc }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AD.Desc }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "desc", scope = AD.class)
    public JAXBElement<AD.Desc> createADDesc(AD.Desc value) {
        return new JAXBElement<>(_ADDesc_QNAME, AD.Desc.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IVLTS }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IVLTS }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "useablePeriod", scope = AD.class)
    public JAXBElement<IVLTS> createADUseablePeriod(IVLTS value) {
        return new JAXBElement<>(_ADUseablePeriod_QNAME, IVLTS.class, AD.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link II }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link II }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "id", scope = AD.class)
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
    @XmlElementDecl(namespace = NAMESPACEURI, name = "content", scope = StrucDocTh.class)
    public JAXBElement<StrucDocContent> createStrucDocThContent(StrucDocContent value) {
        return new JAXBElement<>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "linkHtml", scope = StrucDocTh.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocThLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sub", scope = StrucDocTh.class)
    public JAXBElement<StrucDocSub> createStrucDocThSub(StrucDocSub value) {
        return new JAXBElement<>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sup", scope = StrucDocTh.class)
    public JAXBElement<StrucDocSup> createStrucDocThSup(StrucDocSup value) {
        return new JAXBElement<>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "br", scope = StrucDocTh.class)
    public JAXBElement<StrucDocBr> createStrucDocThBr(StrucDocBr value) {
        return new JAXBElement<>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnote", scope = StrucDocTh.class)
    public JAXBElement<StrucDocFootnote> createStrucDocThFootnote(StrucDocFootnote value) {
        return new JAXBElement<>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnoteRef", scope = StrucDocTh.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocThFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "renderMultiMedia", scope = StrucDocTh.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocThRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocTh.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "content", scope = StrucDocTd.class)
    public JAXBElement<StrucDocContent> createStrucDocTdContent(StrucDocContent value) {
        return new JAXBElement<>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "linkHtml", scope = StrucDocTd.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocTdLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sub", scope = StrucDocTd.class)
    public JAXBElement<StrucDocSub> createStrucDocTdSub(StrucDocSub value) {
        return new JAXBElement<>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sup", scope = StrucDocTd.class)
    public JAXBElement<StrucDocSup> createStrucDocTdSup(StrucDocSup value) {
        return new JAXBElement<>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "br", scope = StrucDocTd.class)
    public JAXBElement<StrucDocBr> createStrucDocTdBr(StrucDocBr value) {
        return new JAXBElement<>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnote", scope = StrucDocTd.class)
    public JAXBElement<StrucDocFootnote> createStrucDocTdFootnote(StrucDocFootnote value) {
        return new JAXBElement<>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnoteRef", scope = StrucDocTd.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocTdFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "renderMultiMedia", scope = StrucDocTd.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocTdRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "paragraph", scope = StrucDocTd.class)
    public JAXBElement<StrucDocParagraph> createStrucDocTdParagraph(StrucDocParagraph value) {
        return new JAXBElement<>(_StrucDocTdParagraph_QNAME, StrucDocParagraph.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "list", scope = StrucDocTd.class)
    public JAXBElement<StrucDocList> createStrucDocTdList(StrucDocList value) {
        return new JAXBElement<>(_StrucDocTdList_QNAME, StrucDocList.class, StrucDocTd.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocCaption }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocCaption }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "caption", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocCaption> createStrucDocParagraphCaption(StrucDocCaption value) {
        return new JAXBElement<>(_StrucDocParagraphCaption_QNAME, StrucDocCaption.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "content", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocContent> createStrucDocParagraphContent(StrucDocContent value) {
        return new JAXBElement<>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "linkHtml", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocParagraphLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sub", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocSub> createStrucDocParagraphSub(StrucDocSub value) {
        return new JAXBElement<>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sup", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocSup> createStrucDocParagraphSup(StrucDocSup value) {
        return new JAXBElement<>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "br", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocBr> createStrucDocParagraphBr(StrucDocBr value) {
        return new JAXBElement<>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnote", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocFootnote> createStrucDocParagraphFootnote(StrucDocFootnote value) {
        return new JAXBElement<>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnoteRef", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocParagraphFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "renderMultiMedia", scope = StrucDocParagraph.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocParagraphRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocParagraph.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnote", scope = StrucDocLinkHtml.class)
    public JAXBElement<StrucDocFootnote> createStrucDocLinkHtmlFootnote(StrucDocFootnote value) {
        return new JAXBElement<>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocLinkHtml.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnoteRef", scope = StrucDocLinkHtml.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocLinkHtmlFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocLinkHtml.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocCaption }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocCaption }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "caption", scope = StrucDocItem.class)
    public JAXBElement<StrucDocCaption> createStrucDocItemCaption(StrucDocCaption value) {
        return new JAXBElement<>(_StrucDocParagraphCaption_QNAME, StrucDocCaption.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "content", scope = StrucDocItem.class)
    public JAXBElement<StrucDocContent> createStrucDocItemContent(StrucDocContent value) {
        return new JAXBElement<>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "linkHtml", scope = StrucDocItem.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocItemLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sub", scope = StrucDocItem.class)
    public JAXBElement<StrucDocSub> createStrucDocItemSub(StrucDocSub value) {
        return new JAXBElement<>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sup", scope = StrucDocItem.class)
    public JAXBElement<StrucDocSup> createStrucDocItemSup(StrucDocSup value) {
        return new JAXBElement<>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "br", scope = StrucDocItem.class)
    public JAXBElement<StrucDocBr> createStrucDocItemBr(StrucDocBr value) {
        return new JAXBElement<>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnote", scope = StrucDocItem.class)
    public JAXBElement<StrucDocFootnote> createStrucDocItemFootnote(StrucDocFootnote value) {
        return new JAXBElement<>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnoteRef", scope = StrucDocItem.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocItemFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "renderMultiMedia", scope = StrucDocItem.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocItemRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "paragraph", scope = StrucDocItem.class)
    public JAXBElement<StrucDocParagraph> createStrucDocItemParagraph(StrucDocParagraph value) {
        return new JAXBElement<>(_StrucDocTdParagraph_QNAME, StrucDocParagraph.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "list", scope = StrucDocItem.class)
    public JAXBElement<StrucDocList> createStrucDocItemList(StrucDocList value) {
        return new JAXBElement<>(_StrucDocTdList_QNAME, StrucDocList.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTable }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTable }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "table", scope = StrucDocItem.class)
    public JAXBElement<StrucDocTable> createStrucDocItemTable(StrucDocTable value) {
        return new JAXBElement<>(_StrucDocItemTable_QNAME, StrucDocTable.class, StrucDocItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTitleContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTitleContent }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "content", scope = StrucDocTitleFootnote.class)
    public JAXBElement<StrucDocTitleContent> createStrucDocTitleFootnoteContent(StrucDocTitleContent value) {
        return new JAXBElement<>(_StrucDocThContent_QNAME, StrucDocTitleContent.class, StrucDocTitleFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sub", scope = StrucDocTitleFootnote.class)
    public JAXBElement<StrucDocSub> createStrucDocTitleFootnoteSub(StrucDocSub value) {
        return new JAXBElement<>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocTitleFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sup", scope = StrucDocTitleFootnote.class)
    public JAXBElement<StrucDocSup> createStrucDocTitleFootnoteSup(StrucDocSup value) {
        return new JAXBElement<>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocTitleFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "br", scope = StrucDocTitleFootnote.class)
    public JAXBElement<StrucDocBr> createStrucDocTitleFootnoteBr(StrucDocBr value) {
        return new JAXBElement<>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocTitleFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "content", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocContent> createStrucDocFootnoteContent(StrucDocContent value) {
        return new JAXBElement<>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "linkHtml", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocFootnoteLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sub", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocSub> createStrucDocFootnoteSub(StrucDocSub value) {
        return new JAXBElement<>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sup", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocSup> createStrucDocFootnoteSup(StrucDocSup value) {
        return new JAXBElement<>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "br", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocBr> createStrucDocFootnoteBr(StrucDocBr value) {
        return new JAXBElement<>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "renderMultiMedia", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocFootnoteRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "paragraph", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocParagraph> createStrucDocFootnoteParagraph(StrucDocParagraph value) {
        return new JAXBElement<>(_StrucDocTdParagraph_QNAME, StrucDocParagraph.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "list", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocList> createStrucDocFootnoteList(StrucDocList value) {
        return new JAXBElement<>(_StrucDocTdList_QNAME, StrucDocList.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTable }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTable }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "table", scope = StrucDocFootnote.class)
    public JAXBElement<StrucDocTable> createStrucDocFootnoteTable(StrucDocTable value) {
        return new JAXBElement<>(_StrucDocItemTable_QNAME, StrucDocTable.class, StrucDocFootnote.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTitleContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTitleContent }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "content", scope = StrucDocTitleContent.class)
    public JAXBElement<StrucDocTitleContent> createStrucDocTitleContentContent(StrucDocTitleContent value) {
        return new JAXBElement<>(_StrucDocThContent_QNAME, StrucDocTitleContent.class, StrucDocTitleContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sub", scope = StrucDocTitleContent.class)
    public JAXBElement<StrucDocSub> createStrucDocTitleContentSub(StrucDocSub value) {
        return new JAXBElement<>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocTitleContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sup", scope = StrucDocTitleContent.class)
    public JAXBElement<StrucDocSup> createStrucDocTitleContentSup(StrucDocSup value) {
        return new JAXBElement<>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocTitleContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "br", scope = StrucDocTitleContent.class)
    public JAXBElement<StrucDocBr> createStrucDocTitleContentBr(StrucDocBr value) {
        return new JAXBElement<>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocTitleContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTitleFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTitleFootnote }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnote", scope = StrucDocTitleContent.class)
    public JAXBElement<StrucDocTitleFootnote> createStrucDocTitleContentFootnote(StrucDocTitleFootnote value) {
        return new JAXBElement<>(_StrucDocThFootnote_QNAME, StrucDocTitleFootnote.class, StrucDocTitleContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnoteRef", scope = StrucDocTitleContent.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocTitleContentFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocTitleContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "content", scope = StrucDocContent.class)
    public JAXBElement<StrucDocContent> createStrucDocContentContent(StrucDocContent value) {
        return new JAXBElement<>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "linkHtml", scope = StrucDocContent.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocContentLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sub", scope = StrucDocContent.class)
    public JAXBElement<StrucDocSub> createStrucDocContentSub(StrucDocSub value) {
        return new JAXBElement<>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sup", scope = StrucDocContent.class)
    public JAXBElement<StrucDocSup> createStrucDocContentSup(StrucDocSup value) {
        return new JAXBElement<>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "br", scope = StrucDocContent.class)
    public JAXBElement<StrucDocBr> createStrucDocContentBr(StrucDocBr value) {
        return new JAXBElement<>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnote", scope = StrucDocContent.class)
    public JAXBElement<StrucDocFootnote> createStrucDocContentFootnote(StrucDocFootnote value) {
        return new JAXBElement<>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnoteRef", scope = StrucDocContent.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocContentFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "renderMultiMedia", scope = StrucDocContent.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocContentRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocContent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "linkHtml", scope = StrucDocCaption.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocCaptionLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocCaption.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sub", scope = StrucDocCaption.class)
    public JAXBElement<StrucDocSub> createStrucDocCaptionSub(StrucDocSub value) {
        return new JAXBElement<>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocCaption.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sup", scope = StrucDocCaption.class)
    public JAXBElement<StrucDocSup> createStrucDocCaptionSup(StrucDocSup value) {
        return new JAXBElement<>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocCaption.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnote", scope = StrucDocCaption.class)
    public JAXBElement<StrucDocFootnote> createStrucDocCaptionFootnote(StrucDocFootnote value) {
        return new JAXBElement<>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocCaption.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnoteRef", scope = StrucDocCaption.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocCaptionFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocCaption.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTitleContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTitleContent }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "content", scope = StrucDocTitle.class)
    public JAXBElement<StrucDocTitleContent> createStrucDocTitleContent(StrucDocTitleContent value) {
        return new JAXBElement<>(_StrucDocThContent_QNAME, StrucDocTitleContent.class, StrucDocTitle.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sub", scope = StrucDocTitle.class)
    public JAXBElement<StrucDocSub> createStrucDocTitleSub(StrucDocSub value) {
        return new JAXBElement<>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocTitle.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sup", scope = StrucDocTitle.class)
    public JAXBElement<StrucDocSup> createStrucDocTitleSup(StrucDocSup value) {
        return new JAXBElement<>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocTitle.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "br", scope = StrucDocTitle.class)
    public JAXBElement<StrucDocBr> createStrucDocTitleBr(StrucDocBr value) {
        return new JAXBElement<>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocTitle.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTitleFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTitleFootnote }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnote", scope = StrucDocTitle.class)
    public JAXBElement<StrucDocTitleFootnote> createStrucDocTitleFootnote(StrucDocTitleFootnote value) {
        return new JAXBElement<>(_StrucDocThFootnote_QNAME, StrucDocTitleFootnote.class, StrucDocTitle.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnoteRef", scope = StrucDocTitle.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocTitleFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocTitle.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocContent }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "content", scope = StrucDocText.class)
    public JAXBElement<StrucDocContent> createStrucDocTextContent(StrucDocContent value) {
        return new JAXBElement<>(_StrucDocThContent_QNAME, StrucDocContent.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocLinkHtml }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "linkHtml", scope = StrucDocText.class)
    public JAXBElement<StrucDocLinkHtml> createStrucDocTextLinkHtml(StrucDocLinkHtml value) {
        return new JAXBElement<>(_StrucDocThLinkHtml_QNAME, StrucDocLinkHtml.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSub }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sub", scope = StrucDocText.class)
    public JAXBElement<StrucDocSub> createStrucDocTextSub(StrucDocSub value) {
        return new JAXBElement<>(_StrucDocThSub_QNAME, StrucDocSub.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocSup }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "sup", scope = StrucDocText.class)
    public JAXBElement<StrucDocSup> createStrucDocTextSup(StrucDocSup value) {
        return new JAXBElement<>(_StrucDocThSup_QNAME, StrucDocSup.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocBr }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "br", scope = StrucDocText.class)
    public JAXBElement<StrucDocBr> createStrucDocTextBr(StrucDocBr value) {
        return new JAXBElement<>(_StrucDocThBr_QNAME, StrucDocBr.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnote }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnote", scope = StrucDocText.class)
    public JAXBElement<StrucDocFootnote> createStrucDocTextFootnote(StrucDocFootnote value) {
        return new JAXBElement<>(_StrucDocThFootnote_QNAME, StrucDocFootnote.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocFootnoteRef }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "footnoteRef", scope = StrucDocText.class)
    public JAXBElement<StrucDocFootnoteRef> createStrucDocTextFootnoteRef(StrucDocFootnoteRef value) {
        return new JAXBElement<>(_StrucDocThFootnoteRef_QNAME, StrucDocFootnoteRef.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocRenderMultiMedia }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "renderMultiMedia", scope = StrucDocText.class)
    public JAXBElement<StrucDocRenderMultiMedia> createStrucDocTextRenderMultiMedia(StrucDocRenderMultiMedia value) {
        return new JAXBElement<>(_StrucDocThRenderMultiMedia_QNAME, StrucDocRenderMultiMedia.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocParagraph }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "paragraph", scope = StrucDocText.class)
    public JAXBElement<StrucDocParagraph> createStrucDocTextParagraph(StrucDocParagraph value) {
        return new JAXBElement<>(_StrucDocTdParagraph_QNAME, StrucDocParagraph.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocList }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "list", scope = StrucDocText.class)
    public JAXBElement<StrucDocList> createStrucDocTextList(StrucDocList value) {
        return new JAXBElement<>(_StrucDocTdList_QNAME, StrucDocList.class, StrucDocText.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrucDocTable }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StrucDocTable }{@code >}
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "table", scope = StrucDocText.class)
    public JAXBElement<StrucDocTable> createStrucDocTextTable(StrucDocTable value) {
        return new JAXBElement<>(_StrucDocItemTable_QNAME, StrucDocTable.class, StrucDocText.class, value);
    }


    /**
     * List of custom built JAXB elements that weren't pre-generated. These are being used in unit tests to test specific
     * deeply nested elements.
     *
     */
    @XmlElementDecl(namespace = NAMESPACEURI, name = "code")
    public JAXBElement<CD> createCode(CD value) {
        return new JAXBElement<CD>(_CD_QNAME, CD.class, null, value);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "ehrComposition")
    public JAXBElement<RCMRMT030101UKEhrComposition> createEhrComposition(RCMRMT030101UKEhrComposition value) {
        return new JAXBElement<>(_EHR_COMPOSITION_QNAME, RCMRMT030101UKEhrComposition.class, null, value);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "LinkSet")
    public JAXBElement<RCMRMT030101UKLinkSet> createLinkset(RCMRMT030101UKLinkSet value) {
        return new JAXBElement<>(_LINKSET_QNAME, RCMRMT030101UKLinkSet.class, null, value);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "ObservationStatement")
    public JAXBElement<RCMRMT030101UKObservationStatement> createObservationStatement(RCMRMT030101UKObservationStatement value) {
        return new JAXBElement<>(_OBSERVATION_STATEMENT_QNAME, RCMRMT030101UKObservationStatement.class, null, value);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "agentDirectory")
    public JAXBElement<RCMRMT030101UKAgentDirectory> createAgentDirectory(RCMRMT030101UKAgentDirectory value) {
        return new JAXBElement<>(_AGENT_DIRECTORY_QNAME, RCMRMT030101UKAgentDirectory.class, null, value);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "patient")
    public JAXBElement<RCMRMT030101UK04Patient> createPatient(RCMRMT030101UK04Patient patient) {
        return new JAXBElement<>(_PATIENT_QNAME, RCMRMT030101UK04Patient.class, null, patient);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "addr")
    public JAXBElement<AD> createAddress(AD address) {
        return new JAXBElement<AD>(_ADDRESS_QNAME, AD.class, null, address);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "telecom")
    public JAXBElement<TEL> createTelecom(TEL telecom) {
        return new JAXBElement<TEL>(_TELECOM_QNAME, TEL.class, null, telecom);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "ehrSupplyAuthorise")
    public JAXBElement<RCMRMT030101UKAuthorise> createTelecom(RCMRMT030101UKAuthorise supplyAuthorise) {
        return new JAXBElement<>(_EHR_SUPPLY_AUTHORISE, RCMRMT030101UKAuthorise.class, null, supplyAuthorise);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "MedicationStatement")
    public JAXBElement<RCMRMT030101UKMedicationStatement> createMedicationStatement(RCMRMT030101UKMedicationStatement medicationStatement) {
        return new JAXBElement<>(_MEDICATION_STATEMENT, RCMRMT030101UKMedicationStatement.class, null, medicationStatement);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "consumable")
    public JAXBElement<RCMRMT030101UKConsumable> createConsumable(RCMRMT030101UKConsumable consumable) {
        return new JAXBElement<>(_CONSUMABLE, RCMRMT030101UKConsumable.class, null, consumable);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "CompoundStatement")
    public JAXBElement<RCMRMT030101UKCompoundStatement> createCompoundStatement(RCMRMT030101UKCompoundStatement compoundStatement) {
        return new JAXBElement<>(_COMPOUND_STATEMENT, RCMRMT030101UKCompoundStatement.class, null, compoundStatement);
    }

    @XmlElementDecl(namespace = NAMESPACEURI, name = "NarrativeStatement")
    public JAXBElement<RCMRMT030101UKNarrativeStatement> createNarrativeStatement(RCMRMT030101UKNarrativeStatement narrativeStatement) {
        return new JAXBElement<>(_NARRATIVE_STATEMENT_QNAME, RCMRMT030101UKNarrativeStatement.class, null, narrativeStatement);
    }
}
