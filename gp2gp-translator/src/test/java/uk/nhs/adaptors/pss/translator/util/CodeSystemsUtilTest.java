package uk.nhs.adaptors.pss.translator.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CodeSystemsUtilTest {
    @Test
    public void getFhirCodeSystemWhenHL7CodeIsPresentShouldReturnFhirSystemCode() {
        var hl7Code = "2.16.840.1.113883.2.1.3.2.4.15";

        var result = CodeSystemsUtil.getFhirCodeSystem(hl7Code);

        assertThat(result).isEqualTo("http://snomed.info/sct");
    }

    @Test
    public void getFhirCodeSystemWhenHL7CodeNotPresentShouldHL7Code() {
        var hl7Code = "123456";

        var result = CodeSystemsUtil.getFhirCodeSystem(hl7Code);

        assertThat(result).isEqualTo(hl7Code);
    }
}
