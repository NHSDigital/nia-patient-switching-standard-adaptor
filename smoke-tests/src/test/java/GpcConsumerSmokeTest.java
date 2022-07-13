import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import util.EnvVarsUtil;

public class GpcConsumerSmokeTest {

    private final static String HEALTHCHECK_ENDPOINT = "/healthcheck";
    private final static String SERVER_DEFAULT_HOST = "http://localhost";
    private final static String SERVER_PORT_DEFAULT_VALUE = "8081";
    private final static String GPC_PORT_ENV_VARIABLE = "GPC_FACADE_SERVER_PORT";

    private static String invalidResponseMessage;
    private static String serverPort;

    @BeforeAll
    public static void setup() {
        Map<String, String> envVars = System.getenv();

        Optional<String> serverPortOptional = Optional.ofNullable(envVars.get(GPC_PORT_ENV_VARIABLE));
        serverPort = serverPortOptional.orElse(SERVER_PORT_DEFAULT_VALUE);

        invalidResponseMessage = "Invalid response from GP2GP adaptor at " + SERVER_DEFAULT_HOST + ":" + serverPort;
    }

    @Test
    public void expect_GpcConsumerIsAvailable() {

        Optional<String> responseBody = Optional.empty();

        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(
                SERVER_DEFAULT_HOST + ":" + serverPort + HEALTHCHECK_ENDPOINT
            );

            responseBody = httpClient.execute(httpGet, response -> {
                HttpEntity entity = response.getEntity();

                try {
                    return entity != null ? Optional.of(EntityUtils.toString(entity)) : Optional.empty();
                } catch (ParseException e) {
                    return Optional.empty();
                }
            });
        } catch (IOException e) {
            fail("Unable to connect to GPC Consumer at " + SERVER_DEFAULT_HOST + ":" + serverPort + HEALTHCHECK_ENDPOINT);
        }

        assertThat(responseBody.isPresent())
                .as(invalidResponseMessage)
                .isTrue();

        assertThat(responseBody.get())
                .as(invalidResponseMessage)
                .contains("UP");
    }
}
