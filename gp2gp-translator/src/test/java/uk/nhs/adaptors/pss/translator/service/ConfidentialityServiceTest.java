package uk.nhs.adaptors.pss.translator.service;

import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.v3.CV;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKObservationStatement;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

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

        NOPAT_CV = new CV();
        NOPAT_CV.setCode("NOPAT");
        NOPAT_CV.setCodeSystem("http://hl7.org/fhir/v3/ActCode");
        NOPAT_CV.setDisplayName("no disclosure to patient, family or caregivers without attending provider's authorization");

        ALTERNATIVE_CV = new CV();
        ALTERNATIVE_CV.setCode("NOSCRUB");
        ALTERNATIVE_CV.setCodeSystem("http://hl7.org/fhir/v3/ActCode");
        ALTERNATIVE_CV.setDisplayName("no scrubbing of the patient, family or caregivers without attending provider's authorization");
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
            Collections.singletonList((ehrComposition.getConfidentialityCode())),
            DUMMY_PROFILE
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
            Collections.singletonList((ehrComposition.getConfidentialityCode())),
            DUMMY_PROFILE
        );

        assertThat(result.getSecurity().size()).isEqualTo(0);
        assertThat(result.getProfile().get(0).getValue()).isEqualTo(DUMMY_PROFILE_URI);
    }

    @Test
    void Given_EhrCompositionWithoutConfidentialityCodePresent_Expect_SecurityNotAddedToMeta() {
        final RCMRMT030101UKEhrComposition ehrComposition = new RCMRMT030101UKEhrComposition();
        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            Collections.singletonList((ehrComposition.getConfidentialityCode())),
            DUMMY_PROFILE
        );

        assertThat(result.getSecurity().size()).isEqualTo(0);
        assertThat(result.getProfile().get(0).getValue()).isEqualTo(DUMMY_PROFILE_URI);
    }

    @Test
    void Given_ObservationStatementWithNopatConfidentialityCodePresent_Expect_SecurityAddedToMeta() {
        final RCMRMT030101UKObservationStatement observationStatement = new RCMRMT030101UKObservationStatement();
        observationStatement.setConfidentialityCode(NOPAT_CV);
        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            Collections.singletonList((observationStatement.getConfidentialityCode())),
            DUMMY_PROFILE
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
            Collections.singletonList((observationStatement.getConfidentialityCode())),
            DUMMY_PROFILE
        );

        assertThat(result.getSecurity().size()).isEqualTo(0);
        assertThat(result.getProfile().get(0).getValue()).isEqualTo(DUMMY_PROFILE_URI);
    }

    @Test
    void Given_ObservationStatementWithoutConfidentialityCodePresent_Expect_SecurityNotAddedToMeta() {
        final RCMRMT030101UKObservationStatement observationStatement = new RCMRMT030101UKObservationStatement();
        final Meta result = confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            Collections.singletonList((observationStatement.getConfidentialityCode())),
            DUMMY_PROFILE
        );

        assertThat(result.getSecurity().size()).isEqualTo(0);
        assertThat(result.getProfile().get(0).getValue()).isEqualTo(DUMMY_PROFILE_URI);
    }
}