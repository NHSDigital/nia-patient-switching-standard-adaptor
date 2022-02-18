package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

    @ParameterizedTest(name = "validBloodPressureTriple")
    @MethodSource("validBloodTriples")
    public void validBloodPressureTripleReturnsTrue(String header, String systolic, String diastolic) {
        assertThat(BloodPressureValidatorUtil.validateBloodPressureTriple(header, systolic, diastolic)).isTrue();
    }

    @Test
    public void invalidBloodPressureTripleReturnsFalse() {
        assertThat(BloodPressureValidatorUtil.validateBloodPressureTriple("123456789", "987654321",
            "192837465")).isFalse();
    }

    private static Stream<Arguments> validBloodTriples() {
        return Stream.of(
                generateTestCases(HEADER_1, SYSTOLIC_1, DIASTOLIC_1),
                generateTestCases(HEADER_1, SYSTOLIC_2, DIASTOLIC_2),
                generateTestCases(HEADER_2, SYSTOLIC_1, DIASTOLIC_1),
                generateTestCases(HEADER_2, SYSTOLIC_2, DIASTOLIC_2),
                generateTestCases(HEADER_3, SYSTOLIC_1, DIASTOLIC_1),
                generateTestCases(HEADER_3, SYSTOLIC_2, DIASTOLIC_2),
                generateTestCases(HEADER_4, SYSTOLIC_3, DIASTOLIC_3),
                generateTestCases(HEADER_5, SYSTOLIC_4, DIASTOLIC_4),
                generateTestCases(HEADER_6, SYSTOLIC_5, DIASTOLIC_5))
            .flatMap(Collection::stream)
            .collect(Collectors.toList()).stream();
    }

    private static ArrayList<Arguments> generateTestCases(List<String> headers, List<String> systolics, List<String> diastolics) {
        var testCases = new ArrayList<Arguments>();
        for (String header :
            headers) {
            for (String systolic :
                systolics) {
                for (String diastolic :
                    diastolics) {
                    testCases.add(Arguments.of(header, systolic, diastolic));
                }
            }
        }

        return testCases;
    }
}
