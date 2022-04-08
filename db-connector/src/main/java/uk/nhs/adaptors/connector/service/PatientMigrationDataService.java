package uk.nhs.adaptors.connector.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.dao.PatientMigrationDataDao;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))

public class PatientMigrationDataService {
    private final PatientMigrationDataDao patientMigrationDataDao;

    public void addPatientData(String migrationRequestId) {
        System.out.println("CALLING PATIENT MIGRATION SERVICE");
        patientMigrationDataDao.addPatientData(
            migrationRequestId
        );
//        LOGGER.debug("Changed MigrationStatus of PatientMigrationRequest with id=[{}] to [{}]", migrationRequestId, migrationStatus.name());
    }

//    public void removePatientData() {
//        patientMigrationDataDao.removePatientData(
//            "test",
//            "test",
//            "test"
//        );
////        LOGGER.debug("Changed MigrationStatus of PatientMigrationRequest with id=[{}] to [{}]", migrationRequestId, migrationStatus.name());
//    }
//
//    public PatientMigrationData getPatientMigrationData() {
//        return patientMigrationDataDao.getPatientMigrationData("test");
//    }
//
//    public void updatePatientData() {
//        patientMigrationDataDao.updatePatientData(
//            "test",
//            "test",
//            "test"
//        );
//    }
}
