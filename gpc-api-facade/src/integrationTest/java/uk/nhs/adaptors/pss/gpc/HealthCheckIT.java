package uk.nhs.adaptors.pss.gpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext
public class HealthCheckIT {
    private static final String HEALTHCHECK_ENDPOINT = "/healthcheck";

    @LocalServerPort
    private int port;

    @Test
    public void When_GettingHealthCheck_Expect_ServiceIsUp() {
        String response = WebClient.builder()
            .baseUrl("http://localhost:" + port)
            .build()
            .get()
            .uri(builder -> builder.path(HEALTHCHECK_ENDPOINT).build())
            .retrieve()
            .bodyToMono(String.class)
            .block();

        assertThat(response).contains("UP");
    }
}
