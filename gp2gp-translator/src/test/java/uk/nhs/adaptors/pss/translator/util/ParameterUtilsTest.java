package uk.nhs.adaptors.pss.translator.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import uk.nhs.adaptors.pss.translator.testutil.CreateParametersUtil;

public class ParameterUtilsTest {
    private static final String TEST_NHS_NUMBER = "TEST_NHS_NUMBER";

    @Test
    public void testGetNhsNumberFromParameter() {
        var parameters = CreateParametersUtil.createValidParametersResource(TEST_NHS_NUMBER);
        String nhsNumber = ParametersUtils.getNhsNumberFromParameters(parameters).get().getValue();
        assertEquals(TEST_NHS_NUMBER, nhsNumber);
    }
}
