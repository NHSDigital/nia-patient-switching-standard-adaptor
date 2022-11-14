package uk.nhs.adaptors.pss.gpc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.adaptors.pss.gpc.service.AcknowledgeRecordService;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.CONFIRMATION_RESPONSE;
import static uk.nhs.adaptors.pss.gpc.controller.header.HttpHeaders.CONVERSATION_ID;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AcknowledgeRecordController {

    private final AcknowledgeRecordService acknowledgeRecordService;

    @PostMapping(
            path = "/$gpc.ack"
    )
    public ResponseEntity<String> acknowledgeRecord(
            @RequestHeader(CONFIRMATION_RESPONSE) @NotNull String confirmationResponse,
            @RequestHeader(CONVERSATION_ID) @NotNull String conversationId
            ) {
        LOGGER.info("Received migrated structured record acknowledgement");
        Map<String, String> headers = Map.of(
                CONFIRMATION_RESPONSE, confirmationResponse,
                CONVERSATION_ID, conversationId
        );

        var response = acknowledgeRecordService.handleAcknowledgeRecord(headers);

        return response
                ? new ResponseEntity<>(OK)
                : new ResponseEntity<>(INTERNAL_SERVER_ERROR);
    }
}
