package uk.nhs.adaptors.pss.translator.service;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.UriType;

import org.hl7.v3.CV;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.nhs.adaptors.pss.translator.TestUtility;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ConfidentialityServiceTest {
    private static final String DUMMY_PROFILE = "Test-MyProfile-1";
    private static final String DUMMY_PROFILE_URI = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-%s".formatted(DUMMY_PROFILE);

    private static final CV ALTERNATIVE_CV = TestUtility.createCv(
        "NOSCRUB",
        "http://hl7.org/fhir/v3/FakeCode",
        "no scrubbing of the patient, family or caregivers without attending provider's authorization"
    );

    private static final CV NOPAT_CV = TestUtility.createCv(
        "NOPAT",
        "http://hl7.org/fhir/v3/ActCode",
        "no disclosure to patient, family or caregivers without attending provider's authorization"
    );

    private static ConfidentialityService confidentialityService;

    @BeforeAll
    static void beforeAll() {
        confidentialityService = new ConfidentialityService();
    }

    @Test
    void When_CreateMetaAndAddSecurityIfConfidentialityCodesPresent_With_ValidMetaProfileAndNopatCv_Expect_MetaWithSecurity() {
        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            Optional.of(NOPAT_CV)
        );

        assertMetaSecurityIsPresent(result);
    }

    @Test
    void When_CreateMetaAndAddSecurityIfConfidentialityCodesPresent_With_ValidMetaProfileAndAlternativeCv_Expect_MetaWithoutSecurity() {
        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            Optional.of(ALTERNATIVE_CV)
        );

        assertMetaSecurityIsNotPresent(result);
    }

    @Test
    void When_CreateMetaAndAddSecurityIfConfidentialityCodesPresent_With_ValidMetaProfileAndNoCv_Expect_MetaWithoutSecurity() {
        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            Optional.empty(),
            Optional.empty()
        );

        assertMetaSecurityIsNotPresent(result);
    }

    private void assertMetaSecurityIsPresent(final Meta meta) {
        final List<Coding> metaSecurity = meta.getSecurity();
        final int metaSecuritySize = metaSecurity.size();
        final Coding metaSecurityCoding = metaSecurity.get(0);
        final UriType metaProfile = meta.getProfile().get(0);

        assertAll(
            () -> assertThat(metaSecuritySize).isEqualTo(1),
            () -> assertThat(metaProfile.getValue()).isEqualTo(DUMMY_PROFILE_URI),
            () -> assertThat(metaSecurityCoding.getCode()).isEqualTo(NOPAT_CV.getCode()),
            () -> assertThat(metaSecurityCoding.getDisplay()).isEqualTo(NOPAT_CV.getDisplayName()),
            () -> assertThat(metaSecurityCoding.getSystem()).isEqualTo(NOPAT_CV.getCodeSystem())
        );
    }

    private void assertMetaSecurityIsNotPresent(final Meta meta) {
        assertAll(
            () -> assertThat(meta.getSecurity().size()).isEqualTo(0),
            () -> assertThat(meta.getProfile().get(0).getValue()).isEqualTo(DUMMY_PROFILE_URI)
        );
    }
}