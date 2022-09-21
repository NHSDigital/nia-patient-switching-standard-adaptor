package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.PatientMigrationRequest;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.repository.PatientMigrationRequestRepository;

import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PatientMigrationRequestService {
    private PatientMigrationRequestRepository patientMigrationRequestRepository;


    public Optional<PatientMigrationRequest> findPatientMigrationRequestForConversationId(String conversationId) {
        return Optional.ofNullable(patientMigrationRequestRepository.findByConversationId(conversationId));
    }
}
