import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PostgresDbSmokeTest {

    private static final String URI_ENV_VARIABLE = "PS_DB_URL";
    private static final String URI_DEFAULT_VALUE = "jdbc:postgresql://localhost:5436";

    private static final String DATABASE_NAME_DEFAULT_VALUE = "patient_switching";
    private static final String HOST_ENV_VARIABLE = "HOSTNAME";
    private static final String PORT_ENV_VARIABLE = "DB_PORT";
    private static final String USERNAME_ENV_VARIABLE = "PS_DB_OWNER_NAME";
    private static final String USERNAME_DEFAULT_VARIABLE = "postgres";
    private static final String PASSWORD_ENV_VARIABLE = "PS_DB_OWNER_PASSWORD";
    private static final String PASSWORD_DEFAULT_VARIABLE = "123456";

    private static String connectionString;
    private static String username;
    private static String password;
    private static Map<String, String> envVars;

    @BeforeAll
    public static void setup() {
        envVars = System.getenv();

        Optional<String> postgresUriOptional = Optional.ofNullable(envVars.get(URI_ENV_VARIABLE));
        String uri = postgresUriOptional.orElse(URI_DEFAULT_VALUE);

        connectionString = buildConnectionString(envVars).orElse(uri);

        Optional<String> postgresUsernameOptional = Optional.ofNullable(envVars.get(USERNAME_ENV_VARIABLE));
        username = postgresUsernameOptional.orElse(USERNAME_DEFAULT_VARIABLE);

        Optional<String> postgresPasswordOptional = Optional.ofNullable(envVars.get(PASSWORD_ENV_VARIABLE));
        password = postgresPasswordOptional.orElse(PASSWORD_DEFAULT_VARIABLE);
    }

    @Test
    public void when_HostEnvIsPresent_Expect_PortEnvIsPresent() {

        // test skipped if host environment variable is not set
        assumeThat(envVars.containsKey(HOST_ENV_VARIABLE)).isTrue();

        assertThat(envVars.containsKey(PORT_ENV_VARIABLE))
            .as("If the environment variable " + HOST_ENV_VARIABLE + " is set then " +
                PORT_ENV_VARIABLE + " should also be set")
            .isTrue();
    }

    @Test
    public void when_UsernameEnvIsPresent_Expect_PasswordEnvIsPresent() {

        // test skipped if the host and username environment variables are not set
        assumeThat(envVars.containsKey(HOST_ENV_VARIABLE)).isTrue();
        assumeThat(envVars.containsKey(USERNAME_ENV_VARIABLE)).isTrue();

        assertThat(envVars.containsKey(PASSWORD_ENV_VARIABLE))
            .as("If the environment variable " + USERNAME_ENV_VARIABLE + " is set then " +
                PASSWORD_ENV_VARIABLE + " should also be set")
            .isTrue();
    }

    @Test
    public void when_PasswordEnvIsPresent_Expect_UsernameEnvIsPresent() {

        // test skipped if the host and password environment variables are not set
        assumeThat(envVars.containsKey(HOST_ENV_VARIABLE)).isTrue();
        assumeThat(envVars.containsKey(PASSWORD_ENV_VARIABLE)).isTrue();

        assertThat(envVars.containsKey(USERNAME_ENV_VARIABLE))
            .as("If the environment variable " + PASSWORD_ENV_VARIABLE + " is set then " +
                USERNAME_ENV_VARIABLE + " should also be set")
            .isTrue();
    }

    @Test
    public void expect_postgresDbConnectionIsAvailable() {

            try (Connection connection = DriverManager.getConnection(connectionString, username, password)) {
                Class.forName("org.postgresql.Driver");
                connection.getSchema();
            } catch (SQLException e) {
                fail("Unable to connect to postgres DB at " + connectionString);
            } catch (ClassNotFoundException e) {
                fail("Could not load the JDBC driver!");
            }
    }

    private static Optional<String> buildConnectionString(Map<String, String> envVars) {

        Optional<String> connectionStringOptional = Optional.empty();

        if (envVars.containsKey(HOST_ENV_VARIABLE) && envVars.containsKey(PORT_ENV_VARIABLE)) {

            String connectionString = "jdbc:postgresql://";

            connectionString += envVars.get(HOST_ENV_VARIABLE) + ":" + envVars.get(PORT_ENV_VARIABLE) + "/" + DATABASE_NAME_DEFAULT_VALUE;

            connectionStringOptional = Optional.of(connectionString);
        }

        return connectionStringOptional;
    }
}
