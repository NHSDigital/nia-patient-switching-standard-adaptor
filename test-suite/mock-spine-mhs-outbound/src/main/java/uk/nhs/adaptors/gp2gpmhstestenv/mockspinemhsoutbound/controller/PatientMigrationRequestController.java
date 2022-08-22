package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.controller;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.MigrationStatusLog;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service.PatientMigrationRequestService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/pss-adaptor-db")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PatientMigrationRequestController
{
    private PatientMigrationRequestService patientMigrationRequestService;

    @GetMapping(path = "/migration-status-log/{conversationId}")
    public ResponseEntity<List<MigrationStatusLog>> findReceivedAcknowledgement(@PathVariable String conversationId, @RequestParam("migrationStatus") String migrationStatus) {
        Optional<List<MigrationStatusLog>> acknowledgement =
                patientMigrationRequestService.findReceivedAcknowledgmentForConversationId(conversationId);

        /*
            ToDo ask scott about the data he wants from the tables
            We need the Request Type but also the message ID
          */
        return acknowledgement.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
