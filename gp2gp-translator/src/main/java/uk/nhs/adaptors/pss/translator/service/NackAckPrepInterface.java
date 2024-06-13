package uk.nhs.adaptors.pss.translator.service;

import org.hl7.v3.COPCIN000001UK01Message;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.hl7.v3.RCMRIN030000UKMessage;
import org.springframework.retry.annotation.Retryable;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKReason;

public interface NackAckPrepInterface {

    @Retryable
    boolean sendAckMessage(RCMRIN030000UK06Message payload, String conversationId);

    @Retryable
    boolean sendAckMessage(COPCIN000001UK01Message payload, String conversationId, String losingPracticeOdsCode);

    NACKMessageData prepareNackMessageData(NACKReason reason, RCMRIN030000UKMessage payload,
                                           String conversationId);

    NACKMessageData prepareNackMessageData(NACKReason reason, COPCIN000001UK01Message payload,
                                           String conversationId);

    boolean sendNackMessage(NACKReason reason, RCMRIN030000UKMessage payload, String conversationId);

    boolean sendNackMessage(NACKReason reason, COPCIN000001UK01Message payload, String conversationId);
}