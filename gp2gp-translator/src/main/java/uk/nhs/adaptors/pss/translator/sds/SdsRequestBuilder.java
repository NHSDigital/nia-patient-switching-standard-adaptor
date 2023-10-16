package uk.nhs.adaptors.pss.translator.sds;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import lombok.RequiredArgsConstructor;
import reactor.netty.http.client.HttpClient;
import uk.nhs.adaptors.pss.translator.config.SdsConfiguration;
import uk.nhs.adaptors.pss.translator.service.RequestBuilderService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SdsRequestBuilder {

    private static final String ROUTING_AND_READABILITY_ENDPOINT = "/Endpoint";
    private static final String ACCREDITED_SYSTEMS_INFORMATION_DEVICE_ENDPOINT = "/Device";
    private static final String IDENTIFIER_HEADER = "identifier";
    private static final String INTERACTION_ID_IDENTIFIER =
        "https://fhir.nhs.uk/Id/nhsServiceInteractionId|urn:nhs:names:services:gp2gp:";
    private static final String ORGANISATION_HEADER = "organization";
    private static final String ORGANISATION_CODE_IDENTIFIER = "https://fhir.nhs.uk/Id/ods-organization-code|";
    private static final String NHS_MHS_PARTY_KEY_URL =  "https://fhir.nhs.uk/Id/nhsMhsPartyKey|";
    private static final String CORRELATION_ID = "X-Correlation-Id";
    private static final String API_KEY_HEADER = "apikey";

    private final RequestBuilderService requestBuilderService;
    private final SdsConfiguration sdsConfiguration;

    public WebClient.RequestHeadersSpec<?> buildEndpointGetRequestWithIdentifierAndOrgParams(String messageType,
                                                                                             String odsCode,
                                                                                             String conversationId) {
        WebClient client = fetchWebClient();

        WebClient.RequestBodySpec uri = client.method(GET).uri(
            uriBuilder -> uriBuilder
                .path(ROUTING_AND_READABILITY_ENDPOINT)
                .queryParam(IDENTIFIER_HEADER, INTERACTION_ID_IDENTIFIER.concat(messageType))
                .queryParam(ORGANISATION_HEADER, ORGANISATION_CODE_IDENTIFIER.concat(odsCode))
                .build()
        );

        return uri
            .accept(APPLICATION_JSON)
            .header(CORRELATION_ID, conversationId)
            .header(API_KEY_HEADER, sdsConfiguration.getApikey());
    }

    public WebClient.RequestHeadersSpec<?> buildEndpointGetRequestWithDoubleIdentifierParams(String messageType,
                                                                                             String nhsMhsPartyKey,
                                                                                             String conversationId) {
        WebClient client = fetchWebClient();

        WebClient.RequestBodySpec uri = client.method(GET).uri(
            uriBuilder -> uriBuilder
                .path(ROUTING_AND_READABILITY_ENDPOINT)
                .queryParam(IDENTIFIER_HEADER, INTERACTION_ID_IDENTIFIER.concat(messageType))
                .queryParam(IDENTIFIER_HEADER, NHS_MHS_PARTY_KEY_URL.concat(nhsMhsPartyKey))
                .build()
                                                              );

        return uri
            .accept(APPLICATION_JSON)
            .header(CORRELATION_ID, conversationId)
            .header(API_KEY_HEADER, sdsConfiguration.getApikey());
    }

    public WebClient.RequestHeadersSpec<?> buildDeviceGetRequest(String messageType, String odsCode, String conversationId) {
        WebClient client = fetchWebClient();

        WebClient.RequestBodySpec uri = client.method(GET).uri(
            uriBuilder -> uriBuilder
                .path(ACCREDITED_SYSTEMS_INFORMATION_DEVICE_ENDPOINT)
                .queryParam(IDENTIFIER_HEADER, INTERACTION_ID_IDENTIFIER.concat(messageType))
                .queryParam(ORGANISATION_HEADER, ORGANISATION_CODE_IDENTIFIER.concat(odsCode))
                .build()
           );

        return uri
            .accept(APPLICATION_JSON)
            .header(CORRELATION_ID, conversationId)
            .header(API_KEY_HEADER, sdsConfiguration.getApikey());
    }

    private WebClient fetchWebClient() {
        SslContext sslContext = requestBuilderService.buildSSLContext();
        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
        return buildWebClient(httpClient);
    }


    private WebClient buildWebClient(HttpClient httpClient) {
        return WebClient
            .builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(sdsConfiguration.getUrl())
            .build();
    }
}
