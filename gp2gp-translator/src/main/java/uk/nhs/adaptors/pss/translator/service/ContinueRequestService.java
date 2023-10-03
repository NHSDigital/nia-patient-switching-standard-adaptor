package uk.nhs.adaptors.pss.translator.service;

import java.io.IOException;

import com.github.mustachejava.Mustache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.template.parameter.SendContinueRequestParams;
import static uk.nhs.adaptors.pss.translator.util.template.TemplateUtil.fillTemplate;
import static uk.nhs.adaptors.pss.translator.util.template.TemplateUtil.loadTemplate;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ContinueRequestService {
    private static final Mustache CONTINUE_REQUEST_FILE = loadTemplate("sendContinueRequest.mustache");
    private final DateUtils dateUtils;

    public String buildContinueRequest(ContinueRequestData data, String messageId) throws IOException {
        LOGGER.debug("Building ContinueRequest");

        SendContinueRequestParams params = SendContinueRequestParams.builder()
            .messageId(messageId)
            .timestamp(DateFormatUtil.toHl7Format(dateUtils.getCurrentInstant()))
            .nhsNumber(data.getNhsNumber())
            .toAsid(data.getToAsid())
            .fromAsid(data.getFromAsid())
            .fromOdsCode(data.getFromOdsCode())
            .toOdsCode(data.getToOdsCode())
            .mcciIN010000UK13creationTime(data.getMcciIN010000UK13creationTime())
            .ehrExtractId(data.getEhrExtractId())
            .build();

        return fillTemplate(CONTINUE_REQUEST_FILE, params);
    }
}
