package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.controller;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping(path = "/patient-migration-request/{id}")
    public ResponseEntity<List<MigrationStatusLog>> findReceivedAcknowledgement(@PathVariable String id) {
        Optional<List<MigrationStatusLog>> acknowledgement =
                patientMigrationRequestService.findReceivedAcknowledgmentForConversationId(id);


        return acknowledgement.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
