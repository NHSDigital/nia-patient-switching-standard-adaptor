package uk.nhs.adaptors.pss.translator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.v3.RCMRIN030000UK06Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.common.enums.ConfirmationResponse;
import uk.nhs.adaptors.common.model.AcknowledgeRecordMessage;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.NACKReason;

import javax.xml.bind.JAXBException;
import java.util.Map;

import static uk.nhs.adaptors.common.enums.ConfirmationResponse.*;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.*;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

@Slf4j
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AcknowledgeRecordService {

    private final NackAckPreparationService preparationService;
    private final ObjectMapper objectMapper;

    private final static Map<ConfirmationResponse, NACKReason> reasons = Map.of(
            ABA_INCORRECT_PATIENT, ABA_EHR_EXTRACT_REJECTED_WRONG_PATIENT,
            NON_ABA_INCORRECT_PATIENT, NON_ABA_EHR_EXTRACT_REJECTED_WRONG_PATIENT,
            FAILED_TO_INTEGRATE, CLINICAL_SYSTEM_INTEGRATION_FAILURE,
            SUPPRESSED, ABA_EHR_EXTRACT_SUPPRESSED
    );

    public boolean prepareAndSendAcknowledgementMessage(AcknowledgeRecordMessage acknowledgeRecordMessage) {
        RCMRIN030000UK06Message message;

        try {
            message = parseOriginalMessage(acknowledgeRecordMessage);
        }
        catch(Exception e) {
            return false;
        }

        var conversationId = acknowledgeRecordMessage.getConversationId();

        if (acknowledgeRecordMessage.getConfirmationResponse() == ACCEPTED) {
            return preparationService.sendAckMessage(message, conversationId);
        }

        var nackReason = reasons.get(acknowledgeRecordMessage.getConfirmationResponse());

        return preparationService.sendNackMessage(nackReason, message, conversationId);
    }

    private RCMRIN030000UK06Message parseOriginalMessage(AcknowledgeRecordMessage message)
        throws JAXBException, JsonProcessingException {
        var payload = objectMapper.readValue(message.getOriginalMessage(), InboundMessage.class)
                .getPayload();

        return unmarshallString(payload, RCMRIN030000UK06Message.class);
    }

}
