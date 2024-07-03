package uk.nhs.adaptors.pss.translator.service;

import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CV;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedactionServiceTest {
    private static final CV CV_WITH_NOPAT = new CV();
    private static final CV CV_WITHOUT_NOPAT = new CV();
    private static final Meta INITIAL_META;

    static {
        CV_WITH_NOPAT.setCode("NOPAT");
        CV_WITHOUT_NOPAT.setCode("NOSCRUB");

        INITIAL_META = new Meta().setProfile(
            Collections.singletonList(
                new UriType("")
            )
        );
    }

    private final RedactionService redactionService;

    @Autowired
    public RedactionServiceTest(RedactionService redactionService) {
        this.redactionService = redactionService;
    }

    @Test
    void When_ConfidentialityCodeWithNopatProvided_Expect_MetaSecurityToBeAdded() {
        // given
        final Meta expectedMeta = redactionService.processRedactions(() -> INITIAL_META,
            Optional.of(CV_WITH_NOPAT), Optional.empty());

        // then
        assertThat(expectedMeta.getSecurity()).hasSize(1);
        assertThat(expectedMeta.getSecurity().get(0).getCode()).isEqualTo("NOPAT");
    }

    @Test
    void When_ConfidentialityCodeWithNopatProvidedAndConfidentialityCodeWithoutNopat_Expect_MetaSecurityToBeAdded() {
        // given
        final Meta expectedMeta = redactionService.processRedactions(() -> INITIAL_META,
            Optional.of(CV_WITH_NOPAT), Optional.of(CV_WITHOUT_NOPAT));

        // then
        assertThat(expectedMeta.getSecurity()).hasSize(1);
        assertThat(expectedMeta.getSecurity().get(0).getCode()).isEqualTo("NOPAT");
    }

    @Test
    void When_ConfidentialityCodeWithoutNopatProvided_Expect_MetaSecurityNotToBeAdded() {
        // when
        final Meta expectedMeta = redactionService.processRedactions(() -> INITIAL_META,
            Optional.of(CV_WITHOUT_NOPAT));

        // then
        assertThat(expectedMeta.getSecurity()).hasSize(0);
    }

    @Test
    void When_ConfidentialityCodeWithOptionalEmptyIsProvided_Expect_MetaSecurityNotToBeAdded() {
        // when
        final Meta expectedMeta = redactionService.processRedactions(() -> INITIAL_META,
            Optional.empty());

        // then
        assertThat(expectedMeta.getSecurity()).hasSize(0);
    }
}