package uk.nhs.adaptors.pss.translator.application;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.connector.dao.ImmunizationSnomedCTDao;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationCommandLineRunner implements CommandLineRunner {

    private final ImmunizationSnomedCTDao immunizationSnomedCTDao;

    @Override
    public void run(String... args) {
        if (!immunizationSnomedCTDao.areImmunizationCodesLoaded()) {
            throw new DataAccessException("FATAL: Database not set up correctly. Immunization codes are not loaded.") {
            };
        }
    }
}
