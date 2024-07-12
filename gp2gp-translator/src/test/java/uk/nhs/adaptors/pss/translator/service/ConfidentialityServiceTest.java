package uk.nhs.adaptors.pss.translator.service;

import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.v3.CV;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKObservationStatement;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfidentialityServiceTest {
    private static final String DUMMY_PROFILE = "MyProfile-1";
    private static final String DUMMY_PROFILE_URI;
    private static final CV ALTERNATIVE_CV;
    private static final CV NOPAT_CV;

    private static ConfidentialityService confidentialityService;

    static {
        DUMMY_PROFILE_URI = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-%s"
            .formatted(DUMMY_PROFILE);

        NOPAT_CV = createCv(
            "NOPAT",
            "http://hl7.org/fhir/v3/ActCode",
            "no disclosure to patient, family or caregivers without attending provider's authorization"
        );

        ALTERNATIVE_CV = createCv(
            "NOSCRUB",
            "http://hl7.org/fhir/v3/FakeCode",
            "no scrubbing of the patient, family or caregivers without attending provider's authorization"
        );
    }

    @BeforeAll
    static void beforeAll() {
        confidentialityService = new ConfidentialityService();
    }

    @Test
    void Given_EhrCompositionWithNopatConfidentialityCodePresent_Expect_AddSecurityToMeta() {
        final RCMRMT030101UKEhrComposition ehrComposition = new RCMRMT030101UKEhrComposition();
        ehrComposition.setConfidentialityCode(NOPAT_CV);
        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            ehrComposition.getConfidentialityCode()
        );

        assertThat(result.getSecurity().size()).isEqualTo(1);
        assertThat(result.getProfile().get(0).getValue()).isEqualTo(DUMMY_PROFILE_URI);
        assertThat(result.getSecurity().get(0).getCode()).isEqualTo(NOPAT_CV.getCode());
        assertThat(result.getSecurity().get(0).getDisplay()).isEqualTo(NOPAT_CV.getDisplayName());
        assertThat(result.getSecurity().get(0).getSystem()).isEqualTo(NOPAT_CV.getCodeSystem());
    }

    @Test
    void Given_EhrCompositionWithConfidentialityCodeOtherThanNopatPresent_Expect_SecurityNotAddedToMeta() {
        final RCMRMT030101UKEhrComposition ehrComposition = new RCMRMT030101UKEhrComposition();
        ehrComposition.setConfidentialityCode(ALTERNATIVE_CV);
        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            ehrComposition.getConfidentialityCode()
        );

        assertThat(result.getSecurity().size()).isEqualTo(0);
        assertThat(result.getProfile().get(0).getValue()).isEqualTo(DUMMY_PROFILE_URI);
    }

    @Test
    void Given_EhrCompositionWithoutConfidentialityCodePresent_Expect_SecurityNotAddedToMeta() {
        final RCMRMT030101UKEhrComposition ehrComposition = new RCMRMT030101UKEhrComposition();
        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            ehrComposition.getConfidentialityCode()
        );

        assertThat(result.getSecurity().size()).isEqualTo(0);
        assertThat(result.getProfile().get(0).getValue()).isEqualTo(DUMMY_PROFILE_URI);
    }

    @Test
    void Given_ObservationStatementWithNopatConfidentialityCodePresent_Expect_SecurityAddedToMeta() {
        final RCMRMT030101UKObservationStatement observationStatement = new RCMRMT030101UKObservationStatement();
        observationStatement.setConfidentialityCode(NOPAT_CV);
        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            observationStatement.getConfidentialityCode()
        );

        assertThat(result.getSecurity().size()).isEqualTo(1);
        assertThat(result.getProfile().get(0).getValue()).isEqualTo(DUMMY_PROFILE_URI);
        assertThat(result.getSecurity().get(0).getCode()).isEqualTo(NOPAT_CV.getCode());
        assertThat(result.getSecurity().get(0).getDisplay()).isEqualTo(NOPAT_CV.getDisplayName());
        assertThat(result.getSecurity().get(0).getSystem()).isEqualTo(NOPAT_CV.getCodeSystem());
    }

    @Test
    void Given_ObservationStatementWithConfidentialityCodeOtherThanNopatPresent_Expect_SecurityNotAddedToMeta() {
        final RCMRMT030101UKObservationStatement observationStatement = new RCMRMT030101UKObservationStatement();
        observationStatement.setConfidentialityCode(ALTERNATIVE_CV);
        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            observationStatement.getConfidentialityCode()
        );

        assertThat(result.getSecurity().size()).isEqualTo(0);
        assertThat(result.getProfile().get(0).getValue()).isEqualTo(DUMMY_PROFILE_URI);
    }

    @Test
    void Given_ObservationStatementWithoutConfidentialityCodePresent_Expect_SecurityNotAddedToMeta() {
        final RCMRMT030101UKObservationStatement observationStatement = new RCMRMT030101UKObservationStatement();
        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            DUMMY_PROFILE,
            observationStatement.getConfidentialityCode()
        );

        assertThat(result.getSecurity().size()).isEqualTo(0);
        assertThat(result.getProfile().get(0).getValue()).isEqualTo(DUMMY_PROFILE_URI);
    }

    private static CV createCv(String code, String codeSystem, String displayName) {
        final CV cv = new CV();
        cv.setCode(code);
        cv.setCodeSystem(codeSystem);
        cv.setDisplayName(displayName);
        return cv;
    }
}