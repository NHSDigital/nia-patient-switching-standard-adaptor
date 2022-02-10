package uk.nhs.adaptors.pss.translator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.nio.file.Files;
import java.util.stream.Stream;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.v3.RCMRIN030000UK06Message;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;
import lombok.SneakyThrows;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class BundleMapperServiceTest {

    private static final String TEST_ID = "TEST_ID_123";

    private static final String XML_RESOURCES_BASE = "xml/RCMRIN030000UK06/";
    private static final String EXPECTED_JSON_BASE = "json/RCMRIN030000UK06/";

    private static final String STRUCTURED_RECORD_SMALL_XML = "structuredRecord_small.xml";
    private static final String STRUCTURED_RECORD_XML = "structuredRecord.xml";


    private static final String EXPECTED_BUNDLE_FROM_STRUCTURED_RECORD = "expected_bundle_from_structuredRecord.json";


    @MockBean
    private FhirIdGeneratorService idGenerator;

    @Autowired
    private BundleMapperService bundleMapperService;

    @BeforeEach
    public void setup() {
        when(idGenerator.generateUuid()).thenReturn(TEST_ID);
    }

    @Test
    public void testBundleIsGenerated() {
        final RCMRIN030000UK06Message xml = unmarshallCodeElement(STRUCTURED_RECORD_SMALL_XML);
        final Bundle bundle = bundleMapperService.mapToBundle(xml);

        assertThat(bundle.getEntry()).isNotNull();
        assertThat(bundle.getType()).isEqualTo(Bundle.BundleType.COLLECTION);
        assertThat(bundle.hasId()).isTrue();
        assertThat(bundle.hasMeta()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("testFiles")
    public void testBundleContainsMappedResources(String inputXML, String expectedJson) throws JSONException {
        final RCMRIN030000UK06Message xml = unmarshallCodeElement(inputXML);
        final String expectedJsonOutput = getFileAsString(EXPECTED_JSON_BASE, expectedJson);
        final Bundle bundle = bundleMapperService.mapToBundle(xml);

        final String jsonBundle = parseBundleToJson(bundle);

        JSONObject bundleJsonObject = new JSONObject(jsonBundle);
        JSONObject expectedJsonObject = new JSONObject(expectedJsonOutput);

        assertThat(bundleJsonObject.toString()).isEqualTo(expectedJsonObject.toString());
    }

    private static Stream<Arguments> testFiles() {
        return Stream.of(
            Arguments.of(STRUCTURED_RECORD_XML, EXPECTED_BUNDLE_FROM_STRUCTURED_RECORD)
        );
    }

    private String parseBundleToJson(Bundle bundle) {
        return FhirContext.forDstu3().newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    @SneakyThrows
    private String getFileAsString(String base, String fileName) {
        return Files.readString(ResourceUtils.getFile("classpath:" + base + fileName).toPath());
    }

    @SneakyThrows
    private RCMRIN030000UK06Message unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRIN030000UK06Message.class);
    }
}
