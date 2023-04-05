package uk.nhs.adaptors.connector.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.common.enums.MigrationStatus;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PatientMigrationRequestService {
    private final PatientMigrationRequestDao migrationRequestDao;

    public List<PatientMigrationRequest> getMigrationRequestsByMigrationStatusIn(List<MigrationStatus> migrationStatusList) {
        return migrationRequestDao.getMigrationRequestsByLatestMigrationStatusIn(migrationStatusList);
    }

    public boolean hasMigrationRequest(String conversationId) {
        return migrationRequestDao.existsByConversationId(conversationId);
    }
}
