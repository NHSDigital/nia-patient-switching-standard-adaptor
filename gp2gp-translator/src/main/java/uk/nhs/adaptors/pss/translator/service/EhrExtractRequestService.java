package uk.nhs.adaptors.pss.translator.service;

import static uk.nhs.adaptors.common.util.FileUtil.readResourceAsString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.model.TransferRequestMessage;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class EhrExtractRequestService {
    private static final String EHR_EXTRACT_REQUEST_FILE = "ehr/ehrExtractRequest.xml";
    private static final String FROM_ODS_CODE_PLACEHOLDER = "%%fromODSCode%%";
    private static final String TO_ODS_CODE_PLACEHOLDER = "%%toODSCode%%";
    private static final String NHS_NUMBER_PLACEHOLDER = "%%NHSNumber%%";
    private static final String CREATION_TIMESTAMP_PLACEHOLDER = "%%timestamp%%";
    private static final String MESSAGE_ID_PLACEHOLDER = "%%messageId%%";
    private static final String TO_ASID_PLACEHOLDER = "%%toAsid%%";
    private static final String FROM_ASID_PLACEHOLDER = "%%fromAsid%%";
    private static final String EHR_REQUEST_ID_PLACEHOLDER = "%%ehrRequestId%%";

    private final DateUtils dateUtils;
    private final IdGeneratorService idGeneratorService;

    public String buildEhrExtractRequest(TransferRequestMessage transferRequestMessage) {
        LOGGER.debug("Building EHRExtractRequest with nhsNumber=[{}]", transferRequestMessage.getPatientNhsNumber());

        var timestamp = DateFormatUtil.toHl7Format(dateUtils.getCurrentInstant());
        var messageId = idGeneratorService.generateUuid().toLowerCase();
        var ehrRequestId = idGeneratorService.generateUuid().toLowerCase();

        return readResourceAsString(EHR_EXTRACT_REQUEST_FILE)
            .replace(MESSAGE_ID_PLACEHOLDER, messageId)
            .replace(CREATION_TIMESTAMP_PLACEHOLDER, timestamp)
            .replace(TO_ASID_PLACEHOLDER, transferRequestMessage.getToAsid())
            .replace(FROM_ASID_PLACEHOLDER, transferRequestMessage.getFromAsid())
            .replace(NHS_NUMBER_PLACEHOLDER, transferRequestMessage.getPatientNhsNumber())
            .replace(EHR_REQUEST_ID_PLACEHOLDER, ehrRequestId)
            .replace(FROM_ODS_CODE_PLACEHOLDER, transferRequestMessage.getFromOds())
            .replace(TO_ODS_CODE_PLACEHOLDER, transferRequestMessage.getToOds());
    }
}
