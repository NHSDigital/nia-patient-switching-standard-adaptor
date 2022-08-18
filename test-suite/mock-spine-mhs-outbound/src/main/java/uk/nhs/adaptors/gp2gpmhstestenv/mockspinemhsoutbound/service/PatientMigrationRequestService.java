package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.MigrationStatusLog;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.PatientMigrationRequest;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.repository.PatientMigrationRequestRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PatientMigrationRequestService
{
    private PatientMigrationRequestRepository patientMigrationRequestRepository;


    public Optional<List<MigrationStatusLog>> findReceivedAcknowledgmentForConversationId(String conversationId) {
        Optional<PatientMigrationRequest> extractStatus = Optional.ofNullable(patientMigrationRequestRepository.findPatientMigrationRequestByConversationId(conversationId));

        return extractStatus.map(PatientMigrationRequest::getMigrationStatusLog);
    }
}
