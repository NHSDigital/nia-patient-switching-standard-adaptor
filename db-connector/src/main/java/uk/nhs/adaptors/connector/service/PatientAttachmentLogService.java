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
     * @description
     * Requires a minimum of mid, filename and patient request id
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
     * @param conversationId
     * @return PatientAttachmentLog
     *
     * @description
     * Find an attachment log with an mid and conversation id
     */
    public PatientAttachmentLog findAttachmentLog(
        String mid,
        String conversationId
    ) {
        return patientAttachmentLogDao.findPatientAttachment(mid, conversationId);

    }

    /**
     *
     * @param attachmentLogInput
     * @param conversation_id
     *
     * @description
     * Update an attachment log with using mid and conversation id
     */
    public void updateAttachmentLog(
        PatientAttachmentLog attachmentLogInput,
        String conversation_id
    ) {
        var mid = attachmentLogInput.getMid();
        patientAttachmentLogDao.updateAttachmentLog(
            mid,
            conversation_id,
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
        String mid,
        String conversation_id
    ) {
        patientAttachmentLogDao.deleteAttachmentLog(
            mid,
            conversation_id
        );
        LOGGER.debug("Deleted flag set on migration log mid=[{}] ", mid);

    }
}
