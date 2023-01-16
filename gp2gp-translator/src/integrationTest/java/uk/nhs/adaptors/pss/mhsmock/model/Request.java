package uk.nhs.adaptors.pss.mhsmock.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Request {
    private Headers headers;
    private String body;
    private Long loggedDate;

    @Getter
    @Setter
    @ToString
    public static class Headers {
        @JsonProperty("Interaction-Id")
        private String interactionId;
        @JsonProperty("Correlation-Id")
        private String correlationId;
    }
}
