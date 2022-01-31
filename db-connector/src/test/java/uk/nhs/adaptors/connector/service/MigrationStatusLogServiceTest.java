package uk.nhs.adaptors.connector.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.util.DateUtils;

@ExtendWith(MockitoExtension.class)
public class MigrationStatusLogServiceTest {

    @Mock
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Mock
    private MigrationStatusLogDao migrationStatusLogDao;

    @Mock
    private DateUtils dateUtils;

    @InjectMocks
    private MigrationStatusLogService migrationStatusLogService;

    @Test
    public void testAddMigrationStatusLog() {
        String nhsNumber = "123456";
        int migrationRequestId = 10;
        OffsetDateTime now = OffsetDateTime.now();

        when(patientMigrationRequestDao.getMigrationRequestId(nhsNumber)).thenReturn(migrationRequestId);
        when(dateUtils.getCurrentOffsetDateTime()).thenReturn(now);

        migrationStatusLogService.addMigrationStatusLog(MigrationStatus.MIGRATION_COMPLETED, nhsNumber);

        verify(migrationStatusLogDao).addMigrationStatusLog(MigrationStatus.MIGRATION_COMPLETED, now, migrationRequestId);
    }
}
