package uk.nhs.adaptors.pss.translator.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.nhs.adaptors.pss.translator.service.FhirIDGeneratorService;

@SpringBootTest
@ExtendWith({SpringExtension.class})
@DirtiesContext
public class BundleGeneratorTest {

    @Mock
    private FhirIDGeneratorService FhirIdGeneratorService;

    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-StructuredRecord-Bundle-1";
    private static final String BUNDLE_ID = "219b7cb8-da9e-447a-b279-82be7a3299da";

    @BeforeEach
    public void setUp() throws IOException {
        when(FhirIdGeneratorService.generateUuid()).thenReturn(BUNDLE_ID);
    }

    @Test
    public void When_GeneratingBundleResource_Expect_BundleResourceJson() throws IOException {
        BundleGenerator bundleGenerator = new BundleGenerator(FhirIdGeneratorService);
        var bundle = bundleGenerator.generateBundle();

        assertThat(bundle.getResourceType().toString()).isEqualTo(ResourceType.Bundle.name());
        assertThat(bundle.getId()).isEqualTo(BUNDLE_ID);
        assertThat(bundle.getType()).isEqualTo(Bundle.BundleType.COLLECTION);
        assertThat(bundle.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
    }
}