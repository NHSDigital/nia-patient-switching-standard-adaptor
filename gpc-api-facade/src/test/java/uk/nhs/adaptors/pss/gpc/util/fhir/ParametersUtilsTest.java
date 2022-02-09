package uk.nhs.adaptors.pss.gpc.util.fhir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import uk.nhs.adaptors.common.testutil.CreateParametersUtil;

public class ParametersUtilsTest {
    private static final String TEST_NHS_NUMBER = "TEST_NHS_NUMBER";

    @Test
    public void testGetNhsNumberFromParameter() {
        var parameters = CreateParametersUtil.createValidParametersResource(TEST_NHS_NUMBER);
        String nhsNumber = ParametersUtils.getNhsNumberFromParameters(parameters).get().getValue();
        assertEquals(TEST_NHS_NUMBER, nhsNumber);
    }
}
