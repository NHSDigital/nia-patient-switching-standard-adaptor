package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.controller;

import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.OutboundMessage;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service.JournalService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;



/*
    Retrieves the data from JournalService Static map with messages.
    we use it to query the messages sent from MHS outbout to our service
 */
@RestController
@RequestMapping(path = "/retrieve")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class RetrieveSpineController {

    private JournalService journalService;

    @GetMapping
    public ResponseEntity<Map<String, List<OutboundMessage>>> getRecords() {

        return ResponseEntity.ok(journalService.getRequestJournal());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<List<OutboundMessage>> getRecordById(@PathVariable String id) {

        Optional<List<OutboundMessage>> messagesOptional = journalService.getRequestJournalById(id);

        return messagesOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping(value = "/{id}/{messageId}")
    public ResponseEntity<List<OutboundMessage>> getRecordByMessageId(@PathVariable String id, @PathVariable String messageId) {

        Optional<List<OutboundMessage>> messagesOptional = journalService.getRequestJournalById(id);

        return messagesOptional
                .map(
/*                        outboundMessageList-> ResponseEntity.ok(
                                outboundMessageList
                                        .stream()
                                        .filter(
                                                outboundMessage -> outboundMessage
                                                        .getHeaders()
                                                        .get("message-id")
                                                        .equals(messageId)
                                        )
                                        .toList()
                        )*/
                        ResponseEntity::ok
                )
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
