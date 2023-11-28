package uk.nhs.adaptors.pss.gpc.util.fhir;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Extension;
import org.junit.jupiter.api.Test;
import uk.nhs.adaptors.common.util.CodeableConceptUtils;
import java.net.URISyntaxException;

public class CodeableConceptUtilsTest {

    private static final String CODE = "RESOURCE_NOT_FOUND";
    private static final String ISSUE_SYSTEM = "Spine-ErrorOrWarningCode-1";
    private static final String DISPLAY = "Resource not found";
    private static final String TEXT = "Resource got lost";

    @Test
    public void testCreateCodeableConcept() {
        CodeableConcept result = CodeableConceptUtils.createCodeableConcept(CODE, ISSUE_SYSTEM, DISPLAY, TEXT);

        assertAll(
            () -> assertEquals(CODE, result.getCodingFirstRep().getCode()),
            () -> assertEquals(ISSUE_SYSTEM, result.getCodingFirstRep().getSystem()),
            () -> assertEquals(DISPLAY, result.getCodingFirstRep().getDisplay()),
            () -> assertEquals(TEXT, result.getText())
        );
    }

    @Test
    public void testCreateCodeableConceptWithNullText() {

        CodeableConcept result = CodeableConceptUtils.createCodeableConcept(CODE, ISSUE_SYSTEM, DISPLAY, null);

        assertAll(
            () -> assertEquals(CODE, result.getCodingFirstRep().getCode()),
            () -> assertEquals(ISSUE_SYSTEM, result.getCodingFirstRep().getSystem()),
            () -> assertEquals(DISPLAY, result.getCodingFirstRep().getDisplay()),
            () -> assertThat(result.getText()).isNull()
        );
    }

    @Test
    public void testCreateCodeableConceptWithExtension() throws URISyntaxException {

        final String EXTENSION_URL = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-coding-sctdescid";
        final Extension extension = new Extension().setUrl(EXTENSION_URL);
        CodeableConcept result = CodeableConceptUtils.createCodeableConcept(CODE, ISSUE_SYSTEM, DISPLAY, null, extension);

        assertEquals(CODE, result.getCodingFirstRep().getCode());
        assertEquals(ISSUE_SYSTEM, result.getCodingFirstRep().getSystem());
        assertEquals(DISPLAY, result.getCodingFirstRep().getDisplay());
        assertEquals(EXTENSION_URL, result.getCoding().get(0).getExtension().get(0).getUrlElement().getValue());
        assertThat(result.getText()).isNull();
    }

    @Test
    public void testCreateCodeableConceptWithDoubleCodingSections() throws URISyntaxException {
        final String EXTENSION_URL = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-coding-sctdescid";
        var result = CodeableConceptUtils.createCodeableConceptWithDoubleCoding(CODE, ISSUE_SYSTEM, DISPLAY, null, "99");

        assertAll(
            () -> assertEquals(ISSUE_SYSTEM, result.getCoding().get(0).getSystem()),
            () -> assertEquals(CODE, result.getCoding().get(0).getCode()),
            () -> assertEquals(DISPLAY, result.getCoding().get(0).getDisplay())
        );
        assertAll(
            () -> assertEquals("urn:oid:2.16.840.1.113883.2.1.3.2.4.17.101", result.getCoding().get(1).getSystem()),
            () -> assertEquals("99", result.getCoding().get(1).getCode()),
            () -> assertEquals(DISPLAY, result.getCoding().get(1).getDisplay())
        );
    }

}
