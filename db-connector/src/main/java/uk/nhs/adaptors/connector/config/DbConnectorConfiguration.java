package uk.nhs.adaptors.connector.config;

import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.sql.DataSource;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.connector.dao.ImmunizationSnomedCTDao;
import uk.nhs.adaptors.connector.dao.MigrationStatusLogDao;
import uk.nhs.adaptors.connector.dao.PatientAttachmentLogDao;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.dao.SnomedCTDao;

@Slf4j
@Configuration
@EntityScan(basePackages = {"uk.nhs.adaptors.connector"})
@ComponentScan(basePackages = {"uk.nhs.adaptors.connector"})
public class DbConnectorConfiguration {

    @Bean
    public Jdbi jdbi(DataSource ds, List<JdbiPlugin> jdbiPlugins, List<RowMapper<?>> rowMappers) {
        TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy(ds);
        Jdbi jdbi = Jdbi.create(proxy);

        SqlLogger sqlLogger = new SqlLogger() {
            @Override
            public void logAfterExecution(StatementContext context) {
                LOGGER.debug("sql {}, parameters {}, timeTaken {} ms", context.getRenderedSql(),
                        context.getBinding().toString(), context.getElapsedTime(ChronoUnit.MILLIS));
            }
        };
        jdbi.setSqlLogger(sqlLogger);

        jdbiPlugins.forEach(jdbi::installPlugin);
        rowMappers.forEach(jdbi::registerRowMapper);
        return jdbi;
    }

    @Bean
    public JdbiPlugin sqlObjectPlugin() {
        return new SqlObjectPlugin();
    }

    @Bean
    public JdbiPlugin postgresPlugin() {
        return new PostgresPlugin();
    }

    @Bean
    public PatientMigrationRequestDao patientMigrationRequestDao(Jdbi jdbi) {
        return jdbi.onDemand(PatientMigrationRequestDao.class);
    }

    @Bean
    public MigrationStatusLogDao migrationStatusLogDao(Jdbi jdbi) {
        return jdbi.onDemand(MigrationStatusLogDao.class);
    }

    @Bean
    public PatientAttachmentLogDao attachmentLogDao(Jdbi jdbi) {
        return jdbi.onDemand(PatientAttachmentLogDao.class);
    }

    @Bean
    public ImmunizationSnomedCTDao immunizationSnomedCTDao(Jdbi jdbi) {
        return jdbi.onDemand(ImmunizationSnomedCTDao.class);
    }

    @Bean
    public SnomedCTDao snomedCTDao(Jdbi jdbi) {
        return jdbi.onDemand(SnomedCTDao.class);
    }
}
