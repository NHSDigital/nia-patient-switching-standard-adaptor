package uk.nhs.adaptors.pss.translator.util;

import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CV;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ConfidentialityUtilTest {
    private static final CV CV_WITH_NOPAT = new CV();
    private static final CV CV_WITHOUT_NOPAT = new CV();
    private static final Meta INITIAL_META = new Meta().setProfile(
        Collections.singletonList(
            new UriType("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-AllergyIntolerance-1")
        )
    );

    static {
        CV_WITH_NOPAT.setCode("NOPAT");
        CV_WITHOUT_NOPAT.setCode("NOSCRUB");
    }

    @Test
    void Given_CvWithNopatAndOptionalEmpty_Expect_MetaSecurityAdded() {
        // when
        final Meta expected = ConfidentialityUtil.addSecurityToMetaIfConfidentialityCodesPresent(List.of(
            Optional.of(CV_WITH_NOPAT),
            Optional.empty()
        ), INITIAL_META);

        // then
        assertThat(expected.getSecurity()).hasSize(1);
        assertThat(expected.getSecurity().get(0).getCode()).isEqualTo("NOPAT");
    }

    @Test
    void Given_CvWithNopatAndCvWithoutNopat_Expect_MetaSecurityAdded() {
        // when
        final Meta expected = ConfidentialityUtil.addSecurityToMetaIfConfidentialityCodesPresent(List.of(
            Optional.of(CV_WITH_NOPAT),
            Optional.of(CV_WITHOUT_NOPAT)
        ), INITIAL_META);

        // then
        assertThat(expected.getSecurity()).hasSize(1);
        assertThat(expected.getSecurity().get(0).getCode()).isEqualTo("NOPAT");
    }

    @Test
    void Given_CvWithoutNopatAndOptionalEmpty_Expect_MetaSecurityNotAdded() {
        // when
        final Meta expected = ConfidentialityUtil.addSecurityToMetaIfConfidentialityCodesPresent(List.of(
            Optional.of(CV_WITHOUT_NOPAT),
            Optional.empty()
        ), INITIAL_META);

        // then
        assertThat(expected.getSecurity()).isEmpty();
    }
}