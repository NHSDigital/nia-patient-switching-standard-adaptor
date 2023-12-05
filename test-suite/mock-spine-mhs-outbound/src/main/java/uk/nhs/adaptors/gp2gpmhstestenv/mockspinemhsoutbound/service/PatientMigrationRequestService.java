package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service;

import java.util.Comparator;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.MigrationStatusLog;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.PatientMigrationRequest;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.repository.PatientMigrationRequestRepository;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PatientMigrationRequestService {
    private PatientMigrationRequestRepository patientMigrationRequestRepository;


    public Optional<PatientMigrationRequest> findPatientMigrationRequestForConversationId(String conversationId) {
        var migrationRequestOptional = Optional.ofNullable(patientMigrationRequestRepository.findByConversationId(conversationId));

        if (migrationRequestOptional.isEmpty()) {
            return Optional.empty();
        }

        var migrationRequest = migrationRequestOptional.orElseThrow();

        migrationRequest.getMigrationStatusLog()
            .sort(Comparator.comparing(MigrationStatusLog::getDate));

        return Optional.of(migrationRequest);
    }
}
