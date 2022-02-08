package uk.nhs.adaptors.pss.translator.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.FileUtils;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class EhrExtractRequestService {
    private static final String EHR_EXTRACT_REQUEST_FILE = "/ehr/ehrExtractRequest.xml";
    private static final String FROM_ODS_CODE_PLACEHOLDER = "%%fromODSCode%%";
    private static final String NHS_NUMBER_PLACEHOLDER = "%%NHSNumber%%";
    private static final String CREATION_TIMESTAMP_PLACEHOLDER = "%%timestamp%%";
    private static final String MESSAGE_ID_PLACEHOLDER = "%%messageId%%";

    private final DateUtils dateUtils;
    private final IdGeneratorService idGeneratorService;

    public String buildEhrExtractRequest(String nhsNumber, String fromODSCode) throws IOException {
        LOGGER.debug(
            "Building EHRExtractRequest with nhsNumber=[{}], fromODSCode=[{}]",
            nhsNumber, fromODSCode
        );

        var timestamp = DateFormatUtil.toHl7Format(dateUtils.getCurrentInstant());
        var messageId = idGeneratorService.generateUuid().toLowerCase();

        return FileUtils.readFile(EHR_EXTRACT_REQUEST_FILE)
            .replace(CREATION_TIMESTAMP_PLACEHOLDER, timestamp)
            .replace(MESSAGE_ID_PLACEHOLDER, messageId)
            .replace(NHS_NUMBER_PLACEHOLDER, nhsNumber)
            .replace(FROM_ODS_CODE_PLACEHOLDER, fromODSCode);
    }
}
