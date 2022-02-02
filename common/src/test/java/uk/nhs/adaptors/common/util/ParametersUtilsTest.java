package uk.nhs.adaptors.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import uk.nhs.adaptors.common.testutil.CreateParametersUtil;
import uk.nhs.adaptors.common.util.fhir.ParametersUtils;

public class ParametersUtilsTest {
    private static final String TEST_NHS_NUMBER = "TEST_NHS_NUMBER";

    @Test
    public void testGetNhsNumberFromParameter() {
        var parameters = CreateParametersUtil.createValidParametersResource(TEST_NHS_NUMBER);
        String nhsNumber = ParametersUtils.getNhsNumberFromParameters(parameters).get().getValue();
        assertEquals(TEST_NHS_NUMBER, nhsNumber);
    }
}
