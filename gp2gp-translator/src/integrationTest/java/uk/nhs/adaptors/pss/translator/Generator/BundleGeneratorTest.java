package uk.nhs.adaptors.pss.translator.Generator;

import java.io.IOException;
import java.util.stream.Stream;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import uk.nhs.adaptors.pss.translator.generator.BundleGenerator;
import uk.nhs.adaptors.pss.translator.utils.ResourceTestFileUtils;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class BundleGeneratorTest {

    private static final BundleGenerator BUNDLE = new BundleGenerator();
    private static final String BUNDLE_RESOURCE = "/generator/bundleResource.json";

    private static Stream<Arguments> resourceFileParams() {
        return Stream.of(
            Arguments.of(BUNDLE_RESOURCE)
        );
    }

    @ParameterizedTest
    @MethodSource("resourceFileParams")
    public void When_GeneratingBundleResource_Expect_BundleResourceJson(String outputBundle)
        throws IOException {
        CharSequence expectedOutputJson = ResourceTestFileUtils.getFileContent(outputBundle);
        AssertionsForClassTypes.assertThat(BUNDLE).isEqualTo(expectedOutputJson);

    }
}
