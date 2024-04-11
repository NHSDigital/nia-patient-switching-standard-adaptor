package uk.nhs.adaptors.pss.translator.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.nhs.adaptors.common.util.OidUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class OidUtilTest {
    @ParameterizedTest
    @ValueSource(strings = {"1.1", "1.2.3", "1.2.4.3", "0.1.2.3.4.5", "2.16.840.1.113883.2.1.4.5.5"})
    public void When_IsOidIsPassedAValueWhichIsAnOID_Expect_True(String value) {
        var actual = OidUtil.isOid(value);

        assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1", "1.0", "1-1", "1.a.3", "1.2.3.", "not-oid", "not.oid", "not an oid", "3.1.2.3"})
    public void When_IsOidIsPassedAValueWhichIsNotAnOID_Expect_False(String value) {
        var actual = OidUtil.isOid(value);

        assertThat(actual).isFalse();
    }

    @Test
    public void When_TryParseToUrnWithAnOid_Expect_OidIsReturnedAsUrn() {
        final var oid = "2.16.840.1.113883.2.1.4.5.5";
        final var expected = "urn:oid:" + oid;

        final var actual = OidUtil.tryParseToUrn(oid);
        assert actual.isPresent();

        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void When_TryParseToUrnWithAnNonOid_Expect_ResultIsNotPresent() {
        final var nonOid = "not.an.oid";

        final var actual = OidUtil.tryParseToUrn(nonOid);

        assertThat(actual).isNotPresent();
    }
}
