package uk.nhs.adaptors.pss.translator.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.toHl7Format;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
public class ACKMessageServiceTest {

    private static final String MESSAGE_ID = "123-ABC";
    private static final String MESSAGE_REF = "8910-XYZ";
    private static final String TEST_FROM_ASID = "TEST_FROM_ASID";
    private static final String TEST_TO_ASID = "TEST_TO_ASID";

    @Mock
    private DateUtils dateUtils;

    @Mock
    private IdGeneratorService idGeneratorService;

    @InjectMocks
    private ACKMessageService ackMessageService;

    @Test
    public void whenBuildNACKMessageThenTemplateIsFilled() {
        Instant instant = Instant.now();
        when(dateUtils.getCurrentInstant()).thenReturn(instant);
        when(idGeneratorService.generateUuid()).thenReturn(MESSAGE_ID);

        ACKMessageData messageData = ACKMessageData.builder()
                .messageRef(MESSAGE_REF)
                .fromAsid(TEST_FROM_ASID)
                .toAsid(TEST_TO_ASID)
                .build();

        String nackMessage = ackMessageService.buildNACKMessage(messageData);

        assertTrue(nackMessage.contains(MESSAGE_ID));
        assertTrue(nackMessage.contains(MESSAGE_REF));
        assertTrue(nackMessage.contains(TEST_FROM_ASID));
        assertTrue(nackMessage.contains(TEST_TO_ASID));
        assertTrue(nackMessage.contains(toHl7Format(instant)));

    }
}
