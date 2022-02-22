package uk.nhs.adaptors.pss.gpc.util.fhir;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.junit.jupiter.api.Test;

import uk.nhs.adaptors.common.util.CodeableConceptUtils;

public class CodeableConceptUtilsTest {
    private static final String CODE = "RESOURCE_NOT_FOUND";
    private static final String ISSUE_SYSTEM = "Spine-ErrorOrWarningCode-1";
    private static final String DISPLAY = "Resource not found";
    private static final String TEXT = "Resource got lost";

    @Test
    public void testCreateCodeableConcept() {
        CodeableConcept result = CodeableConceptUtils.createCodeableConcept(CODE, ISSUE_SYSTEM, DISPLAY, TEXT);

        assertThat(result.getCodingFirstRep().getCode()).isEqualTo(CODE);
        assertThat(result.getCodingFirstRep().getSystem()).isEqualTo(ISSUE_SYSTEM);
        assertThat(result.getCodingFirstRep().getDisplay()).isEqualTo(DISPLAY);
        assertThat(result.getText()).isEqualTo(TEXT);
    }

    @Test
    public void testCreateCodeableConceptWithNullText() {
        CodeableConcept result = CodeableConceptUtils.createCodeableConcept(CODE, ISSUE_SYSTEM, DISPLAY, null);

        assertThat(result.getCodingFirstRep().getCode()).isEqualTo(CODE);
        assertThat(result.getCodingFirstRep().getSystem()).isEqualTo(ISSUE_SYSTEM);
        assertThat(result.getCodingFirstRep().getDisplay()).isEqualTo(DISPLAY);
        assertThat(result.getText()).isNull();
    }
}
