package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;

@RunWith(JUnitParamsRunner.class)
public class ExtensionUtilTest {

    @ParameterizedTest
    @MethodSource("urls")
    public void shouldCreateExpectedExtension(String url, Resource resource, ResourceType resourceType) {
        resource.setId(url);
        Reference reference = new Reference(resource);
        Extension extension = ResourceUtil.buildReferenceExtension(url, reference);

        assertThat(extension.getUrl()).isEqualTo(url);
        Reference extensionRef = (Reference) extension.getValue();
        assertThat(extensionRef.getResource().fhirType()).isEqualTo(resourceType.name());
        assertThat(extensionRef.getResource().getIdElement().getIdPart()).isEqualTo(url);
    }

    public static Stream<Arguments> urls() {
        return Stream.of(
            Arguments.of(
                "TEST_URL_OBSERVATION",
                new Observation(),
                ResourceType.Observation
            ),
            Arguments.of(
                "TEST_URL_CONDITION",
                new Condition(),
                ResourceType.Condition
            ),
            Arguments.of(
                "TEST_URL_LOCATION",
                new Location(),
                ResourceType.Location
            ),
            Arguments.of(
                "TEST_URL_REFERRAL_REQUEST",
                new ReferralRequest(),
                ResourceType.ReferralRequest
            )
        );
    }

}
