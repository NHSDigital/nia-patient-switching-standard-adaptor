package uk.nhs.adaptors.pss.translator.service;

import com.github.mustachejava.Mustache;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.util.template.parameter.ApplicationAcknowledgmentParams;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.toHl7Format;
import static uk.nhs.adaptors.pss.translator.util.template.TemplateUtil.fillTemplate;
import static uk.nhs.adaptors.pss.translator.util.template.TemplateUtil.loadTemplate;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ACKMessageService {
    private static final Mustache ACK_MESSAGE_TEMPLATE =
            loadTemplate("MCCI_IN010000UK13ApplicationAcknowledgment.mustache");

    private final DateUtils dateUtils;
    private final IdGeneratorService idGeneratorService;

    public String buildMessage(ACKMessageData messageData) {
        LOGGER.debug("Building NACK message for message = [{}]", messageData.getMessageRef());

        ApplicationAcknowledgmentParams params = ApplicationAcknowledgmentParams.builder()
                .messageId(idGeneratorService.generateUuid())
                .creationTime(toHl7Format(dateUtils.getCurrentInstant()))
                .toAsid(messageData.getToAsid())
                .fromAsid(messageData.getFromAsid())
                .messageRef(messageData.getMessageRef())
                .ackType(messageData.getAckType())
                .build();

        return fillTemplate(ACK_MESSAGE_TEMPLATE, params);
    }
}
