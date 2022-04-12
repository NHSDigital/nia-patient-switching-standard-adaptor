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

    public void addAttachmentLog(
        String mid,
        String filename,
        Integer patient_migration_req_id,
        Boolean uploaded,
        Integer order_num
    ) {
        patientAttachmentLogDao.addAttachmentLog(
            mid,
            filename,
            patient_migration_req_id,
            uploaded,
            order_num
        );
        LOGGER.debug("Created migration log mid=[{}] for patient migration request id=[{}]", mid, patient_migration_req_id);
    }

    public void updateAttachmentLog(
        String mid,
        String parent_mid,
        String content_type,
        Boolean compressed,
        Boolean large_attachment,
        Boolean base64,
        Boolean skeleton,
        Boolean uploaded,
        Integer length_num,
        Integer order_num
    ) {
        patientAttachmentLogDao.updateAttachmentLog(
            mid,
            parent_mid,
            content_type,
            compressed,
            large_attachment,
            base64,
            skeleton,
            uploaded,
            length_num,
            order_num
        );
        LOGGER.debug("Updated migration log mid=[{}]", mid);
    }

    public void deleteAttachmentLog(
        String file_name
    ) {
        patientAttachmentLogDao.deleteAttachmentLog(
            file_name
        );
        LOGGER.debug("Deleted flag set on migration log filename=[{}] ", file_name);

    }
}
