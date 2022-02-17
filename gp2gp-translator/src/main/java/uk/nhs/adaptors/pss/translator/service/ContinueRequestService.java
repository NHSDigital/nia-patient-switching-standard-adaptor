package uk.nhs.adaptors.pss.translator.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.common.util.FileUtil;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
// TODO: This service is related to the large messaging epic and can be used during implementation of NIAD-2045
public class ContinueRequestService {
    private static final String CONTINUE_REQUEST_FILE = ""; // this file needs to be created
    private static final String CREATION_TIMESTAMP_PLACEHOLDER = "%%timestamp%%";
    private static final String MESSAGE_ID_PLACEHOLDER = "%%messageId%%";
    private static final String TO_ASID_PLACEHOLDER = "%%toAsid%%";
    private static final String FROM_ASID_PLACEHOLDER = "%%fromAsid%%";

    private final DateUtils dateUtils;
    private final IdGeneratorService idGeneratorService;

    public String buildContinueRequest(String fromAsid, String toAsid) throws IOException {
        LOGGER.debug("Building ContinueRequest");

        var timestamp = DateFormatUtil.toHl7Format(dateUtils.getCurrentInstant());
        var messageId = idGeneratorService.generateUuid().toLowerCase();

        return FileUtil.readResourceAsString(CONTINUE_REQUEST_FILE)
            .replace(MESSAGE_ID_PLACEHOLDER, messageId)
            .replace(CREATION_TIMESTAMP_PLACEHOLDER, timestamp)
            .replace(FROM_ASID_PLACEHOLDER, fromAsid)
            .replace(TO_ASID_PLACEHOLDER, toAsid);
    }
}
