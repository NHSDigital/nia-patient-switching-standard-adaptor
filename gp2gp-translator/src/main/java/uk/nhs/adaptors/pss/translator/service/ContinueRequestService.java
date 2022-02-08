package uk.nhs.adaptors.pss.translator.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.util.FileUtils;

@Service
@Slf4j
public class ContinueRequestService {
    private static final String CONTINUE_REQUEST_FILE = "/COPC_IN000001UK01.xml";
    private static final String FROM_ODS_CODE_PLACEHOLDER = "%%From_ODS_Code%%";
    private static final String NHS_NUMBER_PLACEHOLDER = "%%NHSNumber%%";

    public String buildContinueRequest(String nhsNumber, String fromODSCode) throws IOException {
        LOGGER.debug(
            "Building EHRExtractRequest with nhsNumber=[{}], fromODSCode=[{}]",
            nhsNumber, fromODSCode
        );
        return fillContinueRequestTemplate(nhsNumber, fromODSCode);
    }

    private String fillContinueRequestTemplate(String nhsNumber, String fromODSCode) throws IOException {
        return FileUtils.readFile(CONTINUE_REQUEST_FILE)
            .replace(NHS_NUMBER_PLACEHOLDER, nhsNumber)
            .replace(FROM_ODS_CODE_PLACEHOLDER, fromODSCode);
    }
}
