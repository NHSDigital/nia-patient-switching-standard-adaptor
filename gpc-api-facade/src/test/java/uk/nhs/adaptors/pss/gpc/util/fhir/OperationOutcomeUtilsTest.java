package uk.nhs.adaptors.pss.gpc.util.fhir;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.dstu3.model.OperationOutcome.IssueType.NOTSUPPORTED;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.junit.jupiter.api.Test;

import uk.nhs.adaptors.common.util.CodeableConceptUtils;

public class OperationOutcomeUtilsTest {
    private static final String URI_TYPE = "https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-OperationOutcome-1";

    @Test
    public void testCreateOperationOutcome() {
        var details = createCodeableConcept();
        var diagnostics = "Dancing not supported";

        OperationOutcome result = OperationOutcomeUtils.createOperationOutcome(NOTSUPPORTED, ERROR, details, diagnostics);

        assertThat(result.getMeta().getProfile().get(0).equals(URI_TYPE)).isTrue();
        assertThat(result.getIssueFirstRep().getCode()).isEqualTo(NOTSUPPORTED);
        assertThat(result.getIssueFirstRep().getSeverity()).isEqualTo(ERROR);
        assertThat(result.getIssueFirstRep().getDetails()).isEqualTo(details);
        assertThat(result.getIssueFirstRep().getDiagnostics()).isEqualTo(diagnostics);
    }

    private CodeableConcept createCodeableConcept() {
        return CodeableConceptUtils.createCodeableConcept("code", "system", "display", "text");
    }
}
