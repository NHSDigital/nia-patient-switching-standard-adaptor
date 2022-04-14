package uk.nhs.adaptors.connector.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.dao.PatientAttachmentLogDao;
import uk.nhs.adaptors.connector.model.PatientAttachmentLog;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))

public class PatientAttachmentLogService {
    private final PatientAttachmentLogDao patientAttachmentLogDao;

    /**
     *
     * @param attachmentLogInput
     *
     * Requires a minimum of mid, filename and migration id
     *
     */
    public void addAttachmentLog(
        PatientAttachmentLog attachmentLogInput
    ) {
        var mid = attachmentLogInput.getMid();
        var filename = attachmentLogInput.getFilename();
        patientAttachmentLogDao.addAttachmentLog(
            mid,
            filename,
            attachmentLogInput.getParent_mid(),
            attachmentLogInput.getPatient_migration_req_id(),
            attachmentLogInput.getContent_type(),
            attachmentLogInput.getCompressed(),
            attachmentLogInput.getLarge_attachment(),
            attachmentLogInput.getBase64(),
            attachmentLogInput.getSkeleton(),
            attachmentLogInput.getUploaded(),
            attachmentLogInput.getOrder_num(),
            attachmentLogInput.getLength_num()
        );
        LOGGER.debug("Created migration log mid=[{}] for patient migration request id=[{}]", mid, filename);
    }

    /**
     *
     * @param mid
     * @param conversation_id
     * @return PatientAttachmentLog
     *
     * @description
     * Find an attachment log with an mid and conversation id
     */
    public PatientAttachmentLog findAttachmentLog(
        String mid,
        String conversation_id
    ) {
        return patientAttachmentLogDao.findPatientAttachment(mid, conversation_id);

    }

    /**
     *
     * @param attachmentLogInput
     * @description
     *
     * Uses an mid and filename as a composite primary key to update fields
     */
    public void updateAttachmentLog(
        PatientAttachmentLog attachmentLogInput
    ) {
        var mid = attachmentLogInput.getMid();
        patientAttachmentLogDao.updateAttachmentLog(
            mid,
            attachmentLogInput.getFilename(),
            attachmentLogInput.getParent_mid(),
            attachmentLogInput.getContent_type(),
            attachmentLogInput.getCompressed(),
            attachmentLogInput.getLarge_attachment(),
            attachmentLogInput.getBase64(),
            attachmentLogInput.getSkeleton(),
            attachmentLogInput.getUploaded(),
            attachmentLogInput.getLength_num(),
            attachmentLogInput.getOrder_num()
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
