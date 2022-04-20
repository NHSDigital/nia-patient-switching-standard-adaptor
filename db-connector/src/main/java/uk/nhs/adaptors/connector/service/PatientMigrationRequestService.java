package uk.nhs.adaptors.connector.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PatientMigrationRequestService {
    private final MigrationStatusLogDao migrationStatusLogDao;
    private final PatientMigrationRequestDao migrationRequestDao;

    public List<PatientMigrationRequest> getMigrationRequestByCurrentMigrationStatus(MigrationStatus migrationStatus) {
        List<Integer> ids = migrationStatusLogDao.getMigrationRequestIdsByMigrationStatus(migrationStatus);

        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        return migrationRequestDao.getMigrationRequestByIdIn(ids);
    }
}
