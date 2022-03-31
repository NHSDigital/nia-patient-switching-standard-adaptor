package uk.nhs.adaptors.pss.translator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.toHl7Format;

import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.pss.translator.model.ApplicationAcknowledgmentData;

@ExtendWith(MockitoExtension.class)
public class ApplicationAcknowledgementMessageServiceTest {

    private static final String MESSAGE_ID = "123-ABC";
    private static final String MESSAGE_REF = "8910-XYZ";
    private static final String TEST_FROM_ASID = "TEST_FROM_ASID";
    private static final String TEST_TO_ASID = "TEST_TO_ASID";
    private static final String TEST_TO_ODS = "TEST_TO_ODS";
    private static final String TEST_CONVERSATION_ID = "abcd12345";
    private static final String NACK_CODE = "TEST_NACK_CODE";

    @Mock
    private DateUtils dateUtils;

    @Mock
    private IdGeneratorService idGeneratorService;

    @InjectMocks
    private ApplicationAcknowledgementMessageService messageService;

    private ApplicationAcknowledgmentData messageData;

    @BeforeEach
    public void setup() {
        Instant instant = Instant.now();
        when(dateUtils.getCurrentInstant()).thenReturn(instant);
        when(idGeneratorService.generateUuid()).thenReturn(MESSAGE_ID);

        messageData = ApplicationAcknowledgmentData.builder()
            .conversationId(TEST_CONVERSATION_ID)
            .toOdsCode(TEST_TO_ODS)
            .nackCode(NACK_CODE)
            .messageRef(MESSAGE_REF)
            .fromAsid(TEST_FROM_ASID)
            .toAsid(TEST_TO_ASID)
            .build();
    }

    @Test
    public void whenBuildNackMessage_withValidTestData_thenNackCodeIsSetCorrectly() {
        String nackMessage = messageService.buildNackMessage(messageData);

        assertTrue(nackMessage.contains(NACK_CODE));
    }

    @Test
    public void whenBuildNackMessage_withValidTestData_thenMessageIdIsSetCorrectly() {
        String nackMessage = messageService.buildNackMessage(messageData);

        assertTrue(nackMessage.contains(MESSAGE_ID));
    }

    @Test
    public void whenBuildNackMessage_withValidTestData_thenMessageRefIsSetCorrectly() {
        String nackMessage = messageService.buildNackMessage(messageData);

        assertTrue(nackMessage.contains(MESSAGE_REF));
    }

    @Test
    public void whenBuildNackMessage_withValidTestData_thenToAsidIsSetCorrectly() {
        String nackMessage = messageService.buildNackMessage(messageData);

        assertTrue(nackMessage.contains(TEST_TO_ASID));
    }

    @Test
    public void whenBuildNackMessage_withValidTestData_thenFromAsidIsSetCorrectly() {
        String nackMessage = messageService.buildNackMessage(messageData);

        assertTrue(nackMessage.contains(TEST_FROM_ASID));
    }

    @Test
    public void whenBuildNackMessage_withValidTestData_thenCreationTimeIsSetCorrectly() {
        Instant instant = Instant.now();
        when(dateUtils.getCurrentInstant()).thenReturn(instant);

        String nackMessage = messageService.buildNackMessage(messageData);

        assertTrue(nackMessage.contains(toHl7Format(instant)));
    }

    @Test
    public void whenBuildNackMessage_withNackCodePresent_thenTypeCodeSetCorrectly() {
        String nackMessage = messageService.buildNackMessage(messageData);

        assertTrue(nackMessage.contains("typeCode=\"AE\""));
        assertFalse(nackMessage.contains("typeCode=\"AA\""));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void whenBuildNackMessage_withNackCodeMissing_thenThrowIllegalArgumentException() {

        ApplicationAcknowledgmentData missingNackData = ApplicationAcknowledgmentData.builder()
            .conversationId(TEST_CONVERSATION_ID)
            .toOdsCode(TEST_TO_ODS)
            .messageRef(MESSAGE_REF)
            .fromAsid(TEST_FROM_ASID)
            .toAsid(TEST_TO_ASID)
            .build();

        assertThrowsExactly(IllegalArgumentException.class, () -> messageService.buildNackMessage(missingNackData));
    }

    @Test
    public void whenBuildNackMessage_withNackCodePresent_thenReasonElementIncluded() throws ParserConfigurationException, IOException,
        SAXException {
        String nackMessage = messageService.buildNackMessage(messageData);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document messageXml = db.parse(new InputSource(new StringReader(nackMessage)));
        NodeList nodes = messageXml.getElementsByTagName("reason");

        assertTrue(nodes.getLength() > 0);
    }

    @Test
    public void whenBuildNackMessage_withNackCodePresent_thenReasonHasCorrectAttribute() throws ParserConfigurationException, IOException, SAXException {
        String nackMessage = messageService.buildNackMessage(messageData);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document messageXml = db.parse(new InputSource(new StringReader(nackMessage)));
        NodeList nodes = messageXml.getElementsByTagName("reason");
        String reasonAttribute = nodes.item(0).getAttributes().item(0).toString();

        assertEquals("typeCode=\"RSON\"", reasonAttribute);
    }
}
