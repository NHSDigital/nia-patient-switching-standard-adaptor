package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.ReliabilityResponse;

@RestController
@RequestMapping("/reliability")
public class ReliabilityController {

    @GetMapping
    public ResponseEntity<ReliabilityResponse> reliabilityLookup(@RequestHeader(name = "Correlation-Id") String conversationId,
        @RequestParam("org-code") String odsCode,
        @RequestParam(name = "service-id") String interactionId) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Correlation-Id", conversationId);

        ReliabilityResponse response = ReliabilityResponse.builder()
            .nhsMHSSyncReplyMode("MSHSignalsOnly")
            .nhsMHSRetries("3")
            .nhsMHSPersistDuration("PT3H")
            .nhsMHSAckRequested("MSHSignalsOnly")
            .nhsMHSDuplicateElimination("always")
            .nhsMHSRetryInterval("PT0.5S")
            .build();

        return ResponseEntity
            .ok()
            .headers(headers)
            .body(response);
    }
}
