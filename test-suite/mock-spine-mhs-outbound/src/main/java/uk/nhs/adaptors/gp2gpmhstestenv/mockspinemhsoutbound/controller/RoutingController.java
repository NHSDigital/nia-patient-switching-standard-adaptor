package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.RoutingResponse;

@RestController
@RequestMapping("/routing")
public class RoutingController {

    @GetMapping
    public ResponseEntity<RoutingResponse> routeLookup(@RequestHeader(name = "Correlation-Id") String conversationId,
        @RequestParam(name = "org-code") String odsCode,
        @RequestParam(name = "service-id") String interactionId) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Correlation-Id", conversationId);

        RoutingResponse response = RoutingResponse.builder()
            .nhsMHSEndPoint("https://localhost:8443")
            .nhsMHSPartyKey("AP4RTY-K33Y")
            .nhsMhsCPAId("S918999410559")
            .uniqueIdentifier("123456789")
            .build();

        return ResponseEntity
            .ok()
            .headers(headers)
            .body(response);
    }
}
