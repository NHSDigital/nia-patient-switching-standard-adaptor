package uk.nhs.adaptors.pss.translator.application;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.connector.dao.ImmunizationSnomedCTDao;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class VerifyImmunizationsLoadedCommandLineRunner implements CommandLineRunner {

    private final ImmunizationSnomedCTDao immunizationSnomedCTDao;

    @Override
    public void run(String... args) {
        if (!immunizationSnomedCTDao.areImmunizationCodesLoaded()) {
            throw new RuntimeException("""
            FATAL: Expected Immunization codes not found in snomedct.immunization_codes view.
            SNOMED CT Database not set up correctly.
            Please update / reload the SNOMED DB.
            """
            );
        }
    }
}
