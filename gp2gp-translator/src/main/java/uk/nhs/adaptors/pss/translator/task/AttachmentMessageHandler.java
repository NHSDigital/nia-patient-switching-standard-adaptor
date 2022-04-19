package uk.nhs.adaptors.pss.translator.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.v3.COPCIN000001UK01Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;

import javax.xml.bind.JAXBException;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AttachmentMessageHandler {

    private final SendACKMessageHandler sendACKMessageHandler;

    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException {

        COPCIN000001UK01Message payload = unmarshallString(inboundMessage.getPayload(), COPCIN000001UK01Message.class);
        sendAckMessage(payload, conversationId);

    }

    public boolean sendAckMessage(COPCIN000001UK01Message payload, String conversationId) {

        LOGGER.debug("Sending ACK message for message with Conversation ID: [{}]", conversationId);

        return sendACKMessageHandler.prepareAndSendMessage(prepareAckMessageData(
                payload,
                conversationId
        ));
    }

    private ACKMessageData prepareAckMessageData(COPCIN000001UK01Message payload,
                                                 String conversationId) {

        String toOdsCode = parseToOdsCode(payload);
        String messageRef = parseMessageRef(payload);
        String toAsid = parseToAsid(payload);
        String fromAsid = parseFromAsid(payload);

        return ACKMessageData.builder()
                .conversationId(conversationId)
                .toOdsCode(toOdsCode)
                .messageRef(messageRef)
                .toAsid(toAsid)
                .fromAsid(fromAsid)
                .build();
    }

    private String parseFromAsid(COPCIN000001UK01Message payload) {
        return payload.getCommunicationFunctionRcv()
                .get(0)
                .getDevice()
                .getId()
                .get(0)
                .getExtension();
    }

    private String parseToAsid(COPCIN000001UK01Message payload) {
        return payload.getCommunicationFunctionSnd()
                .getDevice()
                .getId()
                .get(0)
                .getExtension();
    }

    private String parseToOdsCode(COPCIN000001UK01Message payload) {
        return payload.getControlActEvent()
                .getSubject()
                .getEhrExtract()
                .getAuthor()
                .getAgentOrgSDS()
                .getAgentOrganizationSDS()
                .getId()
                .getExtension();
    }

    private String parseMessageRef(COPCIN000001UK01Message payload) {
        return payload.getId().getRoot();
    }
}
