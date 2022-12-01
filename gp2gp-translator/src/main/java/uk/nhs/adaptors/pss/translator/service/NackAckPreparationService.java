package uk.nhs.adaptors.pss.translator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.v3.COPCIN000001UK01Message;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKReason;
import uk.nhs.adaptors.pss.translator.task.SendACKMessageHandler;
import uk.nhs.adaptors.pss.translator.task.SendNACKMessageHandler;
import uk.nhs.adaptors.pss.translator.util.XmlParseUtilService;

import static uk.nhs.adaptors.connector.model.MigrationStatus.COPC_ACKNOWLEDGED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_REQUEST_ACKNOWLEDGED;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NackAckPreparationService {

    private final SendNACKMessageHandler sendNACKMessageHandler;
    private final SendACKMessageHandler sendACKMessageHandler;
    private final MigrationStatusLogService migrationStatusLogService;

    public boolean sendAckMessage(RCMRIN030000UK06Message payload, String conversationId) {

        LOGGER.debug("Sending ACK message for message with Conversation ID: [{}]", conversationId);

        migrationStatusLogService.
                addMigrationStatusLog(EHR_EXTRACT_REQUEST_ACKNOWLEDGED, conversationId, null);

        return sendACKMessageHandler.prepareAndSendMessage(prepareAckMessageData(
                payload,
                conversationId
        ));
    }

    public boolean sendAckMessage(COPCIN000001UK01Message payload, String conversationId, String losingPracticeOdsCode) {

        LOGGER.debug("Sending ACK message for message with Conversation ID: [{}]", conversationId);

        migrationStatusLogService.
                addMigrationStatusLog(COPC_ACKNOWLEDGED, conversationId, null);

        return sendACKMessageHandler.prepareAndSendMessage(prepareAckMessageData(
                payload,
                conversationId,
                losingPracticeOdsCode
        ));
    }

    private ACKMessageData prepareAckMessageData(RCMRIN030000UK06Message payload,
                                                String conversationId) {

        String toOdsCode = XmlParseUtilService.parseToOdsCode(payload);
        String messageRef = XmlParseUtilService.parseMessageRef(payload);
        String toAsid = XmlParseUtilService.parseToAsid(payload);
        String fromAsid = XmlParseUtilService.parseFromAsid(payload);

        return ACKMessageData.builder()
                .conversationId(conversationId)
                .toOdsCode(toOdsCode)
                .messageRef(messageRef)
                .toAsid(toAsid)
                .fromAsid(fromAsid)
                .build();
    }

    private ACKMessageData prepareAckMessageData(COPCIN000001UK01Message payload,
                                                String conversationId, String losingPracticeOdsCode) {

        String messageRef = XmlParseUtilService.parseMessageRef(payload);
        String toAsid = XmlParseUtilService.parseToAsid(payload);
        String fromAsid = XmlParseUtilService.parseFromAsid(payload);

        return ACKMessageData.builder()
                .conversationId(conversationId)
                .toOdsCode(losingPracticeOdsCode)
                .messageRef(messageRef)
                .toAsid(toAsid)
                .fromAsid(fromAsid)
                .build();
    }


    public NACKMessageData prepareNackMessageData(NACKReason reason, RCMRIN030000UK06Message payload,
                                                  String conversationId) {

        String toOdsCode = XmlParseUtilService.parseToOdsCode(payload);
        String messageRef = XmlParseUtilService.parseMessageRef(payload);
        String toAsid = XmlParseUtilService.parseToAsid(payload);
        String fromAsid = XmlParseUtilService.parseFromAsid(payload);
        String nackCode = reason.getCode();

        return NACKMessageData.builder()
                .conversationId(conversationId)
                .nackCode(nackCode)
                .toOdsCode(toOdsCode)
                .messageRef(messageRef)
                .toAsid(toAsid)
                .fromAsid(fromAsid)
                .build();
    }

    public NACKMessageData prepareNackMessageData(NACKReason reason, COPCIN000001UK01Message payload,
                                                   String conversationId) {

        String toOdsCode = XmlParseUtilService.parseToOdsCode(payload);
        String messageRef = XmlParseUtilService.parseMessageRef(payload);
        String toAsid = XmlParseUtilService.parseToAsid(payload);
        String fromAsid = XmlParseUtilService.parseFromAsid(payload);
        String nackCode = reason.getCode();

        return NACKMessageData.builder()
                .conversationId(conversationId)
                .nackCode(nackCode)
                .toOdsCode(toOdsCode)
                .messageRef(messageRef)
                .toAsid(toAsid)
                .fromAsid(fromAsid)
                .build();
    }

    public boolean sendNackMessage(NACKReason reason, RCMRIN030000UK06Message payload, String conversationId) {

        LOGGER.debug("Sending NACK message with acknowledgement code [{}] for message EHR Extract message [{}]", reason.getCode(),
                payload.getId().getRoot());

        migrationStatusLogService.addMigrationStatusLog(reason.getMigrationStatus(), conversationId, null);

        return sendNACKMessageHandler.prepareAndSendMessage(prepareNackMessageData(
                reason,
                payload,
                conversationId
        ));
    }

    public boolean sendNackMessage(NACKReason reason, COPCIN000001UK01Message payload, String conversationId) {

        LOGGER.debug("Sending NACK message with acknowledgement code [{}] for message EHR Extract message [{}]", reason.getCode(),
                payload.getId().getRoot());

        migrationStatusLogService.addMigrationStatusLog(reason.getMigrationStatus(), conversationId, null);

        return sendNACKMessageHandler.prepareAndSendMessage(prepareNackMessageData(
                reason,
                payload,
                conversationId
        ));
    }

}
