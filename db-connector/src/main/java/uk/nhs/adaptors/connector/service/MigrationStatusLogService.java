package uk.nhs.adaptors.connector.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatus;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class MigrationStatusLogService {
    private final PatientMigrationRequestDao patientMigrationRequestDao;
    private final MigrationStatusLogDao migrationStatusLogDao;
    private final DateUtils dateUtils;

    public void addMigrationStatusLog(MigrationStatus migrationStatus, String nhsNumber) {
        int migrationRequestId = patientMigrationRequestDao.getMigrationRequestId(nhsNumber);
        migrationStatusLogDao.addMigrationStatusLog(
            migrationStatus,
            dateUtils.getCurrentOffsetDateTime(),
            migrationRequestId
        );
        LOGGER.debug("Changed RequestStatus of PatientMigrationRequest with id=[{}] to [{}]", migrationRequestId, migrationStatus.name());
    }
}
