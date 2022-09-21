package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.controller;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.MigrationStatusLog;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.PatientMigrationRequest;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service.PatientMigrationRequestService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/pss-adaptor-db")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PatientMigrationRequestController
{
    private PatientMigrationRequestService patientMigrationRequestService;

    @GetMapping(path = "/patient-migration-request/{conversationId}")
    public ResponseEntity<PatientMigrationRequest> findPatientMigrationRequestByConversationId(
            @PathVariable String conversationId
    ) {
        Optional<PatientMigrationRequest> acknowledgement = patientMigrationRequestService.findPatientMigrationRequestForConversationId(conversationId);
        return acknowledgement.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
