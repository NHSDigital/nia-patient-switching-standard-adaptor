package uk.nhs.adaptors.connector.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.dao.PatientAttachmentLogDao;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))

public class PatientAttachmentLogService {
    private final PatientAttachmentLogDao patientAttachmentLogDao;

    public void addAttachmentLog(String mid, String filename, Boolean uploaded, String patient_req_link, Integer patient_migration_req_id, Integer order_num) {
        patientAttachmentLogDao.addAttachmentLog(
            mid,
            filename,
            uploaded,
            patient_req_link,
            patient_migration_req_id,
            order_num
        );
        LOGGER.debug("Created migration log mid=[{}] for patient migration request id=[{}]", mid, patient_migration_req_id);
    }



    public void updateAttachmentLog(String mid, String filename, Boolean uploaded) {
        patientAttachmentLogDao.updateAttachmentLog(
            mid,
            filename,
            uploaded
        );
    }
}
