package uk.nhs.adaptors.connector.service;

import java.util.List;

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
            attachmentLogInput.getParentMid(),
            attachmentLogInput.getPatientMigrationReqId(),
            attachmentLogInput.getContentType(),
            attachmentLogInput.getCompressed(),
            attachmentLogInput.getLargeAttachment(),
            attachmentLogInput.getBase64(),
            attachmentLogInput.getSkeleton(),
            attachmentLogInput.getUploaded(),
            attachmentLogInput.getOrderNum(),
            attachmentLogInput.getLengthNum()
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
     * @param conversationId
     * @return PatientAttachmentLogs
     *
     * @description
     * Find all attachment logs associated with a conversation id
     */
    public List<PatientAttachmentLog> findAttachmentLogs(
        String conversationId
    ) {
        return patientAttachmentLogDao.findPatientAttachments(conversationId);
    }



    /**
     *
     * @param attachmentLogInput
     * @param conversationId
     *
     * @description
     * Update an attachment log with using mid and conversation id
     */
    public void updateAttachmentLog(
        PatientAttachmentLog attachmentLogInput,
        String conversationId
    ) {
        var mid = attachmentLogInput.getMid();
        patientAttachmentLogDao.updateAttachmentLog(
            mid,
            conversationId,
            attachmentLogInput.getParentMid(),
            attachmentLogInput.getContentType(),
            attachmentLogInput.getCompressed(),
            attachmentLogInput.getLargeAttachment(),
            attachmentLogInput.getBase64(),
            attachmentLogInput.getSkeleton(),
            attachmentLogInput.getUploaded(),
            attachmentLogInput.getLengthNum(),
            attachmentLogInput.getOrderNum()
        );
        LOGGER.debug("Updated migration log mid=[{}]", mid);
    }

    public void deleteAttachmentLog(
        String mid,
        String conversationId
    ) {
        patientAttachmentLogDao.deleteAttachmentLog(
            mid,
            conversationId
        );
        LOGGER.debug("Deleted flag set on migration log mid=[{}] ", mid);
    }

    /**
     *
     * @param conversationId
     * @param parentMid
     * @return PatientAttachmentLogs
     *
     * @description
     * Find all attachment logs associated with a conversation id and parent_mid
     */
    public List<PatientAttachmentLog> findAttachmentLogsByParentMid(
        String conversationId, String parentMid) {
        return patientAttachmentLogDao.findPatientAttachmentsByParentMid(conversationId, parentMid);
    }

}
