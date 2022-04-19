INSERT INTO patient_attachment_log(
                                   mid,
                                   filename,
                                   parent_mid,
                                   patient_migration_req_id,
                                   content_type,
                                   compressed,
                                   large_attachment,
                                   base64,
                                   skeleton,
                                   uploaded,
                                   order_num,
                                   length_num
                                   )
VALUES (
        :mid,
        :filename,
        :parent_mid,
        :patient_migration_req_id,
        :content_type,
        :compressed,
        :large_attachment,
        :base64,
        :skeleton,
        COALESCE(:uploaded, false),
        COALESCE(:order_num, 0),
        :length_num
        );