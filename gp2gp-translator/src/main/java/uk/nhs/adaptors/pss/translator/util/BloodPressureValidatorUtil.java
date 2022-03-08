package uk.nhs.adaptors.pss.translator.util;

import static uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil.getObservationStatementsFromCompoundStatement;

import java.util.Arrays;
import java.util.List;

import org.hl7.v3.RCMRMT030101UK04CompoundStatement;

public class BloodPressureValidatorUtil {
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

    private static final List<List<List<String>>> VALID_BLOOD_PRESSURE_TRIPLES = Arrays.asList(
        Arrays.asList(HEADER_1, SYSTOLIC_1, DIASTOLIC_1),
        Arrays.asList(HEADER_1, SYSTOLIC_2, DIASTOLIC_2),
        Arrays.asList(HEADER_2, SYSTOLIC_1, DIASTOLIC_1),
        Arrays.asList(HEADER_2, SYSTOLIC_2, DIASTOLIC_2),
        Arrays.asList(HEADER_3, SYSTOLIC_1, DIASTOLIC_1),
        Arrays.asList(HEADER_3, SYSTOLIC_2, DIASTOLIC_2),
        Arrays.asList(HEADER_4, SYSTOLIC_3, DIASTOLIC_3),
        Arrays.asList(HEADER_5, SYSTOLIC_4, DIASTOLIC_4),
        Arrays.asList(HEADER_6, SYSTOLIC_5, DIASTOLIC_5));

    public static boolean validateBloodPressureTriple(String header, String observationStatement1, String observationStatement2) {
        return validateTriple(header, observationStatement1, observationStatement2);
    }

    public static boolean isSystolicBloodPressure(String code) {
        return SYSTOLIC_1.contains(code) || SYSTOLIC_2.contains(code) || SYSTOLIC_3.contains(code) || SYSTOLIC_4.contains(code)
            || SYSTOLIC_5.contains(code);
    }

    public static boolean isDiastolicBloodPressure(String code) {
        return DIASTOLIC_1.contains(code) || DIASTOLIC_2.contains(code) || DIASTOLIC_3.contains(code) || DIASTOLIC_4.contains(code)
            || DIASTOLIC_5.contains(code);
    }

    public static boolean containsValidBloodPressureTriple(RCMRMT030101UK04CompoundStatement compoundStatement) {
        var observationStatements = getObservationStatementsFromCompoundStatement(compoundStatement);

        if (observationStatements.size() == 2) {
            return BloodPressureValidatorUtil.validateBloodPressureTriple(compoundStatement.getCode().getCode(),
                observationStatements.get(0).getCode().getCode(), observationStatements.get(1).getCode().getCode());
        }

        return false;
    }

    private static boolean validateTriple(String header, String observationStatement1, String observationStatement2) {
        for (List<List<String>> list : VALID_BLOOD_PRESSURE_TRIPLES) {
            if (isValidBloodTriple(list.get(0), list.get(1), list.get(2), header, observationStatement1, observationStatement2)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isValidBloodTriple(List<String> validHeaders, List<String> validSystolics, List<String> validDiastolic,
        String header, String observationStatement1, String observationStatement2) {
        return validHeaders.contains(header)
            && (validSystolics.contains(observationStatement1) || validSystolics.contains(observationStatement2))
            && (validDiastolic.contains(observationStatement1) || validDiastolic.contains(observationStatement2));
    }
}
