package uk.nhs.adaptors.pss.translator.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.toHl7Format;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;

@ExtendWith(MockitoExtension.class)
public class NACKMessageServiceTest {

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
    private NACKMessageService NACKMessageService;

    private NACKMessageData messageData;

    @BeforeEach
    public void setup() {
        Instant instant = Instant.now();
        when(dateUtils.getCurrentInstant()).thenReturn(instant);
        when(idGeneratorService.generateUuid()).thenReturn(MESSAGE_ID);

        messageData = NACKMessageData.builder()
            .conversationId(TEST_CONVERSATION_ID)
            .toOdsCode(TEST_TO_ODS)
            .nackCode(NACK_CODE)
            .messageRef(MESSAGE_REF)
            .fromAsid(TEST_FROM_ASID)
            .toAsid(TEST_TO_ASID)
            .build();
    }

    @Test
    public void whenBuildMessage_withValidTestData_thenNackCodeIsSetCorrectly() {
        String nackMessage = NACKMessageService.buildMessage(messageData);

        assertTrue(nackMessage.contains(NACK_CODE));
    }

    @Test
    public void whenBuildMessage_withValidTestData_thenMessageIdIsSetCorrectly() {
        String nackMessage = NACKMessageService.buildMessage(messageData);

        assertTrue(nackMessage.contains(MESSAGE_ID));
    }

    @Test
    public void whenBuildMessage_withValidTestData_thenMessageRefIsSetCorrectly() {
        String nackMessage = NACKMessageService.buildMessage(messageData);

        assertTrue(nackMessage.contains(MESSAGE_REF));
    }

    @Test
    public void whenBuildMessage_withValidTestData_thenToAsidIsSetCorrectly() {
        String nackMessage = NACKMessageService.buildMessage(messageData);

        assertTrue(nackMessage.contains(TEST_TO_ASID));
    }

    @Test
    public void whenBuildMessage_withValidTestData_thenFromAsidIsSetCorrectly() {
        String nackMessage = NACKMessageService.buildMessage(messageData);

        assertTrue(nackMessage.contains(TEST_FROM_ASID));
    }

    @Test
    public void whenBuildMessage_withValidTestData_thenCreationTimeIsSetCorrectly() {
        Instant instant = Instant.now();
        when(dateUtils.getCurrentInstant()).thenReturn(instant);

        String nackMessage = NACKMessageService.buildMessage(messageData);

        assertTrue(nackMessage.contains(toHl7Format(instant)));
    }

}
