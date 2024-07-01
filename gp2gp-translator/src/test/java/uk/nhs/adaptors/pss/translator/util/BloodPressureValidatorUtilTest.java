package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.hl7.v3.RCMRMT030101UKCompoundStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import lombok.SneakyThrows;

public class BloodPressureValidatorUtilTest {
    private static final List<String> HEADER_1 = Arrays.asList("163020007", "254063019", "254065014", "254064013", "2667419011");
    private static final List<String> HEADER_2 = Arrays.asList("386534000", "1480679010");
    private static final List<String> HEADER_3 = Arrays.asList("7536700", "125176019");
    private static final List<String> HEADER_4 = Arrays.asList("163034007", "254081016", "254080015");
    private static final List<String> HEADER_5 = Arrays.asList("163035008", "254082011", "254083018");
    private static final List<String> HEADER_6 = Arrays.asList("163033001", "254078014", "254079018");

    private static final List<String> SYSTOLIC_1 = Arrays.asList("72313002", "120159016");
    private static final List<String> SYSTOLIC_2 = Arrays.asList("271649006", "406507015");
    private static final List<String> SYSTOLIC_3 = Arrays.asList("400974009", "1780182014");
    private static final List<String> SYSTOLIC_4 = Arrays.asList("407554009", "2159155011");
    private static final List<String> SYSTOLIC_5 = Arrays.asList("407556006", "2159157015");

    private static final List<String> DIASTOLIC_1 = Arrays.asList("1091811000000102", "2734671000000117");
    private static final List<String> DIASTOLIC_2 = Arrays.asList("271650006", "406508013");
    private static final List<String> DIASTOLIC_3 = Arrays.asList("400975005", "1780183016");
    private static final List<String> DIASTOLIC_4 = Arrays.asList("407555005", "2159156012");
    private static final List<String> DIASTOLIC_5 = Arrays.asList("407557002", "2159158013");

    private static final String INVALID_CODE = "123456789";
    private static final String BLOOD_PRESSURE_DIRECTORY = "xml/BloodPressure/";
    private static final String TRIPLE_WITHOUT_SNOMED = "triple_without_snomed.xml";
    private static final String TRIPLE_WITH_SNOMED = "triple_with_snomed.xml";
    private static final String TRIPLE_WITH_SNOMED_TRANSLATION = "triple_with_snomed_translation.xml";

    @ParameterizedTest(name = "validBloodPressureTriple")
    @MethodSource("validBloodPressureTriples")
    public void validBloodPressureTripleReturnsTrue(String header, String systolic, String diastolic) {
        assertThat(BloodPressureValidatorUtil.validateBloodPressureTriple(header, systolic, diastolic)).isTrue();
    }

    @Test
    public void invalidBloodPressureTripleReturnsFalse() {
        assertThat(BloodPressureValidatorUtil.validateBloodPressureTriple("123456789", "987654321",
            "192837465")).isFalse();
    }

    @ParameterizedTest(name = "validSystolicBloodPressure")
    @MethodSource("validSystolicBloodPressures")
    public void validSystolicBloodPressureCodeReturnsTrue(String systolicCode) {
        assertThat(BloodPressureValidatorUtil.isSystolicBloodPressure(systolicCode)).isTrue();
    }

    @Test
    public void invalidSystolicBloodPressureCodeReturnsFalse() {
        assertThat(BloodPressureValidatorUtil.isSystolicBloodPressure(INVALID_CODE)).isFalse();
    }

    @ParameterizedTest(name = "validDiastolicBloodPressure")
    @MethodSource("validDiastolicBloodPressures")
    public void validDiastolicBloodPressureCodeReturnsTrue(String diastolicCode) {
        assertThat(BloodPressureValidatorUtil.isDiastolicBloodPressure(diastolicCode)).isTrue();
    }

