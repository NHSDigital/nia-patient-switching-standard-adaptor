package uk.nhs.adaptors.pss.translator;

import org.hl7.v3.RCMRMT030101UKObservationStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.adaptors.pss.translator.util.DatabaseImmunizationChecker;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.adaptors.pss.translator.TestUtility.createCd;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
class DatabaseImmunizationCheckerIT {
    private static final String SNOMED_CODE_SYSTEM = "2.16.840.1.113883.2.1.3.2.4.15";
    private static final String DISPLAY_NAME = "Administration of yellow fever vaccine";

    @Autowired
    private DatabaseImmunizationChecker databaseImmunizationChecker;

    @Test
    void When_Immunization_With_SnomedDescriptionId_Expect_True() {
        final String immunizationDescriptionSnomedId = "67308009";
        final RCMRMT030101UKObservationStatement observationStatement =
            new RCMRMT030101UKObservationStatement();

        observationStatement.setCode(createCd(
            immunizationDescriptionSnomedId,
            SNOMED_CODE_SYSTEM,
            DISPLAY_NAME
        ));

        final boolean result = databaseImmunizationChecker.isImmunization(observationStatement);

        assertThat(result).isTrue();
    }

    @Test
    void When_Immunization_With_SnomedConceptId_Expect_True() {
        final String immunizationConceptSnomedId = "67308009";
        final RCMRMT030101UKObservationStatement observationStatement =
            new RCMRMT030101UKObservationStatement();

        observationStatement.setCode(createCd(
            immunizationConceptSnomedId,
            SNOMED_CODE_SYSTEM,
            DISPLAY_NAME
        ));

        final boolean result = databaseImmunizationChecker.isImmunization(observationStatement);

        assertThat(result).isTrue();
    }
}
