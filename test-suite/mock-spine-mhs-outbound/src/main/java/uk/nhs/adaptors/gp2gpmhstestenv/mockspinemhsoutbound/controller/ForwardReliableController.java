package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.controller;

import static uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.MessageHeaders.CONTENT_TYPE;
import static uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.MessageHeaders.CORRELATION_ID;
import static uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.MessageHeaders.INTERACTION_ID;
import static uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.MessageHeaders.MESSAGE_ID;
import static uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service.ContentTypeService.MHS_OUTBOUND_CONTENT_TYPE;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.OutboundMessage;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.SuccessTemplateParams;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service.ContentTypeService;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service.SpineResponseService;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service.JournalService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(path = "/reliablemessaging/forwardreliable")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ForwardReliableController {

    private SpineResponseService responseService;
    private ContentTypeService contentTypeService;
    private JournalService journalService;

    @GetMapping
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("Hello World");
    }

    @PostMapping(consumes = MediaType.MULTIPART_RELATED_VALUE)
    public ResponseEntity<String> mockSpineEndpoint(
        @RequestBody String body,
        @RequestHeader Map<String, String> headers,
        @RequestHeader(value = MESSAGE_ID) String messageId,
        @RequestHeader(value = CORRELATION_ID) String correlationId,
        @RequestHeader(value = INTERACTION_ID) String interactionId
    ) {

        Optional<String> contentTypeOptional = Optional.ofNullable(headers.get(CONTENT_TYPE));
        contentTypeOptional.ifPresent(contentType -> {
            if (contentTypeService.hasAlteredContentType(messageId)) {
                headers.put(CONTENT_TYPE, MHS_OUTBOUND_CONTENT_TYPE);
            }
        });

        OutboundMessage message = OutboundMessage.builder()
            .headers(headers)
            .body(body)
            .build();

        journalService.postJournalEntry(correlationId, message);

        var params = SuccessTemplateParams.builder()
            .conversationId(correlationId)
            .interactionId(interactionId)
            .refToMessageId(messageId)
            .messageId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now().toString())
            .build();

        return ResponseEntity.accepted().body(responseService.fillSuccessTemplate(params));
    }
}