    @Test
    public void invalidDiastolicBloodPressureCodeReturnsFalse() {
        assertThat(BloodPressureValidatorUtil.isDiastolicBloodPressure(INVALID_CODE)).isFalse();
    }

    @Test
    public void When_ContainsBloodPressureTriple_With_SnomedCode_Expect_True() {
        var compoundStatement = unmarshallCompoundStatement(TRIPLE_WITH_SNOMED);

        assertThat(BloodPressureValidatorUtil.containsValidBloodPressureTriple(compoundStatement)).isTrue();
    }

    @Test
    public void When_ContainedBloodPressureTriple_With_SnomedTranslation_Expect_True() {
        var compoundStatement = unmarshallCompoundStatement(TRIPLE_WITH_SNOMED_TRANSLATION);

        assertThat(BloodPressureValidatorUtil.containsValidBloodPressureTriple(compoundStatement)).isTrue();
    }

    @Test
    public void When_ContainsBloodPressureTriple_With_MissingSnomed_Expect_False() {
        var compoundStatement = unmarshallCompoundStatement(TRIPLE_WITHOUT_SNOMED);

        assertThat(BloodPressureValidatorUtil.containsValidBloodPressureTriple(compoundStatement)).isFalse();
    }

    private static Stream<Arguments> validBloodPressureTriples() {
        return Stream.of(
                generateBloodPressureTriples(HEADER_1, SYSTOLIC_1, DIASTOLIC_1),
                generateBloodPressureTriples(HEADER_1, SYSTOLIC_2, DIASTOLIC_2),
                generateBloodPressureTriples(HEADER_2, SYSTOLIC_1, DIASTOLIC_1),
                generateBloodPressureTriples(HEADER_2, SYSTOLIC_2, DIASTOLIC_2),
                generateBloodPressureTriples(HEADER_3, SYSTOLIC_1, DIASTOLIC_1),
                generateBloodPressureTriples(HEADER_3, SYSTOLIC_2, DIASTOLIC_2),
                generateBloodPressureTriples(HEADER_4, SYSTOLIC_3, DIASTOLIC_3),
                generateBloodPressureTriples(HEADER_5, SYSTOLIC_4, DIASTOLIC_4),
                generateBloodPressureTriples(HEADER_6, SYSTOLIC_5, DIASTOLIC_5))
            .flatMap(Collection::stream);
    }

    private static Stream<Arguments> validSystolicBloodPressures() {
        return Stream.of(
            Arguments.of("72313002"),
            Arguments.of("120159016"),
            Arguments.of("271649006"),
            Arguments.of("406507015"),
            Arguments.of("400974009"),
            Arguments.of("1780182014"),
            Arguments.of("407554009"),
            Arguments.of("2159155011"),
            Arguments.of("407556006"),
            Arguments.of("2159157015"));
    }

    private static Stream<Arguments> validDiastolicBloodPressures() {
        return Stream.of(
            Arguments.of("1091811000000102"),
            Arguments.of("2734671000000117"),
            Arguments.of("271650006"),
            Arguments.of("406508013"),
            Arguments.of("400975005"),
            Arguments.of("1780183016"),
            Arguments.of("407555005"),
            Arguments.of("2159156012"),
            Arguments.of("407557002"),
            Arguments.of("2159158013"));
    }

    private static ArrayList<Arguments> generateBloodPressureTriples(List<String> headers, List<String> systolics,
        List<String> diastolics) {
        var testCases = new ArrayList<Arguments>();
        for (String header : headers) {
            for (String systolic : systolics) {
                for (String diastolic : diastolics) {
                    testCases.add(Arguments.of(header, systolic, diastolic));
                }
            }
        }

        return testCases;
    }

    @SneakyThrows
    private RCMRMT030101UKCompoundStatement unmarshallCompoundStatement(String fileName) {
        return unmarshallFile(getFile("classpath:" + BLOOD_PRESSURE_DIRECTORY + fileName),
            RCMRMT030101UKCompoundStatement.class);
    }
}
