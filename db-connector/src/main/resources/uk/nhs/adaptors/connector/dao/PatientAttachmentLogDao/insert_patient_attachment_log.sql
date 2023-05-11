INSERT INTO patient_attachment_log(
                                   mid,
                                   filename,
                                   parent_mid,
                                   patient_migration_req_id,
                                   content_type,
                                   compressed,
                                   large_attachment,
                                   original_base64,
                                   skeleton,
                                   uploaded,
                                   order_num,
                                   length_num,
                                   post_processed_length_num
                                   )
VALUES (
        :mid,
        :filename,
        :parentMid,
        :patientMigrationReqId,
        :contentType,
        :compressed,
        :largeAttachment,
        :originalBase64,
        :skeleton,
        COALESCE(:uploaded, false),
        COALESCE(:orderNum, 0),
        :lengthNum,
        :postProcessedLengthNum
        );