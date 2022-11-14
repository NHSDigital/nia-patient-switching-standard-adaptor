package uk.nhs.adaptors.pss.translator.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.common.enums.ConfirmationResponse;
import uk.nhs.adaptors.common.model.AcknowledgeRecordMessage;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.task.SendACKMessageHandler;
import uk.nhs.adaptors.pss.translator.task.SendNACKMessageHandler;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AcknowledgeRecordService {

    private final SendNACKMessageHandler nackMessageHandler;
    private final SendACKMessageHandler ackMessageHandler;

    public boolean prepareAndSendAcknowledgementMessage(AcknowledgeRecordMessage message) {

        //TODO: Get Components from original message

        if (message.getConfirmationResponse() == ConfirmationResponse.ACCEPTED) {
            var ackMessageData = ACKMessageData.builder()
                    .conversationId(message.getConversationId())
                    .toAsid("")
                    .fromAsid("")
                    .toOdsCode("")
                    .build();

            return ackMessageHandler.prepareAndSendMessage(ackMessageData);
        }

        var nackMessageData = NACKMessageData.builder()
                .conversationId(message.getConversationId())
                .toAsid("")
                .fromAsid("")
                .toOdsCode("")
                .nackCode("")
                .build();

        return nackMessageHandler.prepareAndSendMessage(nackMessageData);
    }
}
