package uk.nhs.adaptors.pss.translator.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Property;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.pss.translator.exception.SdsRetrievalException;
import uk.nhs.adaptors.pss.translator.sds.SdsRequestBuilder;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SDSService {

    private static final String EXTENSION_KEY_VALUE = "extension";
    private static final String PERSIST_DURATION_URL = "nhsMHSPersistDuration";

    private final SdsRequestBuilder requestBuilder;
    private final SdsClientService sdsClientService;
    private final FhirParser fhirParser;

    public Duration getPersistDurationFor(String messageType, String odsCode, String conversationId) throws SdsRetrievalException {

        String sdsResponse = getResponseFromSds(messageType, odsCode, conversationId);
        Duration duration = parsePersistDuration(sdsResponse);

        LOGGER.debug("Retrieved persist duration of [{}] for odscode [{}] and  messageType [{}]", duration, odsCode, messageType);
        return duration;
    }

    private String getResponseFromSds(String messageType, String odsCode, String conversationId) throws SdsRetrievalException {
        var request = requestBuilder.buildGetRequest(messageType, odsCode, conversationId);

        try {
            return sdsClientService.send(request);
        } catch (WebClientResponseException e) {
            LOGGER.error("Received an ERROR response from SDS: [{}]", e.getMessage());
            throw new SdsRetrievalException(String.format("Error getting messageType [%s] info from SDS", messageType));
        }
    }

    private Duration parsePersistDuration(String sdsResponse) throws SdsRetrievalException {
        Bundle bundle = fhirParser.parseResource(sdsResponse, Bundle.class);

        List<Bundle.BundleEntryComponent> entries = bundle.getEntry();

        if (entries.isEmpty()) {
            throw new SdsRetrievalException("sds response doesn't contain any results");
        }

        Resource resource = entries.get(0).getResource();
        Property matchingChildren = resource.getChildByName(EXTENSION_KEY_VALUE);
        Optional<Extension> extensions = matchingChildren.getValues().stream().map(child -> (Extension) child).findFirst();

        Optional<Extension> persistDuration = extensions
            .orElseThrow(() -> new SdsRetrievalException("Error parsing persist duration extension"))
            .getExtensionsByUrl(PERSIST_DURATION_URL)
            .stream().findFirst();

        String isoDuration = persistDuration
            .orElseThrow(() -> new SdsRetrievalException("Error parsing persist duration value"))
            .getValue()
            .toString();

        return Duration.parse(isoDuration);
    }
}
