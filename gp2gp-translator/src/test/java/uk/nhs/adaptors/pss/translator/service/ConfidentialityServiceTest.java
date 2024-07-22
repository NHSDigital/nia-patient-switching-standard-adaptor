package uk.nhs.adaptors.pss.translator.service;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CV;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKMedicationStatement;
import org.hl7.v3.RCMRMT030101UKObservationStatement;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ConfidentialityServiceTest {
    private static final String DUMMY_PROFILE = "MyProfile-1";
    private static final String DUMMY_PROFILE_URI = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-%s".formatted(DUMMY_PROFILE);

    private static final CV ALTERNATIVE_CV = createCv(
        "NOSCRUB",
        "http://hl7.org/fhir/v3/FakeCode",
        "no scrubbing of the patient, family or caregivers without attending provider's authorization"
    );

    private static final CV NOPAT_CV = createCv(
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
    void When_EhrCompositionWithNopatConfidentialityCodePresent_Expect_AddSecurityToMeta() {
        final RCMRMT030101UKEhrComposition ehrComposition = new RCMRMT030101UKEhrComposition();
        ehrComposition.setConfidentialityCode(NOPAT_CV);

        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            ehrComposition.getConfidentialityCode()
        );

        assertMetaSecurityIsPresent(result);
    }

    @Test
    void When_EhrCompositionWithConfidentialityCodeOtherThanNopatPresent_Expect_SecurityNotAddedToMeta() {
        final RCMRMT030101UKEhrComposition ehrComposition = new RCMRMT030101UKEhrComposition();
        ehrComposition.setConfidentialityCode(ALTERNATIVE_CV);

        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            ehrComposition.getConfidentialityCode()
        );

        assertMetaSecurityIsNotPresent(result);
    }

    @Test
    void When_EhrCompositionWithoutConfidentialityCodePresent_Expect_SecurityNotAddedToMeta() {
        final RCMRMT030101UKEhrComposition ehrComposition = new RCMRMT030101UKEhrComposition();

        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            ehrComposition.getConfidentialityCode()
        );

        assertMetaSecurityIsNotPresent(result);
    }

    @Test
    void When_ObservationStatementWithNopatConfidentialityCodePresent_Expect_SecurityAddedToMeta() {
        final RCMRMT030101UKObservationStatement observationStatement = new RCMRMT030101UKObservationStatement();
        observationStatement.setConfidentialityCode(NOPAT_CV);

        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            observationStatement.getConfidentialityCode()
        );

        assertMetaSecurityIsPresent(result);
    }

    @Test
    void When_ObservationStatementWithConfidentialityCodeOtherThanNopatPresent_Expect_SecurityNotAddedToMeta() {
        final RCMRMT030101UKObservationStatement observationStatement = new RCMRMT030101UKObservationStatement();
        observationStatement.setConfidentialityCode(ALTERNATIVE_CV);

        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            observationStatement.getConfidentialityCode()
        );

        assertMetaSecurityIsNotPresent(result);
    }

    @Test
    void When_ObservationStatementWithoutConfidentialityCodePresent_Expect_SecurityNotAddedToMeta() {
        final RCMRMT030101UKObservationStatement observationStatement = new RCMRMT030101UKObservationStatement();

        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            observationStatement.getConfidentialityCode()
        );

        assertMetaSecurityIsNotPresent(result);
    }

    @Test
    void When_MedicationStatementWithNopatConfidentialityCodePresent_Expect_SecurityAddedToMeta() {
        final RCMRMT030101UKMedicationStatement medicationStatement = new RCMRMT030101UKMedicationStatement();
        medicationStatement.setConfidentialityCode(NOPAT_CV);

        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            medicationStatement.getConfidentialityCode()
        );

        assertMetaSecurityIsPresent(result);
    }

    @Test
    void When_MedicationStatementWithConfidentialityCodeOtherThanNopatPresent_Expect_SecurityNotAddedToMeta() {
        final RCMRMT030101UKMedicationStatement medicationStatement = new RCMRMT030101UKMedicationStatement();
        medicationStatement.setConfidentialityCode(ALTERNATIVE_CV);

        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            medicationStatement.getConfidentialityCode()
        );

        assertMetaSecurityIsNotPresent(result);
    }

    @Test
    void When_MedicationStatementWithoutConfidentialityCodePresent_Expect_SecurityNotAddedToMeta() {
        final RCMRMT030101UKMedicationStatement medicationStatement = new RCMRMT030101UKMedicationStatement();

        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            medicationStatement.getConfidentialityCode()
        );

        assertMetaSecurityIsNotPresent(result);
    }

    private void assertMetaSecurityIsPresent(final Meta meta) {
        final int metaSecuritySize = meta.getSecurity().size();
        final Coding metaSecurity = meta.getSecurity().get(0);
        final UriType metaProfile = meta.getProfile().get(0);

        assertAll(
            () -> assertThat(metaSecuritySize).isEqualTo(1),
            () -> assertThat(metaProfile.getValue()).isEqualTo(DUMMY_PROFILE_URI),
            () -> assertThat(metaSecurity.getCode()).isEqualTo(NOPAT_CV.getCode()),
            () -> assertThat(metaSecurity.getDisplay()).isEqualTo(NOPAT_CV.getDisplayName()),
            () -> assertThat(metaSecurity.getSystem()).isEqualTo(NOPAT_CV.getCodeSystem())
        );
    }

    private void assertMetaSecurityIsNotPresent(final Meta meta) {
        assertAll(
            () -> assertThat(meta.getSecurity().size()).isEqualTo(0),
            () -> assertThat(meta.getProfile().get(0).getValue()).isEqualTo(DUMMY_PROFILE_URI)
        );
    }

    private static CV createCv(String code, String codeSystem, String displayName) {
        final CV cv = new CV();
        cv.setCode(code);
        cv.setCodeSystem(codeSystem);
        cv.setDisplayName(displayName);
        return cv;
    }
}