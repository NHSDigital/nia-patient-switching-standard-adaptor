package uk.nhs.adaptors.pss.translator.service;

import java.io.IOException;

import com.github.mustachejava.Mustache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.template.parameter.SendContinueRequestParams;
import static uk.nhs.adaptors.pss.translator.util.template.TemplateUtil.fillTemplate;
import static uk.nhs.adaptors.pss.translator.util.template.TemplateUtil.loadTemplate;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
// TODO: This service is related to the large messaging epic and can be used during implementation of NIAD-2045
public class ContinueRequestService {
    private static final Mustache CONTINUE_REQUEST_FILE = loadTemplate("sendContinueRequest.xml");; // this file needs to be created

/*    private static final String CREATION_TIMESTAMP_PLACEHOLDER = "%%timestamp%%";
    private static final String MESSAGE_ID_PLACEHOLDER = "%%messageId%%";
    private static final String TO_ASID_PLACEHOLDER = "%%toAsid%%";
    private static final String FROM_ASID_PLACEHOLDER = "%%fromAsid%%";*/ //delete

    private final DateUtils dateUtils;
    private final IdGeneratorService idGeneratorService;

    public String buildContinueRequest(String fromAsid, String toAsid) throws IOException {
        LOGGER.debug("Building ContinueRequest");

        SendContinueRequestParams params = SendContinueRequestParams.builder()
        .messageId(idGeneratorService.generateUuid().toLowerCase())
        .timestamp(DateFormatUtil.toHl7Format(dateUtils.getCurrentInstant()))
        .toAsid(toAsid)
        .fromAsid(fromAsid)
        .build();

        return fillTemplate(CONTINUE_REQUEST_FILE, params);
    }
}
