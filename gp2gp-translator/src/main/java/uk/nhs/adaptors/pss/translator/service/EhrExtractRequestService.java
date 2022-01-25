package uk.nhs.adaptors.pss.translator.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.utils.FileUtils;

@Service
@Slf4j
public class EhrExtractRequestService {

    private static final String EHR_EXTRACT_REQUEST_TEST_FILE = "/ehr/ehrExtractRequest.xml";
    private static final String FROM_ODS_CODE_PLACEHOLDER = "%%From_ODS_Code%%";
    private static final String NHS_NUMBER_PLACEHOLDER = "%%NHSNumber%%";

    public String buildEhrExtractRequest(String nhsNumber, String fromODSCode) throws IOException{
        LOGGER.info(
            "Building EHRExtractRequest with nhsNumber=[{}], fromODSCode=[{}]",
            nhsNumber, fromODSCode
        );
        return fillEhrExtractRequestTemplate(nhsNumber, fromODSCode);
    }

    private String fillEhrExtractRequestTemplate(String nhsNumber, String fromODSCode) throws IOException {
        return FileUtils.readFile(EHR_EXTRACT_REQUEST_TEST_FILE)
            .replace(NHS_NUMBER_PLACEHOLDER, nhsNumber)
            .replace(FROM_ODS_CODE_PLACEHOLDER, fromODSCode);
    }

}
